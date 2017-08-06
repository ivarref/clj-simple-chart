(ns clj-simple-chart.rect
  (:require [clj-simple-chart.scale.core :refer [scale]]
            [clj-simple-chart.point :refer [point]]))

(defn stack-coll [coll]
  (reductions
    (fn [{h :h y :y} new]
      (update new :y #(+ h (or y 0.0) (or % 0.0)))) coll))

(defn stack-horizontal [coll]
  (reductions
    (fn [{h :h x :x} new]
      (update new :x #(+ h (or x 0.0) (or % 0.0)))) coll))

(defn vertical-rect
  [xscale yscale {px     :p
                  py     :y
                  height :h
                  fill   :fill
                  stroke :stroke
                  stroke-width :stroke-width
                  :as    all
                  :or    {py   (first (:domain yscale))
                          fill "red"
                          stroke "none"
                          stroke-width "1px"}}]
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
                :stroke stroke
                :stroke-width stroke-width
                :width  (:bandwidth xscale)}])
      (let [top (first (:range yscale))
            h (- top (point yscale height))
            yy (- (point yscale py) h)]
        [:rect {:x      (point xscale px)
                :y      (double yy)
                :height (double h)
                :fill   fill
                :stroke stroke
                :stroke-width stroke-width
                :style  "shape-rendering:crispEdges;"
                :width  (:bandwidth xscale)}]))))


(defn horizontal-rect
  [xscale yscale {py     :p
                  px     :x
                  height :h
                  fill   :fill
                  stroke :stroke
                  stroke-width :stroke-width
                  :as    all
                  :or    {px   (first (:domain xscale))
                          fill "red"
                          stroke "none"
                          stroke-width "1px"}}]
  (let [svg-natural-order (apply < (:range xscale))]
    (if svg-natural-order
      (let [bottom (first (:range xscale))
            w (- (point xscale height) bottom)]
        [:rect {:x      (point xscale px)
                :y      (point yscale py)
                :height (:bandwidth yscale)
                :fill   fill
                :stroke stroke
                :stroke-width stroke-width
                :style  "shape-rendering:crispEdges;"
                :width  (double w)}])
      (let [top (first (:range xscale))
            h (- top (point xscale height))
            xx (- (point xscale px) h)]
        [:rect {:x      xx
                :y      (point yscale py)
                :height (:bandwidth yscale)
                :fill   fill
                :stroke stroke
                :stroke-width stroke-width
                :style  "shape-rendering:crispEdges;"
                :width  (double h)}]))))

(defn update-fill-color [xscale item]
  (cond
    (and (:sub-domain xscale) (:fill xscale))
    (update item :fill #(or % (get (zipmap (:sub-domain xscale) (:fill xscale)) (:c item))))
    :else item))

(defn sort-by-sub-domain [xscale inp]
  (if-let [sub-domain (:sub-domain xscale)]
    (let [rank (zipmap sub-domain (range 0 (count sub-domain)))
          cmp-fn (fn [a b] (< (get rank (:c a)) (get rank (:c b))))]
      (sort cmp-fn inp))
    inp))

(defn rect-or-stacked-vertical [xscale yscale inp]
  (cond
    (not (or (list? inp) (vector? inp)))
    (recur xscale yscale [inp])
    (not-every? map? inp)
    (recur xscale yscale (vec (flatten inp)))
    (> (count (keys (group-by :p inp))) 1)
    [:g (map (partial rect-or-stacked-vertical xscale yscale) (vals (group-by :p inp)))]
    (and (:sub-domain xscale) (= :sideways (:stack xscale)))
    (let [x (scale (merge {:type          :ordinal
                           :width         (:bandwidth xscale)
                           :height        (:height xscale)
                           :domain        (:sub-domain xscale)
                           :axis          :x
                           :orientation   (:orientation xscale)
                           :padding-inner 0.0}
                          (get xscale :stack-opts {})))]
      [:g
       (map (fn [item]
              [:g {:transform (str "translate(" (point xscale (:p item)) ",0)")}
               (rect-or-stacked-vertical x yscale (update-fill-color xscale (assoc item :p (:c item))))]) inp)])
    :else [:g (map (partial vertical-rect xscale yscale)
                   (map (partial update-fill-color xscale)
                        (stack-coll (sort-by-sub-domain xscale inp))))]))

(defn rect-or-stacked-horizontal [xscale yscale inp]
  (cond
    (not (or (list? inp) (vector? inp)))
    (recur xscale yscale [inp])
    (not-every? map? inp)
    (recur xscale yscale (vec (flatten inp)))
    (> (count (keys (group-by :p inp))) 1)
    [:g (map (partial rect-or-stacked-horizontal xscale yscale) (vals (group-by :p inp)))]
    (and (:sub-domain yscale) (= :sideways (:stack yscale)))
    (let [y (scale (merge {:type          :ordinal
                           :width         (:width yscale)
                           :height        (:bandwidth yscale)
                           :domain        (:sub-domain yscale)
                           :axis          :y
                           :orientation   (:orientation yscale)
                           :padding-inner 0.0}
                          (get yscale :stack-opts {})))]
      [:g
       (map (fn [item]
              [:g {:transform (str "translate(0," (point yscale (:p item)) ")")}
               (rect-or-stacked-horizontal xscale y (update-fill-color yscale (assoc item :p (:c item))))]) inp)])
    :else [:g (map (partial horizontal-rect xscale yscale)
                   (map (partial update-fill-color yscale)
                        (stack-horizontal (sort-by-sub-domain yscale inp))))]))

(defmulti scaled-rect (fn [x y] [(:type x) (:type y)]))

(defmethod scaled-rect [:linear :ordinal]
  [x y]
  (partial rect-or-stacked-horizontal x y))

(defmethod scaled-rect [:ordinal :linear]
  [x y]
  (partial rect-or-stacked-vertical x y))
