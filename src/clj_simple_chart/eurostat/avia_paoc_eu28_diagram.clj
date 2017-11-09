(ns clj-simple-chart.eurostat.avia-paoc-eu28-diagram
  (:require [clj-simple-chart.eurostat.avia-paoc :as avia-paoc]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clojure.string :as string]
            [clojure.string :as str]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.line :as line]))

(def data avia-paoc/eu28-monthly-12mms)

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def x-domain (map :date data))

(def x-ticks (filter #(.endsWith % "-12") x-domain))

(def available-width (- svg-width (* 2 marg)))

(def header (opentype/stack
              {:width available-width}
              [{:text "Antall Luftpassasjerar, EU28" :font "Roboto Bold" :font-size 30}
               {:text "Antall luftpassasjerar, millionar" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Eurostat" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :domain      x-domain})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 1000]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (line/line c {:h :12mms-mill
                   :dot (fn [x] (str/ends-with? (:date x) "12"))
                   :dot-style {:fill "blue"}
                   :path {:stroke "blue"
                          :stroke-width 3.5}
                   :p :date} data)

     (axis/render-axis (:x c))]


    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/eurostat-svg/eu28-air-passengers.svg" "./img/eurostat-png/eu28-air-passengers.png" (diagram)))

