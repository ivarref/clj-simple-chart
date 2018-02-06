(ns clj-simple-chart.ssb.diagrams.gjeld-vs-inntekt
  (:require [clj-simple-chart.ssb.data.gjeld :as datasource]
            [clojure.string :as str]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.line :refer [line]]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clojure.string :as string]
            [clj-simple-chart.opentype :as opentype]))

(def data datasource/cumulative)

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal-linear
         :orientation   :bottom
         :tick-values   (map :Tid data)
         :domain        (map :Tid data)
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 500]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Utvikling i gjeld og inntekt" :font "Roboto Bold" :font-size 30}
               {:text "1990 = 100" :font "Roboto Bold" :font-size 16}
               {:text "Prosent" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (line c {:p :Tid :h [[:gjeldsvekst red] [:lonnsvekst green]]} data)]]])

(defn render-self []
  (core/render "tmp.svg" "tmp.png" (diagram)))
