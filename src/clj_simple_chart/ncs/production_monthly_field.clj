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
  (map-indexed (fn [idx x] (assoc x :prev-rows (filter #(some #{(:date %)} (:prev-months x)) rows))) rows))

; steps:
; with-cumulative:  group by field name, add cumulative / prev rows
; by-date: group by date, sum all prev rows ...

(def data (->> raw-production/data
               (map #(assoc % :gas-cumulative (:prfPrdGasNetBillSm3 %)))
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               (map #(assoc % :days-in-month (. (YearMonth/of (:prfYear %) (:prfMonth %)) lengthOfMonth)))
               (map #(assoc % :fldRecoverableGas (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableGas)))
               ; remove unused values
               (map #(dissoc % :prfPrdNGLNetMillSm3
                             :prfPrdCondensateNetMillSm3
                             :prfPrdProducedWaterInFieldMillSm3
                             :prfNpdidInformationCarrier
                             :prfPrdOeNetMillSm3
                             :prfPrdOilNetMillSm3))
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
  {:pre  [(coll? production)
          (= 1 (count (distinct (mapv :prfInformationCarrier production))))
          (= (count production)
             (count (distinct (mapv :date production))))]
   :post [(= (count production)
             (count %))]}
  (->> production
       (sort-by :date)
       (reductions (fn [old n] (update n :gas-cumulative (fn [v] (+ v (:gas-cumulative old))))))
       (add-prev-rows-last-n 12)
       (mapv #(assoc % :gas-production-12-months-est (apply + (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))))
       (mapv #(assoc % :prev-prod (mapv double (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))))
       ;(remove #(zero? (:gas-production-12-months-est %)))
       ;(mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :gas-remaining (- (:fldRecoverableGas %) (:gas-cumulative %))))
       (mapv #(assoc % :gas-rp (if (or (neg? (:gas-remaining %)) (zero? (:gas-production-12-months-est %)))
                                 0
                                 (/ (:gas-remaining %) (:gas-production-12-months-est %)))))
       (mapv #(assoc % :bucket (bucket-fn %)))))

(def with-cumulative (mapcat produce-cumulative (vals (group-by :prfInformationCarrier data))))
(def with-cumulative-eoy-2004 (->> with-cumulative
                                   (filter #(= "2004-12" (:date %)))
                                   (mapcat :prev-prod)))

(test/is (= (count (filter #(= 2004 (:prfYear %)) with-cumulative)) (count raw-production/whole-2004)))
(test/is (= (count (filter #(= 2004 (:prfYear %)) data)) (count raw-production/whole-2004)))
(test/is (= (count with-cumulative) (count data)))

(def eoy-2004-fields (->> with-cumulative
                          (filter #(= "2004-12" (:date %)))
                          (mapv :prfInformationCarrier)
                          (distinct)
                          (sort)
                          (vec)))

(def all-2004-fields (->> raw-production/whole-2004
                          (mapv :prfInformationCarrier)
                          (distinct)
                          (sort)
                          (vec)))

(doseq [fld all-2004-fields]
  (when-not (some #{fld} eoy-2004-fields)
    (println "missing field" fld)))

(test/is (= (count eoy-2004-fields) (count all-2004-fields)))

(def empty-buckets (reduce (fn [o n] (assoc o n 0)) {} (distinct (map :bucket with-cumulative))))

(defn process-date
  [production]
  {:pre [(coll? production)]}
  (merge {:date          (:date (first production))
          :days-in-month (:days-in-month (first production))
          :sum           (reduce + 0.0 (mapcat :prev-prod production))}
         (reduce (fn [org [k v]]
                   (assoc org k
                              (->> production
                                   (filter #(= k (:bucket %)))
                                   (mapcat :prev-prod)
                                   (reduce + 0.0)))) {} empty-buckets)))

(def by-date (->> (map process-date (vals (group-by :date with-cumulative)))
                  (sort-by :date)
                  (mapv #(assoc % :prfYear (read-string (first (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :prfMonth (read-string (last (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :eofYear (if (= 12 (:prfMonth %)) (:prfYear %)
                                                                    (dec (:prfYear %)))))))

(def troll (last (filter #(= "TROLL" (:prfInformationCarrier %)) with-cumulative)))

(def year-end-data (->> by-date
                        (filter #(or (.endsWith (:date %) "-12") (= % (last by-date))))
                        (mapv #(assoc % :diff (Math/abs (- (:sum %)
                                                           (raw-production/sum-for-year (:prfYear %) :prfPrdGasNetBillSm3)))))))

(csvmap/write-csv-format
  "./data/ncs/gas-production-rp-bucket-stacked-yearly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum :diff])
   :format  (merge {:sum  "%.3f"
                    :diff "%.3f"}
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
