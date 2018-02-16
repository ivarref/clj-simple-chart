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
         :tick-values   (map :tid data)
         :domain        (map :tid data)
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
               {:rect {:fill red} :text "Husholdningers gjeld" :font "Roboto Bold" :font-size 16 :margin-top 3}
               {:rect {:fill green} :text "Gjennomsnittlig lønn" :font "Roboto Bold" :font-size 16 :margin-top 3}
               {:text "1999 = 100" :font "Roboto Bold" :font-size 15 :margin-top 3}]))

(def footer (opentype/stack
              {:width available-width :margin-top 6}
              [{:text "Kjelde: SSB (Tabell 11599 og personleg kommunikasjon)" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     (-> yy
                                 (dissoc :grid)
                                 (assoc :orientation :left))}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (axis/render-axis (:y2 c))
     (line c {:p :tid :h [[:gjeldsvekst red] [:lonnsvekst green]]} data)]]])
;;;

(defn render-self []
  (core/render "img/ssb-svg/gjeld-vs-inntekt.svg" "img/ssb-png/gjeld-vs-inntekt.png" (diagram)))
