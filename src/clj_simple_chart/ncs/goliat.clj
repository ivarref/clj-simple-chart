(ns clj-simple-chart.ncs.goliat
  (:require [clj-simple-chart.ncs.reserve :as reserve]))

(def keep-fields [:fldName :fldRecoverableOil])

(defn keep-fields-fn [row]
  (reduce (fn [o k]
            (if (some #{k} keep-fields)
              o
              (dissoc o k))) row (keys row)))

(defn to-mboe [row]
  (update row :fldRecoverableOil #(Math/round (* 6.29 %))))

(def oil-reserve-data (->> reserve/data-parsed
                           (map keep-fields-fn)
                           (map to-mboe)))

(def goliat (first (filter #(= "GOLIAT" (:fldName %)) oil-reserve-data)))

(def top-ten-plus-goliat (->> oil-reserve-data
                              (sort-by :fldRecoverableOil)
                              (take-last 10)
                              (cons goliat)
                              (sort-by :fldRecoverableOil)
                              (reverse)
                              (vec)))
