(ns clj-simple-chart.scale.linear
  (:require [clojure.spec :as s]))

;;; TODO: This is just "common sense" code --- is there some edge cases?
;;; How does d3 do it?
(defn scale-linear
  [{domain :domain range :range :as config}]
  (let [point-fn (fn [x]
                   (let [domain-size (- (last domain) (first domain))
                         domain-offset (- x (first domain))
                         domain-relative (/ domain-offset domain-size)
                         range-size (- (last range) (first range))
                         range-output (+ (first range) (* domain-relative range-size))]
                     range-output))]
    (merge config {:point-fn point-fn})))
