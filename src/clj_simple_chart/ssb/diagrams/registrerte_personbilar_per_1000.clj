(ns clj-simple-chart.ssb.diagrams.registrerte-personbilar-per-1000
  (:require [clj-simple-chart.ssb.data.elbil :as datasource]
    [clojure.string :as str]
    [clj-simple-chart.translate :refer [translate translate-y]]
    [clj-simple-chart.rect :refer [bars]]
    [clj-simple-chart.chart :as chart]
    [clj-simple-chart.axis.core :as axis]
    [clj-simple-chart.core :as core]
    [clj-simple-chart.colors :refer :all]
    [clojure.string :as string]
    [clj-simple-chart.opentype :as opentype]))

(def data datasource/personbilar-per-1000-innbyggjar)

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (filter #(str/ends-with? % "-12") (map :dato data))
         :tick-format   (fn [x] (str/replace x "-12" ""))
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
              [{:text "Antall personbilar" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 10 :text "Per 1000 innbyggjarar og etter drivstofftype" :font "Roboto Regular" :font-size 18}
               {:margin-bottom 3 :text "Antall køyretøy" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))


(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: SSB tabell 07849 og 05231" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def prop->color
  [[:bensin brown "Bensin"]
   [:diesel red "Diesel"]
   [:annet orange "Andre"]
   [:elektrisk green "Elektrisk"]])

(def legend
  (opentype/stack {:fill         "whitesmoke"
                   :fill-opacity 0.95
                   :margin       8
                   :y            (:plot-height c)
                   :x            15
                   :grow-upwards 15}
                  {:text "Drivstofftype" :font "Roboto Bold" :font-size 18
                   :right {:text "Antall, 2017"}}
                  (for [[prop col txt] (reverse prop->color)]
                    {:rect {:fill col}
                     :right {:text (str (Math/round (double (get (last data) prop))))}
                     :text txt :font "Roboto Regular" :font-size 18})
                  {:text "Totalt"
                   :right {:text (str (Math/round (double (:sum (last data)))))}
                   :font "Roboto Regular" :font-size 18}))

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
  (core/render "img/ssb-svg/registrerte-personbilar-per-1000.svg" "img/ssb-png/registrerte-personbilar-per-1000.png" (diagram)))
