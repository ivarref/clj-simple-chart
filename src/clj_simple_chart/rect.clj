(ns clj-simple-chart.rect
  (:require [clj-simple-chart.core :refer :all]))

(defn vertical-rect
  [xscale yscale {px     :x
                  py     :y
                  height :h
                  fill   :fill
                  :as    all
                  :or    {py   (first (domain yscale))
                          fill "red"}}]
  (let [growing-svg-y-axis (apply < (scale-range yscale))]
    (if growing-svg-y-axis
      (let [bottom (first (scale-range yscale))
            h (- (yscale height) bottom)
            yy (yscale py)]
        [:rect {:x      (.doubleValue (xscale px))
                :y      (.doubleValue yy)
                :height (.doubleValue h)
                :fill   fill
                :style  "shape-rendering:crispEdges;"
                :width  (get (meta xscale) :bandwidth)}])
      (let [bottom (first (scale-range yscale))
            h (- bottom (yscale height))
            yy (- (yscale py) h)]
        [:rect {:x      (.doubleValue (xscale px))
                :y      (.doubleValue yy)
                :height (.doubleValue h)
                :fill   fill
                :style  "shape-rendering:crispEdges;"
                :width  (get (meta xscale) :bandwidth)}]))))
