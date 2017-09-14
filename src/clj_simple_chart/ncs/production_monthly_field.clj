(ns clj-simple-chart.ncs.production-monthly-field
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.ncs.raw-production :as raw-production]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.string :as str])
  (:import (java.time YearMonth)))

(defn add-prev-rows-last-n [n rows]
  (map-indexed (fn [idx x] (assoc x :prev-rows (take-last n (take (inc idx) rows)))) rows))

(def data (->> raw-production/data
               ;(remove #(= "33/9-6 DELTA" (:prfInformationCarrier %)))
               ;(remove #(= "SINDRE" (:prfInformationCarrier %)))
               ; bootstrap cumulative values
               (map #(assoc % :gas-cumulative (:prfPrdGasNetBillSm3 %)))
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               (map #(assoc % :days-in-month (. (YearMonth/of (:prfYear %) (:prfMonth %)) lengthOfMonth)))
               (map #(assoc % :fldRecoverableGas (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableGas)))
               (filter #(pos? (:fldRecoverableGas %)))
               ; remove unused values
               (map #(dissoc % :prfPrdNGLNetMillSm3
                             :prfPrdCondensateNetMillSm3
                             :prfPrdProducedWaterInFieldMillSm3
                             :prfNpdidInformationCarrier
                             :prfPrdOeNetMillSm3
                             :prfPrdOilNetMillSm3
                             :prfMonth
                             :prfYear))
               (sort-by :date)
               (vec)))

(defn bucket-fn [{:keys [prfInformationCarrier gas-rp]}]
  (cond
     (= "TROLL" prfInformationCarrier) "5- TROLL"
     (< gas-rp 5) "1- 0 - 5"
     (< gas-rp 10) "2- 5 - 10"
     (< gas-rp 20) "3- 10 - 20"
     :else "4- ≥ 20"))

(defn produce-cumulative
  [production]
  {:pre [(coll? production)
         (= 1 (count (distinct (mapv :prfInformationCarrier production))))]}
  (->> (sort-by :date production)
       (reductions (fn [old n] (update n :gas-cumulative (fn [v] (+ v (:gas-cumulative old))))))
       (add-prev-rows-last-n 12)
       (mapv #(assoc % :gas-production-12-months-est (* (apply + (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))
                                                        (/ 12 (count (:prev-rows %))))))
       (remove #(zero? (:gas-production-12-months-est %)))
       (mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :gas-remaining (- (:fldRecoverableGas %) (:gas-cumulative %))))
       (mapv #(assoc % :percentage-produced (* 100 (/ (:gas-cumulative %) (:fldRecoverableGas %)))))
       (mapv #(assoc % :gas-rp (if (neg? (:gas-remaining %))
                                 0
                                 (/ (:gas-remaining %) (:gas-production-12-months-est %)))))
       (mapv #(assoc % :bucket (bucket-fn %)))))

(def with-cumulative (mapcat produce-cumulative (vals (group-by :prfInformationCarrier data))))

(def empty-buckets (reduce (fn [o n] (assoc o n 0)) {} (distinct (map :bucket with-cumulative))))

(defn process-date
  [production]
  {:pre [(coll? production)]}
  (merge {:date          (:date (first production))
          :days-in-month (:days-in-month (first production))
          :sum           (reduce + 0 (mapv :gas-production-12-months-est production))}
         (reduce (fn [org [k v]]
                   (assoc org k
                              (->> production
                                   (filter #(= k (:bucket %)))
                                   (mapv :gas-production-12-months-est)
                                   (reduce + 0)))) {} empty-buckets)))

(def by-date (->> (map process-date (vals (group-by :date with-cumulative)))
                  (sort-by :date)
                  (mapv #(assoc % :prfYear (read-string (first (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :prfMonth (read-string (last (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :eofYear (if (= 12 (:prfMonth %)) (:prfYear %)
                                                                    (dec (:prfYear %)))))))

(def troll (last (filter #(= "TROLL" (:prfInformationCarrier %)) with-cumulative)))

(def year-end-data (filter #(or (.endsWith (:date %) "-12")
                                (= % (last by-date))) by-date))

(csvmap/write-csv-format
  "./data/ncs/gas-production-rp-bucket-stacked-yearly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum])
   :format  (merge {:sum "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.1f"]) empty-buckets)))
   :data    year-end-data})

(csvmap/write-csv-format
  "./data/ncs/gas-production-rp-bucket-stacked-monthly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum])
   :format  (merge {:sum "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.1f"]) empty-buckets)))
   :data    by-date})

(def field-names (->> data
                      (mapv :prfInformationCarrier)
                      (distinct)
                      (sort)
                      (vec)))

(test/is (not (some #{"SINDRE"} field-names)))

(test/is (some #{"ÅSGARD"} field-names))
(test/is (some #{"ØST FRIGG"} field-names))
(test/is (some #{"STATFJORD"} field-names))
(test/is (some #{"EKOFISK"} field-names))
(test/is (some #{"TROLL"} field-names))
(test/is (some #{"GINA KROG"} field-names))
(test/is (some #{"TYRIHANS"} field-names))
(test/is (some #{"REV"} field-names))
(test/is (some #{"GULLFAKS"} field-names))
