(ns clj-simple-chart.ssb.diagrams.koyrelengde-etter-kategori
  (:require [clojure.string :as str]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; 07301 - https://www.ssb.no/statbank/table/07301?rxid=7b60ade9-9818-4a73-b33c-6389472eb597

(def data (->> {[:ContentsCode :as :km] "*"
                :Kjoretoytype           "*"
                [:Tid :as :dato]        "*"}
               (ssb/fetch 7301)
               (drop-columns [:ContentsCodeCategory])
               (column-value->column :Kjoretoytype)
               (remove-whitespace-in-keys)
               (contract-by-column :dato)
               (keep-columns [:dato
                              :personbiler-i-alt
                              :busser-i-alt
                              :små-godsbiler-i-alt
                              :store-lastebiler-i-alt])
               (div-by-no-round 1000)
               (add-sum-column)))

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (map :dato data)
         :domain        (map :dato data)
         :padding-inner 0.4
         :padding-outer 0.4})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (->> (map :sum data)
                                     (apply max))]})

(def header (opentype/stack
              {:width available-width}
              [{:margin-bottom 20 :text "Køyrelengde etter køyretøytype" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 3 :text "Milliardar kilometer" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))


(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: SSB tabell 07301" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def prop->color
  [[:personbiler-i-alt brown "Personbilar i alt"]
   [:små-godsbiler-i-alt red "Små godsbilar i alt"]
   [:store-lastebiler-i-alt orange "Store lastebilar i alt"]
   [:busser-i-alt green "Bussar i alt"]])

(def legend
  (opentype/stack {:fill         "whitesmoke"
                   :fill-opacity 0.75
                   :margin       8
                   :y            (:plot-height c)
                   :x            5
                   :grow-upwards 10}
                  {:text  "Køyretøytype" :font "Roboto Bold" :font-size 18
                   :right {:text "Mrd. km."}}
                  (for [[prop col txt] (reverse prop->color)]
                    {:rect  {:fill col}
                     :right {:text (str/replace (format "%.1f" (get (last data) prop)) "." ",")}
                     :text  txt :font "Roboto Regular" :font-size 18})))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (bars c {:p :dato :h prop->color} data)
     legend]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "img/ssb-svg/koyrelengde-etter-kategori.svg" "img/ssb-png/koyrelengde-etter-kategori.png" (diagram)))
