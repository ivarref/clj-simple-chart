(ns clj-simple-chart.demo.betterautomargins
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.chart :as chart]))

(defn diagram []
  (let [svg-width 250
        svg-height 240
        x-domain ["ASDFASDF1" "ASDFASDF2" "ASDFASDF3" "ASDFASDF4ASDFASDF4"]
        xx {:type               :ordinal
            :orientation        :top
            :domain             x-domain
            :axis-text-style-fn (fn [x] {:font-size 16})
            :tick-values        [(first x-domain) #_(last x-domain)]}
        yy {:type               :linear
            :orientation        :right
            :ticks              5
            :axis-text-style-fn (fn [x] {:font-size 36})
            :domain             [44 80]}
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

(def _render-self (render "./img/dev/bettermargins.svg" (diagram)))
