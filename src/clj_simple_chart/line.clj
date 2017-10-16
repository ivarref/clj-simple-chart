(ns clj-simple-chart.line
  (:require [clj-simple-chart.chart :as chart]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :as translate]
            [clojure.string :as string]))

(defmulti line (fn [{:keys [x y]} data] [(:type x) (:type y)]))

(defmethod line [:ordinal :linear]
  [{:keys [x y p h path dot dot-style]} coll]
  (let [xfn (partial point/center-point x)
        yfn (partial point/center-point y)
        p (or p :p)
        h (or h :h)
        coll (->> coll
                  (flatten)
                  (map-indexed (fn [idx x] (assoc x :idx idx)))
                  (mapv #(assoc % :p (p %)))
                  (mapv #(assoc % :h (h %)))
                  (vec))
        dots (filter dot coll)
        svg-dots (mapv (fn [{:keys [idx p h]}]
                         [:g (translate/translate-map (xfn p) (yfn h))
                          [:circle (merge {:r    5
                                           :fill "red"}
                                          dot-style)]]) dots)
        svg-path-ops (mapv (fn [{:keys [idx p h]}]
                             (let [char-op (if (= 0 idx) "M" "L")]
                               (str char-op " " (xfn p) " " (yfn h)))) coll)]
    [:g
     [:path (merge {:stroke-width 5
                    :stroke       "red"
                    :fill         "none"
                    :d            (string/join " " svg-path-ops)}
                   path)]
     (vec (cons :g svg-dots))]))
