(ns clj-simple-chart.demo.betterautomargins
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.chart :as chart]))

(defn diagram []
  (let [svg-width 250
        svg-height 240
        x-domain ["ASDFASDF1" "ASDFASDF2" "ASDFASDF3" "ASDFASDF4ASDFASDF4"]
        xx {:type          :ordinal
            :orientation   :bottom
            :domain        x-domain
            :tick-values  [(first x-domain) (last x-domain)]}
        yy {:type        :linear
            :orientation :right
            :ticks       5
            :domain      [0 100]}
        c (chart/chart {:width  svg-width
                        :height svg-height
                        :x      xx
                        :y      yy})]
    [:svg (svg-attrs svg-width svg-height)
     [:rect {:width "100%" :height "100%" :fill "#ffaa00"}]
     [:g {:transform (translate (:margin-left c) (:margin-top c))}
      (render-axis (:x c))
      (render-axis (:y c))]]))

(defn render-self []
  (render "./img/dev/bettermargins.svg" (diagram)))