(ns clj-simple-chart.scale.scale
  (:require [clojure.spec :as s]))

(s/def ::type #{:linear :ordinal})
(s/def ::axis #{:x :y})
(s/def ::width (s/and number? #(> % 0)))
(s/def ::height (s/and number? #(> % 0)))
(s/def ::domain (s/coll-of any? :min-count 1 :distinct true))
(s/def ::padding-inner (s/and number? #(>= % 0)))           ;;; TODO
(s/def ::padding-outer (s/and number? #(>= % 0)))           ;;; TODO
(s/def ::align (s/and number? #(>= 1 % 0)))
(s/def ::round boolean?)
(s/def ::reverse boolean?)

(s/def ::ordinal-scale (s/keys :req-un [::type ::axis ::width ::height ::domain]
                               :opt-un [::padding-inner ::padding-outer
                                        ::align ::round ::reverse]))

(def ordinal-defaults {:padding-inner 0.0
                       :padding-outer 0.0
                       :align         0.0
                       :round         false
                       :reverse       false})

(defn rng [{width  :width
            height :height
            axis   :axis
            rev    :reverse}]
  (let [mv (if (= :x axis) width height)
        v [0.0 mv]
        v (if (= :y axis) (reverse v) v)
        v (if rev (reverse v) v)]
    (vec v)))

(defmulti scale :type)

(defn scale-ordinal
  [{domain        :domain
    rng           :range
    padding-inner :padding-inner
    padding-outer :padding-outer
    align         :align
    round         :round
    :as           config}]
  (let [start (apply min rng)
        reverse-values (apply > rng)
        stop (apply max rng)
        n (count domain)
        step (/ (- stop start)
                (max 1 (+ (* 2 padding-outer)
                          (- n padding-inner))))
        step (if round (Math/floor step) step)
        start (+ start
                 (* align
                    (- stop start (* step (- n padding-inner)))))
        bandwidth (.doubleValue (* step (- 1 padding-inner)))
        start (if round (Math/round start) start)
        bandwidth (if round (Math/round bandwidth) bandwidth)
        values (mapv (fn [i] (+ start (* i step))) (range 0 n))
        values (if reverse-values (reverse values) values)
        mapp (zipmap (map #(.doubleValue %) domain) values)]
    (merge config
           {:bandwidth bandwidth
            :point-fn  (fn [x]
                         (let [v (get mapp (.doubleValue x) ::none)]
                           (if (not= v ::none)
                             v
                             (throw (ex-info (str "Input value >" x "< for scale band is not in scale's domain")
                                             {:value (.doubleValue x)})))))})))

(defmethod scale :ordinal
  [options]
  (let [opts (merge ordinal-defaults options)]
    (when (= (s/conform ::ordinal-scale opts) ::s/invalid)
      (println (str (s/explain-data ::ordinal-scale opts)))
      (throw (ex-info "Invalid ordinal scale configuration"
                      (s/explain-data ::ordinal-scale opts))))
    (scale-ordinal (merge {:range (rng opts)} opts))))
