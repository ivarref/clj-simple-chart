(ns clj-simple-chart.bp.diagrams.world.coaldiagram
  (:require [clj-simple-chart.bp.bpdata2 :as bpdata]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.core :as core]
            [clojure.string :as string]))

(def data (->> bpdata/all-data
               (filter #(= "World" (:country %)))
               (csv/keep-columns [:coal_consumption_mtoe :year])
               (filter #(>= (:year %) 1990))
               (sort-by :year)
               (vec)))

(def max-value (apply max (mapv :coal_consumption_mtoe data)))
(def all-time-high-year (->> data
                             (filter #(= (:coal_consumption_mtoe %) max-value))
                             (first)
                             (:year)))
(def percent-all-time-high (* 100 (/ (:coal_consumption_mtoe (last data)) max-value)))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def x-domain (mapv :year data))

(def available-width (- svg-width (* 2 marg)))

(def x-ticks x-domain)

(def header (opentype/stack
              {:width available-width}
              [{:text "World: Coal consumption" :font "Roboto Bold" :font-size 28}
               {:text "Million tonnes of oil equivalents" :font "Roboto Bold" :font-size 16 :margin-top 2 :margin-bottom 10}

               {:text "Coal consumption" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "Million tonnes of oil equivalents" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 8 :text (str "Source: BP (" bpdata/bp-release-year ")") :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (cond (or (string/ends-with? (str x) "0") (string/ends-with? (str x) "5")) x
                                      :else (subs (str x) 2)))
         :domain        x-domain
         :padding-inner 0.4
         :padding-outer 0.4})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 4000]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")

(def green "rgb(44, 160, 44)")
(def brown "rgb(140, 86, 75)")
(def red "rgb(214, 39, 40)")
(def pink "rgb(227, 119, 194)")
(def gusjegul "rgb(188, 189, 34)")
(def lilla "rgb(148, 103, 189)")
(def cyan "rgb(23, 190, 207)")

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (rect/bars c
                {:p    :year
                 :fill brown
                 :h    :coal_consumption_mtoe}
                data)
     (axis/render-axis (:x c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/bp-svg/world-coal.svg" "./img/bp-png/world-coal.png" (diagram)))
