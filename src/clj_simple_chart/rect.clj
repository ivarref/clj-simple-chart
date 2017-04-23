(ns clj-simple-chart.rect
  (:require [clj-simple-chart.point :refer [point]]))

(defn stack-coll [coll]
  (reductions
    (fn [{h :h y :y} new]
      (update new :y #(+ h (or y 0.0) (or % 0.0)))) coll))

(defn stack-sideways [coll]
  (reductions
    (fn [{h :h y :y} new]
      (update new :y #(+ h (or y 0.0) (or % 0.0)))) coll))

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

(defn rect-or-stacked [xscale yscale inp]
  (if (or (list? inp) (vector? inp))
    [:g (map (partial vertical-rect xscale yscale) (stack-coll inp))]
    (vertical-rect xscale yscale inp)))

(defmulti scaled-rect (fn [x y] [(:type x) (:type y)]))

(defmethod scaled-rect [:ordinal :linear]
  [x y]
  (partial rect-or-stacked x y))
