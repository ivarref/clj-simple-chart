(ns clj-simple-chart.area
  (:require [clj-simple-chart.chart :as chart]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :as translate]))

(defmulti area (fn [{:keys [x y]} data & args] [(:type x) (:type y)]))

(defn- add-below [scale coll item]
  (let [below-sub-domain (take-while #(not= (:c item) %) (:sub-domain scale))
        below-items (filter #(some #{(:c %)} below-sub-domain) coll)]
    (assoc item :h0 (reduce + 0 (mapv :h below-items)))))

(defn- add-is-top [scale coll item]
  (let [below-sub-domain (take-while #(not= (:c item) %) (:sub-domain scale))
        below-items (filter #(some #{(:c %)} below-sub-domain) coll)]
    (assoc item :is-top (= (count below-items) (dec (count coll))))))

(defn- fill-from-scale [scale c]
  (cond
    (and (:sub-domain scale) (:fill scale) (vector? (:fill scale)))
    (get (zipmap (:sub-domain scale) (:fill scale)) c)
    (and (:sub-domain scale) (:fill scale) (map? (:fill scale)))
    (get (:fill scale) c)
    :else nil))

(defn stack [scale coll]
  (->> coll
       (group-by :p)
       (vals)
       (mapv #(mapv (partial add-below scale %) %))
       (flatten)
       (group-by :c)
       (vals)
       (mapv #(sort-by :p %))
       (mapv #(drop-while (fn [x] (= 0 (:h x))) %))
       (flatten)

       (group-by :p)
       (vals)
       (mapv #(mapv (partial add-is-top scale %) %))
       (flatten)
       (group-by :c)
       (vals)
       (mapv #(sort-by :p %))))

(defmethod area [:ordinal-linear :linear]
  [{:keys [x y p h c]} coll & [style-cb]]
  (let [xfn (partial point/center-point x)
        yfn (partial point/center-point y)
        p (or p :p)
        h (or h :h)
        c (or c :c)
        coll (->> coll
                  (flatten)
                  (mapv #(assoc % :p (p %)))
                  (mapv #(assoc % :h (h %)))
                  (mapv #(assoc % :c (c %)))
                  (vec))
        point-str (fn [{:keys [p h h0]}] (str (xfn p) " " (yfn (+ h0 h))))
        point-str-h0 (fn [{:keys [p h0]}] (str (xfn p) " " (yfn h0)))
        stacked (stack x coll)
        lines (mapcat (fn [coll-outer]
                        (mapv (fn [coll2]
                                (when-not (:is-top (first coll2))
                                  [:g
                                   [:path (merge
                                            {:d            (reduce (fn [o v] (str o " L" (point-str v)))
                                                                   (str "M" (point-str (first coll2)))
                                                                   (drop 1 coll2))
                                             :fill         "none"
                                             :stroke       (or (:stroke (first coll2)) "none")
                                             :stroke-width (or (:stroke-width (first coll2)) "1px")}
                                            (cond (fn? style-cb)
                                                  (style-cb (first coll2))
                                                  (map? style-cb) style-cb
                                                  :else {}))]]))
                              (partition-by :is-top coll-outer))) stacked)
        top-line-coll (->> stacked
                           (flatten)
                           (filter :is-top)
                           (sort-by :p))
        top-line (mapv (fn [coll2]
                         [:g
                          [:path (merge
                                   {:d            (reduce (fn [o v] (str o " L" (point-str v)))
                                                          (str "M" (point-str (first coll2)))
                                                          (drop 1 coll2))
                                    :fill         "none"
                                    :stroke       (or (:stroke (first coll2)) "none")
                                    :stroke-width (or (:stroke-width-top (first coll2))
                                                      (:stroke-width (first coll2))
                                                      "1px")}
                                   (cond (fn? style-cb)
                                         (style-cb (first coll2))
                                         (map? style-cb) style-cb
                                         :else {}))]]) [top-line-coll])
        areas (mapv (fn [coll2]
                      [:g
                       [:path (merge
                                {:d            (str (reduce (fn [o v] (str o " L" (point-str v)))
                                                            (str "M" (point-str (first coll2)))
                                                            (drop 1 coll2))
                                                    (reduce (fn [o v] (str o " L" (point-str-h0 v)))
                                                            " "
                                                            (reverse coll2))
                                                    " Z")
                                 :fill         (or (:fill (first coll2)) (fill-from-scale x (:c (first coll2))) "red")
                                 :fill-opacity (or (:fill-opacity (first coll2)) "1.0")
                                 :stroke       "none"}
                                (cond (fn? style-cb)
                                      (style-cb (first coll2))
                                      (map? style-cb) style-cb
                                      :else {}))]])
                    stacked)]
    (vec (concat [:g] areas lines top-line))))
