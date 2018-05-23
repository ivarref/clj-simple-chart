(ns clj-simple-chart.ssb.diagrams.bilbensin
  (:require [clj-simple-chart.ssb.data.sal-av-petroleumsprodukt-11174 :as datasource]
    [clojure.string :as str]
    [clj-simple-chart.translate :refer [translate translate-y]]
    [clj-simple-chart.rect :refer [bars]]
    [clj-simple-chart.chart :as chart]
    [clj-simple-chart.axis.core :as axis]
    [clj-simple-chart.core :as core]
    [clojure.string :as string]
    [clj-simple-chart.opentype :as opentype]))

(def data datasource/data)
(def prop :bilbensin)

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
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (->> (map prop data)
                                     (apply max))]})

(def y2 {:type               :linear
         :orientation        :left
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 100]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Sal av bilbensin" :font "Roboto Bold" :font-size 30}
               {:text "12 månadar glidande sum" :font "Roboto Bold" :font-size 16 :margin-bottom 10}
               {:text "Millionar liter" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6
                :text "Kjelde: SSB tabell 11174" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))
                     ;:y2     y2}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (bars c {:p :dato :h [[prop "green"]]} data)
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))]
     ;(axis/render-axis (:y2 c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "img/ssb-svg/bilbensin.svg" "img/ssb-png/bilbensin.png" (diagram)))
