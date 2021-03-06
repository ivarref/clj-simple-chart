(ns clj-simple-chart.ncs.liquidprodpp.liquidppdata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.ncs.raw-production :as raw-production]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.string :as str]
            [clj-simple-chart.ncs.raw-production :as production]
            [clojure.edn :as edn])
  (:import (java.time YearMonth)))

(def data (->> raw-production/data
               ; bootstrap cumulative values
               (map #(assoc % :cumulative (:prfPrdLiquidsNetMillSm3 %)))
               (map #(assoc % :production (:prfPrdLiquidsNetMillSm3 %)))
               (map #(assoc % :recoverable (reserve/get-reserve (:prfInformationCarrier %) :fldRecoverableLiquids)))
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               ;(filter #(pos? (:recoverable %)))
               ; remove unused values
               (map #(dissoc % :prfPrdNGLNetMillSm3
                             :prfPrdCondensateNetMillSm3
                             :prfPrdProducedWaterInFieldMillSm3
                             :prfNpdidInformationCarrier
                             :prfPrdOeNetMillSm3
                             :prfPrdGasNetBillSm3
                             :prfPrdOilNetMillSm3
                             :prfMonth
                             :prfYear))
               (sort-by :date)
               (vec)))

(defn bucket-fn [{:keys [prfInformationCarrier percentage-produced]}]
  (cond
    (< percentage-produced 25) "0- 0–25"
    (< percentage-produced 50) "1- 25–50"
    (< percentage-produced 75) "2- 50–75"
    :else "5- 75–100"))

(defn produce-cumulative
  [production]
  {:pre [(coll? production) (= 1 (count (distinct (mapv :prfInformationCarrier production))))]}
  (->> (sort-by :date production)
       (reductions (fn [old n] (update n :cumulative (fn [v] (+ v (:cumulative old))))))
       (mapv #(assoc % :production-12-months-est (apply + (mapv :prfPrdLiquidsNetMillSm3 (:prev-rows %)))))
       (mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :remaining (- (:recoverable %) (:cumulative %))))
       (mapv #(assoc % :percentage-produced
                       (if (pos? (:recoverable %))
                         (* 100 (/ (:cumulative %) (:recoverable %)))
                         100.0)))
       (mapv #(assoc % :bucket (bucket-fn %)))))

(def with-cumulative (mapcat produce-cumulative (vals (group-by :prfInformationCarrier data))))

(def empty-buckets (reduce (fn [o n] (assoc o n 0)) {} (distinct (map :bucket with-cumulative))))

(defn process-date
  [production]
  {:pre [(coll? production)
         (= 1 (count (distinct (mapv :date production))))]}
  (merge {:date          (:date (first production))
          :sum           (/ (* 6.29 (reduce + 0 (mapv :production-12-months-est production)))
                            (production/prev-12-months-num-days (:date (first production))))}
         (reduce (fn [org [k v]]
                   (assoc org k
                              (/ (* 6.29 (->> production
                                              (filter #(= k (:bucket %)))
                                              (mapv :production-12-months-est)
                                              (reduce + 0)))
                                 (production/prev-12-months-num-days (:date (first production)))))) {} empty-buckets)))

(def by-date (->> (map process-date (vals (group-by :date with-cumulative)))
                  (sort-by :date)
                  (mapv #(assoc % :prfYear (edn/read-string (first (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :prfMonth (edn/read-string (last (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :eofYear (if (= 12 (:prfMonth %)) (:prfYear %)
                                                                    (dec (:prfYear %)))))))

(def year-end-data (->> by-date
                        (filter #(or (.endsWith (:date %) "-12") (= % (last by-date))))
                        (mapv #(assoc % :diff (Math/abs (- (:sum %)
                                                           (raw-production/sum-for-year-mboed (:prfYear %) :prfPrdLiquidsNetMillSm3)))))))

(csvmap/write-csv-format
  "./data/ncs/liquids-production-pp-bucket-stacked-yearly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum :diff])
   :format  (merge {:sum  "%.3f"
                    :diff "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.2f"]) empty-buckets)))
   :data    year-end-data})

(csvmap/write-csv-format
  "./data/ncs/liquids-production-pp-bucket-stacked-monthly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum])
   :format  (merge {:sum "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.2f"]) empty-buckets)))
   :data    by-date})

