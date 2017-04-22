(ns clj-simple-chart.rect
  (:require [clj-simple-chart.point :refer [point]]))

(defn vertical-rect
  [xscale yscale {px     :x
                  py     :y
                  height :h
                  fill   :fill
                  :as    all
                  :or    {py   (first (:domain yscale))
                          fill "red"}}]
  (let [svg-natural-order (apply < (:range yscale))]
    (if svg-natural-order
      (let [bottom (first (:range yscale))
            h (- (point yscale height) bottom)
            yy (point yscale py)]
        [:rect {:x      (point xscale px)
                :y      (double yy)
                :height (double h)
                :fill   fill
                :style  "shape-rendering:crispEdges;"
                :width  (:bandwidth xscale)}])
      (let [top (first (:range yscale))
            h (- top (point yscale height))
            yy (- (point yscale py) h)]
        [:rect {:x      (point xscale px)
                :y      (double yy)
                :height (double h)
                :fill   fill
                :style  "shape-rendering:crispEdges;"
                :width  (:bandwidth xscale)}]))))
