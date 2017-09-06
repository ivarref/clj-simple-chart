(ns clj-simple-chart.dots
  (:require [clj-simple-chart.chart :as chart]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :as translate]))

(defmulti dots (fn [{:keys [x y]} data] [(:type x) (:type y)]))

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

(defmethod dots [:ordinal-linear :linear]
  [{:keys [x y]} coll]
  (let [xfn (partial point/center-point x)
        yfn (partial point/center-point y)
        stacked (->> coll (group-by :p)
                     (vals)
                     (mapv #(mapv (partial add-below x %) %)) ; hm, looks kind of tricky...
                     (flatten)
                     (vec))
        svg-dots (mapv (fn [{:keys [p c h h0 fill]}]
                         [:g (translate/translate-map (xfn p) (yfn (+ h h0)))
                          [:circle {:r    10
                                    :fill (or fill (fill-from-scale x c) "red")}]]) stacked)]
    (vec (cons :g svg-dots))))
