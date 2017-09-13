(ns clj-simple-chart.area-center-text
  (:require [clj-simple-chart.chart :as chart]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :as translate]
            [clj-simple-chart.opentype :as opentype]))

(defmulti area-center-text (fn [{:keys [x y]} data & args] [(:type x) (:type y)]))

(defn- add-below [scale coll item]
  (let [below-sub-domain (take-while #(not= (:c item) %) (:sub-domain scale))
        below-items (filter #(some #{(:c %)} below-sub-domain) coll)]
    (assoc item :h0 (reduce + 0 (mapv :h below-items)))))

(defn stack [scale coll]
  (->> coll
       (group-by :p)
       (vals)
       (mapv #(mapv (partial add-below scale %) %))
       (flatten)
       (sort-by :p)
       (vec)))

(defmethod area-center-text [:ordinal-linear :linear]
  [{:keys [x y]} coll & [style-cb]]
  (let [xfn (partial point/center-point x)
        yfn (partial point/center-point y)
        stacked (stack x coll)
        text-items (filter :text stacked)
        texts (mapv (fn [{:keys [p c h h0 text]}]
                      [:g (translate/translate-map (xfn p) (yfn (+ (/ h 2) h0)))
                       (let [txt (opentype/text text)
                             woff (- (- (:width (meta txt))) 3)
                             hoff (/ (:height (meta txt)) 2)]
                         [:g (translate/translate-map woff hoff) txt])])
                    text-items)]
    (vec (concat [:g] texts))))
