(ns clj-simple-chart.ncs.production-monthly-field
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.csv.csvmap :as csvmap])
  (:import (java.time YearMonth)))

(def field-monthly-production-url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_production_monthly&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=80.213.237.130&CultureCode=en")
(defonce raw-data (-> field-monthly-production-url
                      (client/get)
                      (:body)
                      (csv/csv-map)))

(test/is (= (:columns raw-data) [:prfInformationCarrier :prfYear :prfMonth :prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3
                                 :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3 :prfPrdOeNetMillSm3
                                 :prfPrdProducedWaterInFieldMillSm3 :prfNpdidInformationCarrier]))

(def numeric-columns [:prfYear :prfMonth :prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3 :prfPrdNGLNetMillSm3
                      :prfPrdCondensateNetMillSm3 :prfPrdOeNetMillSm3 :prfPrdProducedWaterInFieldMillSm3])

(def data (->> raw-data
               :data
               (remove #(= "33/9-6 DELTA" (:prfInformationCarrier %)))
               (remove #(= "SINDRE" (:prfInformationCarrier %)))
               (csv/read-string-columns numeric-columns)
               (csv/number-or-nil-columns numeric-columns)
               ; bootstrap cumulative values
               (map #(assoc % :oil-cumulative (:prfPrdOilNetMillSm3 %)))
               (map #(assoc % :gas-cumulative (:prfPrdGasNetBillSm3 %)))
               (map #(assoc % :oe-cumulative (:prfPrdOeNetMillSm3 %)))
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               (map #(assoc % :days-in-month (. (YearMonth/of (:prfYear %) (:prfMonth %)) lengthOfMonth)))
               (map #(assoc % :fldRecoverableOil (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableOil)))
               (map #(assoc % :fldRecoverableGas (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableGas)))
               (map #(assoc % :fldRecoverableOE (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableOE)))
               (filter #(pos? (:fldRecoverableGas %)))
               ; remove unused values
               (map #(dissoc % :prfPrdNGLNetMillSm3))
               (map #(dissoc % :prfPrdCondensateNetMillSm3))
               (map #(dissoc % :prfPrdProducedWaterInFieldMillSm3))
               (map #(dissoc % :prfNpdidInformationCarrier))
               (sort-by :date)
               (vec)))

(defn add-prev-rows-last-n [n rows]
  (map-indexed (fn [idx x] (assoc x :prev-rows (take-last n (take (inc idx) rows)))) rows))

(def bucket-fn
  #(cond
     (= "TROLL" (:prfInformationCarrier %)) "5- TROLL"
     (< (:gas-rp %) 5) "1- 0 - 5 R/P"
     (< (:gas-rp %) 10) "2- 5 - 10 R/P"
     (< (:gas-rp %) 15) "3- 10 - 15 R/P"
     :else "4- >= 15 R/P"))

(defn produce-cumulative
  [production]
  {:pre [(coll? production)]}
  (->> (sort-by :date production)
       ;(reductions (fn [old n] (assoc n :start-production (:start-production old))))
       (reductions (fn [old n] (update n :oil-cumulative (fn [v] (+ v (:oil-cumulative old))))))
       (reductions (fn [old n] (update n :gas-cumulative (fn [v] (+ v (:gas-cumulative old))))))
       (reductions (fn [old n] (update n :oe-cumulative (fn [v] (+ v (:oe-cumulative old))))))
       (add-prev-rows-last-n 12)
       (mapv #(assoc % :gas-production-12-months-est (* (apply + (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))
                                                        (/ 12 (count (:prev-rows %))))))
       (remove #(zero? (:gas-production-12-months-est %)))
       (mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :gas-remaining (- (:fldRecoverableGas %) (:gas-cumulative %))))
       (mapv #(assoc % :gas-rp (if (neg? (:gas-remaining %))
                                 0
                                 (/ (:gas-remaining %) (:gas-production-12-months-est %)))))
       (mapv #(assoc % :bucket (bucket-fn %)))))

(def with-cumulative (mapcat produce-cumulative (vals (group-by :prfInformationCarrier data))))

(def empty-buckets (reduce (fn [o n] (assoc o n 0.0)) {} (distinct (map :bucket with-cumulative))))

(defn process-date
  [production]
  {:pre [(coll? production)]}
  (merge {:date          (:date (first production))
          :days-in-month (:days-in-month (first production))
          :sum           (reduce + 0.0 (mapv :gas-production-12-months-est production))}
         (reduce (fn [org [k v]]
                   (assoc org k
                              (->> production
                                   (filter #(= k (:bucket %)))
                                   (mapv :gas-production-12-months-est)
                                   (reduce + 0.0)))) {} empty-buckets)))

(def by-date (->> (map process-date (vals (group-by :date with-cumulative)))
                  (sort-by :date)))

(csvmap/write-csv-format
  "./data/ncs/gas-production-rp-bucket-stacked-yearly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum])
   :format  (merge {:sum "%.2f"}
                   (into {} (mapv (fn [[k v]] [k "%.1f"]) empty-buckets)))
   :data    (filter #(or (.endsWith (:date %) "-12")
                         (= % (last by-date))) by-date)})

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
