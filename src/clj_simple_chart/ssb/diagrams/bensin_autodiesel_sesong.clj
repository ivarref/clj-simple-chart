(ns clj-simple-chart.ssb.diagrams.bensin-autodiesel-sesong
  (:require [clojure.string :as str]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.line :refer [line]]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.point :as point]))

; Tabell 11174
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def data (->> {[:ContentsCode :as :salg]               "Salg"
                "Region"                                "Hele landet"
                [:PetroleumProd :as :petroleumsprodukt] ["Bilbensin" "Autodiesel"]
                [:Kjopegrupper :as :kjopegruppe]        "Alle kjøpegrupper"
                [:Tid :as :dato]                        "*"}
               (ssb/fetch 11174)
               (drop-columns [:ContentsCodeCategory :Region :kjopegruppe])
               (remove #(= "Petroleumsprodukter i alt" (:petroleumsprodukt %)))
               (column-value->column :petroleumsprodukt)
               (contract-by-column :dato)
               (remove-whitespace-in-keys)
               ;(flat->12-mms)
               (add-sum-column)
               (keep-columns [:sum :dato])
               (map #(assoc % :month (subs (:dato %) 5 7)))
               (map #(assoc % :year (subs (:dato %) 0 4)))))

(def domain (vec (sort (distinct (map :month data)))))
(def years (vec (sort (distinct (map :year data)))))

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   domain
         :tick-format   (fn [x] (str/replace x "-12" ""))
         :domain        domain
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         ;:grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 500]})

(def lst (last data))

(def header (opentype/stack
              {:width available-width}
              [{:text "Sal av petroleumsprodukt etter type" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 15
                :text          (str "Heile landet.")
                :font          "Roboto Regular" :font-size 18}
               {:margin-bottom 3 :text "Millionar liter" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: SSB tabell 11174" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def prop->color
  [[:autodiesel brown "Autodiesel"]
   [:bilbensin blue "Bilbensin"]])


(def year->color
  [
   ;["2010" brown {:stroke-opacity 0.3}]
   ;["2011" red]
   ;["2012" orange]
   ;["2013" blue]
   ["2014" brown]
   ["2015" red]
   ["2016" orange]
   ["2017" blue]
   ["2018" green {:stroke-opacity 1 :stroke-width 5}]])

(def legend
  (opentype/stack {:fill         "whitesmoke"
                   :fill-opacity 0.8
                   :margin       8
                   :y            (:plot-height c)
                   :x            5
                   :grow-upwards 10}
                  {:text "År" :font "Roboto Bold" :font-size 18}
                  (for [[year col] (reverse year->color)]
                    {:rect {:fill col}
                     :text year :font "Roboto Regular" :font-size 18})))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     [:g (for [[year color path] year->color]
           (line c {:p :month
                    :h [[:sum color]]
                    :path (merge {:stroke-width 3
                                  :stroke-opacity 0.75} path)}
                 (filter #(= year (:year %)) data)))]
     legend]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "img/ssb-svg/bensin-autodiesel-sesong.svg" "img/ssb-png/bensin-autodiesel-sesong.png" (diagram)))
