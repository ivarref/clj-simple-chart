(ns clj-simple-chart.scale.core
  (:require [clojure.spec :as s]
            [clj-simple-chart.scale.ordinal :as ordinal]
            [clj-simple-chart.scale.linear :as linear]
            [clj-simple-chart.scale.ordinal-linear :as ordinal-linear]))

(defn rng [{width  :width
            height :height
            axis   :axis
            rev    :reverse}]
  (let [mv (if (= :x axis) width height)
        v [0.0 (double mv)]
        v (if (= :y axis) (reverse v) v)
        v (if rev (reverse v) v)]
    (vec v)))

(s/def ::type #{:linear :ordinal :ordinal-linear})
(s/def ::axis #{:x :y})
(s/def ::width (s/and number? #(> % 0)))
(s/def ::height (s/and number? #(> % 0)))
(s/def ::reverse boolean?)
(s/def ::domain (s/coll-of any? :min-count 1 :distinct true))

(s/def ::padding-inner (s/and number? #(>= % 0)))
(s/def ::padding-outer (s/and number? #(>= % 0)))
(s/def ::align (s/and number? #(>= 1 % 0)))
(s/def ::round boolean?)

(s/def ::linear-scale (s/keys :req-un [::type ::axis ::width ::height ::domain
                                       ::reverse]))

(s/def ::ordinal-scale (s/keys :req-un [::type ::axis ::width ::height ::domain
                                        ::reverse ::padding-inner ::padding-outer
                                        ::align ::round]))

(s/def ::ordinal-linear-scale (s/keys :req-un [::type ::axis ::width ::height ::domain ::reverse]))

(def linear-defaults {:reverse false})

(def ordinal-defaults {:padding-inner 0.0
                       :padding-outer 0.0
                       :align         0.5
                       :round         false
                       :reverse       false})

(defn make-scale [defaults options spec make-fn]
  (let [opts (merge defaults options)]
    (when (= (s/conform spec opts) ::s/invalid)
      (println (str (s/explain-data spec opts)))
      (throw (ex-info "Invalid scale configuration"
                      (s/explain-data spec opts))))
    (make-fn (merge {:range (rng opts)} opts))))

(defmulti scale :type)

(defmethod scale :ordinal [options]
  (make-scale ordinal-defaults options ::ordinal-scale ordinal/scale-ordinal))

(defmethod scale :linear [options]
  (make-scale linear-defaults options ::linear-scale linear/scale-linear))

(defmethod scale :ordinal-linear [options]
  (make-scale {:reverse false} options ::ordinal-linear-scale ordinal-linear/scale-ordinal-linear))
