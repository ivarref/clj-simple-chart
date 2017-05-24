(ns clj-simple-chart.demo.betterautomargins
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.chart :as chart]))

(def svg-width 450)
(def svg-height 240)

(def x-domain ["ASDFASDF1" "ASDFASDF2" "ASDFASDF3" "ASDFASDF4"])

(def xx {:type          :ordinal
         :orientation   :bottom
         ;:tick-values   x-ticks
         :domain        x-domain
         :padding-inner 0.1
         :padding-outer 0.2})

(def yy {:type        :linear
         :orientation :right
         :ticks       5
         :domain      [0 100]})

(def c (chart/chart {:width  svg-width
                     :height svg-height
                     :x      xx
                     :y      yy}))


(defn diagram []
  [:svg (svg-attrs svg-width svg-height)
   [:rect {:width "100%" :height "100%" :fill "#ffaa00"}]
    [:g {:transform (translate (:margin-left c) (:margin-top c))}
     (render-axis (:x c))
     (render-axis (:y c))
     ]
   ])

(defn render-self []
  (render "./img/dev/bettermargins.svg" (diagram)))