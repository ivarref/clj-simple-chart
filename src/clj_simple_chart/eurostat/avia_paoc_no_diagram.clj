(ns clj-simple-chart.eurostat.avia-paoc-no-diagram
  (:require [clj-simple-chart.eurostat.avia-paoc :as avia-paoc]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clojure.string :as string]
            [clojure.string :as str]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.line :as line]
            [clj-simple-chart.dateutils :as dateutils]))

(def data avia-paoc/norway-monthly-12mms)

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def x-domain (map :date data))

(def x-ticks (filter #(.endsWith % "-12") x-domain))

(def available-width (- svg-width (* 2 marg)))
(def last-data (last data))

(def fill
  "#d62728"                                                 ; //red
  ;"#ff7f0e", //orange
  ;"#8c564b", //brown
  ;"#1f77b4", //blue
  ;"#e377c2", //pink
  ;"#17becf", //cyan
  ;"#bcbd22", //gusjegul
  ;"#9467bd", //purple
  ;"#7f7f7f", //gray
  ;"#2ca02c", //green
  )

(def yoy-fill "#1f77b4")

(def header (opentype/stack
              {:width available-width}
              [{:text "Antall Luftpassasjerar, Noreg" :font "Roboto Bold" :font-size 30}
               {:text (str "Per " (dateutils/months-str (:date (first (take-last 12 data))))
                           "–" (dateutils/months-str (:date last-data)) ": "
                           (string/replace (format "%.1f" (double (get last-data :12mms-mill))) "." ",")
                           " millionar")
                :font "Roboto Bold" :font-size 16 :margin-top 1 :margin-bottom 10}

               ;{:text "Årleg vekst" :font "Roboto Bold" :font-size 16 :margin-top 10 :fill yoy-fill :margin-bottom 2}
               ;{:text "5 år glidande gjennomsnitt" :font "Roboto Bold" :font-size 16 :fill yoy-fill :margin-bottom 3}
               {:text "Antall luftpassasjerar, millionar" :font "Roboto Bold" :font-size 16 :fill fill :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :margin-top 1 :font "Roboto Bold" :font-size 16 :fill fill :valign :bottom :align :right}]))

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
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold" :fill fill})
         ;:tick-format        (fn [x] (str/replace (format "%.1f" x) "." ","))
         :domain             [0 40]})

(def y2 {:type               :linear
         :orientation        :left
         :axis-text-style-fn (fn [x] {:font "Roboto Bold" :fill yoy-fill})
         :tick-values        [-11 -8.25 -5.5 -2.75 0 2.75 5.5 8.25 11]
         :tick-format        (fn [x] (str/replace (format "%.2f" x) "." ","))
         :domain             [-11 11]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     ;:y2     y2
                     }))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     #_(axis/render-axis (:y2 c))
     #_(line/line (assoc c :y (:y2 c))
                {:h         :yoy
                 :dot       false                           ;(fn [x] (str/ends-with? (:date x) "12"))
                 :dot-style {:fill         yoy-fill
                             :r            4.5
                             :stroke       "black"
                             :stroke-width 2.0}
                 :path      {:stroke       yoy-fill
                             :stroke-width 3.5}
                 :p         :date} data)
     (line/line c {:h         :12mms-mill
                   :dot       (fn [x] (str/ends-with? (:date x) "12"))
                   :dot-style {:fill         fill
                               :r            4.5
                               :stroke       "black"
                               :stroke-width 2.0}
                   :path      {:stroke       fill
                               :stroke-width 3.5}
                   :p         :date} data)
     (axis/render-axis (:x c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/eurostat-svg/no-air-passengers.svg" "./img/eurostat-png/no-air-passengers.png" (diagram)))
