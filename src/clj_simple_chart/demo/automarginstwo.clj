(ns clj-simple-chart.demo.automarginstwo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.chart :as chart]))

(defn diagram []
  (let [svg-width 250
        svg-height 240
        xx {:type               :linear
            :orientation        :top
            :domain             [0 100]
            :ticks              2
            :axis-text-style-fn (fn [x] {:font-size 16})}
        yy {:type               :ordinal
            :orientation        :both
            :reverse            true
            :domain             ["Hello" "World"]
            :axis-text-style-fn (fn [x] {:font-size 36})}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:svg (svg-attrs (+ 20 svg-width) (+ 20 svg-height))
     [:rect {:width "100%" :height "100%" :fill-opacity 0.5 :fill "steelblue"}]
     [:g {:transform (translate 10 10)}
      [:rect {:width svg-width :height svg-height :fill "#ffaa00"}]
      [:g {:transform (translate (:margin-left c) (:margin-top c))}
       (render-axis (:x c))
       (render-axis (:y c))]]]))

(defn render-self []
  (render "./img/dev/automarginstwo.svg" (diagram)))