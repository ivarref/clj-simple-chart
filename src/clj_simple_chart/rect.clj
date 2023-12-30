(ns clj-simple-chart.rect
  (:require [clj-simple-chart.scale.core :refer [scale]]
            [clj-simple-chart.point :refer [point]]
            [clj-simple-chart.roughjs :as rough]))

(defn stack-coll-inner [c]
  (if (empty? c)
    []
    (vec (reductions
           (fn [{h :h y :y :as init} new]
             (update new :y #(+ h (or y 0.0) (or % 0.0))))
           c))))

(defn stack-coll [coll]
  (let [chunks (group-by (comp neg? :h) coll)]
    (vec (concat
           (stack-coll-inner (get chunks false []))
           (stack-coll-inner (get chunks true []))))))

(comment
  (vec (stack-coll [{:p :bottom :h 10 :y 0}])))

(defn stack-horizontal [coll]
  (reductions
    (fn [{h :h x :x} new]
      (update new :x #(+ h (or x 0.0) (or % 0.0)))) coll))

(defn vertical-rect
  [{:keys [rough] :as xscale}
   yscale
   {px           :p
    py           :y
    height       :h
    fill         :fill
    stroke       :stroke
    stroke-width :stroke-width
    :as          inp
    :or          {py           (max 0 (first (:domain yscale)))
                  fill         "red"
                  stroke       "none"
                  stroke-width "1px"}}]
  (let [svg-natural-order (apply < (:range yscale))]
    (if svg-natural-order
      (let [bottom (first (:range yscale))
            h (- (point yscale height) bottom)
            yy (point yscale py)]
        (do
          (rough/rect {:x            (point xscale px)
                       :y            (double yy)
                       :height       (double h)
                       :fill         fill
                       :rough        rough
                       :style        "shape-rendering:crispEdges;"
                       :stroke       stroke
                       :stroke-width stroke-width
                       :width        (:bandwidth xscale)})))
      (do
        (if (neg? height)
          (do
            [:g
             (rough/rect {:x            (point xscale px)
                          :y            (double (point yscale py))
                          :height       (double (- (point yscale (+ py height))
                                                   (point yscale py)))
                          :fill         fill
                          :stroke       stroke
                          :stroke-width stroke-width
                          :style        "shape-rendering:crispEdges;"
                          :rough        rough
                          :width        (:bandwidth xscale)})])
          (let [top (point yscale 0)
                h (- top (point yscale height))
                yy (- (point yscale py) h)]
            [:g
             ;[:circle {:r 10 :cy h :fill "black"}]
             ;[:circle {:r 10 :cy top :fill "yellow"}]
             (rough/rect {:x            (point xscale px)
                          :y            (double yy)
                          :height       (double h)
                          :fill         fill
                          :stroke       stroke
                          :stroke-width stroke-width
                          :rough        rough
                          :style        "shape-rendering:crispEdges;"
                          :width        (:bandwidth xscale)})]))))))

(defn horizontal-rect
  [xscale yscale {py           :p
                  px           :x
                  height       :h
                  fill         :fill
                  stroke       :stroke
                  stroke-width :stroke-width
                  :as          all
                  :or          {px           (first (:domain xscale))
                                fill         "red"
                                stroke       "none"
                                stroke-width "1px"}}]
  (let [svg-natural-order (apply < (:range xscale))]
    (if svg-natural-order
      (let [bottom (first (:range xscale))
            w (- (point xscale height) bottom)]
        [:rect {:x            (point xscale px)
                :y            (point yscale py)
                :height       (:bandwidth yscale)
                :fill         fill
                :stroke       stroke
                :stroke-width stroke-width
                :style        "shape-rendering:crispEdges;"
                :width        (double w)}])
      (let [top (first (:range xscale))
            h (- top (point xscale height))
            xx (- (point xscale px) h)]
        [:rect {:x            xx
                :y            (point yscale py)
                :height       (:bandwidth yscale)
                :fill         fill
                :stroke       stroke
                :stroke-width stroke-width
                :style        "shape-rendering:crispEdges;"
                :width        (double h)}]))))

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

(defn scaled-rect-2 [x y]
  (case [(:type x) (:type y)]
    [:linear :ordinal]
    (partial rect-or-stacked-horizontal x y)

    [:ordinal :linear]
    (fn [inp] (rect-or-stacked-vertical x y inp))))

(defmulti scaled-rect (fn [x y] [(:type x) (:type y)]))

(defmethod scaled-rect [:linear :ordinal]
  [x y]
  (partial rect-or-stacked-horizontal x y))

(defmethod scaled-rect [:ordinal :linear]
  [x y]
  (partial rect-or-stacked-vertical x y))

(defn- fill-fn [fill]
  (cond (string? fill)
        (fn [_] fill)

        (fn? fill)
        fill

        (map? fill)
        (fn [x] (get fill (:c x) "yellow"))

        :else nil))

(defn- h-fn [h item]
  (cond (nil? h)
        [(assoc item :h (:h item))]

        (keyword? h)
        [(assoc item :h (h item))]

        (fn? h)
        [(assoc item :h (h item))]

        (and (vector? h) (every? vector? h))                ; so h is like [[:property :fill]]
        (mapv #(assoc item :h (let [property (first %)
                                    v (property item)]
                                (if (number? v) v
                                                (do (println "Could not find property" property)
                                                    (println "available keys:" (vec (keys item)))
                                                    (throw (ex-info "Could not find property" {:property property})))))
                           :fill (second %)
                           :c (first %)) h)

        :else (throw (ex-info "unhandled state" {:h h :item item}))))

(defn bars [c {:keys [p h fill] :as config} data]
  (let [pp (or p :p)
        fill (fill-fn fill)
        processed-data (->> data
                            (flatten)
                            (mapv #(assoc % :p (pp %)))
                            (mapv (partial h-fn h))
                            (flatten)
                            (mapv #(if fill (assoc % :fill (fill %)) %))
                            (vec))]
    ((scaled-rect (:x c) (:y c)) processed-data)))
