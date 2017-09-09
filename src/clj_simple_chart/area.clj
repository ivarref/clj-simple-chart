(ns clj-simple-chart.area
  (:require [clj-simple-chart.chart :as chart]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :as translate]))

(defmulti area (fn [{:keys [x y]} data & args] [(:type x) (:type y)]))

(defn- add-below [scale coll item]
  (let [below-sub-domain (take-while #(not= (:c item) %) (:sub-domain scale))
        below-items (filter #(some #{(:c %)} below-sub-domain) coll)]
    (assoc item :h0 (reduce + 0 (mapv :h below-items)))))

(defn- fill-from-scale [scale c]
  (cond
    (and (:sub-domain scale) (:fill scale) (vector? (:fill scale)))
    (get (zipmap (:sub-domain scale) (:fill scale)) c)
    (and (:sub-domain scale) (:fill scale) (map? (:fill scale)))
    (get (:fill scale) c)
    :else nil))

(defn stack [scale coll]
  (->> coll (group-by :p)
       (vals)
       (mapv #(mapv (partial add-below scale %) %))
       (flatten)
       (group-by :c)
       (vals)
       (mapv #(sort-by :p %))))

(defmethod area [:ordinal-linear :linear]
  [{:keys [x y]} coll & [style-cb]]
  (let [xfn (partial point/center-point x)
        yfn (partial point/center-point y)
        point-str (fn [{:keys [p h h0]}] (str (xfn p) " " (yfn (+ h0 h))))
        point-str-h0 (fn [{:keys [p h0]}] (str (xfn p) " " (yfn h0)))
        stacked (stack x coll)
        areas (mapv (fn [coll2]
                      [:path (merge
                               {:d            (str (reduce (fn [o v] (str o " L" (point-str v)))
                                                           (str "M" (point-str (first coll2)))
                                                           (drop 1 coll2))
                                                   (reduce (fn [o v] (str o " L" (point-str-h0 v)))
                                                           " "
                                                           (reverse coll2))
                                                   " Z")
                                :fill         (or (:fill (first coll2)) (fill-from-scale x (:c (first coll2))) "red")
                                :stroke       "none"
                                :stroke-width "1px"}
                               (cond (fn? style-cb)
                                     (style-cb (first coll2))
                                     (map? style-cb) style-cb
                                     :else {}))])
                    stacked)]
    (vec (concat [:g] areas))))
