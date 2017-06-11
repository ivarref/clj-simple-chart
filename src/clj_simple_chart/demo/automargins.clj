(ns clj-simple-chart.demo.automargins
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.chart :as chart]))

(def svg-width 180)
(def svg-height 180)

(defn var1 []
  (let [xx {:type               :linear
            :orientation        :top
            :domain             [0 2]
            :ticks              2
            :axis-text-style-fn (fn [x] {:font-size 16})}
        yy {:type               :ordinal
            :orientation        :both
            :reverse            true
            :domain             ["Ho" "Wd"]
            :axis-text-style-fn (fn [x] {:font-size 36})}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:g {:transform (translate (:margin-left c) (:margin-top c))}
     (render-axis (:x c))
     (render-axis (:y c))]))

(defn var2 []
  (let [xx {:type               :linear
            :orientation        :both
            :domain             [0 100]
            :ticks              2
            :axis-text-style-fn (fn [x] {:font-size 36})}
        yy {:type               :ordinal
            :orientation        :right
            :reverse            true
            :domain             ["A" "B"]
            :axis-text-style-fn (fn [x] {:font-size 10})}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:g {:transform (translate (:margin-left c) (:margin-top c))}
     (render-axis (:x c))
     (render-axis (:y c))]))


(defn var3 []
  (let [xx {:type               :linear
            :orientation        :top
            :domain             [0 100]
            :ticks              2
            :axis-text-style-fn (fn [x] {:font-size 36})}
        yy {:type               :ordinal
            :orientation        :right
            :reverse            true
            :domain             ["A" "B"]
            :axis-text-style-fn (fn [x] {:font-size 10})}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:g {:transform (translate (:margin-left c) (:margin-top c))}
     (render-axis (:x c))
     (render-axis (:y c))]))

(defn var4 []
  (let [xx {:type               :linear
            :orientation        :bottom
            :domain             [0 100]
            :ticks              2
            :axis-text-style-fn (fn [x] {:font-size 36})}
        yy {:type               :ordinal
            :orientation        :right
            :reverse            true
            :domain             ["A" "B"]
            :axis-text-style-fn (fn [x] {:font-size 10})}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:g {:transform (translate (:margin-left c) (:margin-top c))}
     (render-axis (:x c))
     (render-axis (:y c))]))

(defn diagram []
  [:svg (svg-attrs (+ (* 4 (+ 10 svg-width)) 10) (+ 20 svg-height))
   [:rect {:width "100%" :height "100%" :fill-opacity 0.5 :fill "steelblue"}]
   [:g {:transform (translate 10 10)}
    [:rect {:width svg-width :height svg-height :fill "#ffaa00"}]
    (var1)]
   [:g {:transform (translate 200 10)}
    [:rect {:width svg-width :height svg-height :fill "#ffaa00"}]
    (var2)]
   [:g {:transform (translate 390 10)}
    [:rect {:width svg-width :height svg-height :fill "#ffaa00"}]
    (var3)]
   [:g {:transform (translate (+ 190 390) 10)}
    [:rect {:width svg-width :height svg-height :fill "#ffaa00"}]
    (var4)]])

(defn render-self []
  (render "./img/dev/automargins.svg" (diagram)))