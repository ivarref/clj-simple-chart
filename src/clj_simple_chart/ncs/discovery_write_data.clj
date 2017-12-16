(ns clj-simple-chart.ncs.discovery-write-data
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ncs.discovery :refer [parsed flat-data]]))

(csv/write-csv-format "data/ncs/discovery/per-discovery-mboe.csv"
                      {:columns [:name :year :status :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %)))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(csv/write-csv-format "data/ncs/discovery/pdo-approved-mboe.csv"
                      {:columns [:name :year :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (filter #(= :pdo-approved (:status %)))
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %))
                                     (sort-by :fldRecoverableLiquids)
                                     (reverse)
                                     (vec))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(csv/write-csv-format "data/ncs/discovery/decided-for-production-mboe.csv"
                      {:columns [:name :year :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (filter #(= :decided-for-production (:status %)))
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %))
                                     (sort-by :fldRecoverableLiquids)
                                     (reverse)
                                     (vec))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(csv/write-csv-format "data/ncs/discovery/clarification-mboe.csv"
                      {:columns [:name :year :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (filter #(= :clarification (:status %)))
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %))
                                     (sort-by :fldRecoverableLiquids)
                                     (reverse)
                                     (vec))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(csv/write-csv-format "data/ncs/discovery/likely-mboe.csv"
                      {:columns [:name :year :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (filter #(= :likely (:status %)))
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %))
                                     (sort-by :fldRecoverableLiquids)
                                     (reverse)
                                     (vec))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(csv/write-csv-format "data/ncs/discovery/not-evaluated-mboe.csv"
                      {:columns [:name :year :fldRecoverableLiquids :fldRecoverableGas]
                       :data    (->> parsed
                                     (filter #(= :not-evaluated (:status %)))
                                     (map #(reduce (fn [o [k v]]
                                                     (if (some #{k} [:fldRecoverableLiquids :fldRecoverableGas])
                                                       (assoc o k (* 6.29 v))
                                                       (assoc o k v)))
                                                   {}
                                                   %))
                                     (sort-by :fldRecoverableLiquids)
                                     (reverse)
                                     (vec))
                       :format  {:fldRecoverableLiquids "%.1f"
                                 :fldRecoverableGas     "%.1f"}})

(def number-columns [:shut-down-produced :producing-produced
                     :remaining-reserves :pdo-approved :decided-for-production :clarification
                     :likely :not-evaluated])

(csv/write-csv-format "data/ncs/discovery/produced-reserves-liquids-gb.csv"
                      {:columns [:year :shut-down-produced :producing-produced
                                 :remaining-reserves :pdo-approved :decided-for-production :clarification
                                 :likely :not-evaluated]
                       :data (->> flat-data
                                  (map #(reduce (fn [o [k v]]
                                                  (if (some #{k} number-columns)
                                                    (assoc o k (double (/ (* 6.29 v) 1000)))
                                                    (assoc o k v)))
                                                {}
                                                %))
                                  (sort-by :year)
                                  (reverse))
                       :format (zipmap number-columns (repeat "%.1f"))})
