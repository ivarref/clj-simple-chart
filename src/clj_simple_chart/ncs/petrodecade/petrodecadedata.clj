(ns clj-simple-chart.ncs.petrodecade.petrodecadedata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.ncs.raw-production :as raw-production]
            [clj-simple-chart.ncs.discovery-year :as discovery-year]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.string :as str])
  (:import (java.time YearMonth)))

(def data (->> raw-production/data
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               (map #(dissoc % :prfPrdNGLNetMillSm3
                             :prfPrdCondensateNetMillSm3
                             :prfPrdProducedWaterInFieldMillSm3
                             :prfNpdidInformationCarrier
                             :prfPrdGasNetBillSm3
                             :prfPrdOilNetMillSm3
                             :prfMonth
                             :prfYear))
               (sort-by :date)
               (vec)))

(defn produce-bucket
  [production]
  {:pre [(coll? production) (= 1 (count (distinct (mapv :prfInformationCarrier production))))]}
  (->> (sort-by :date production)
       (mapv #(assoc % :production-12-months-est (apply + (mapv :prfPrdOeNetMillSm3 (:prev-rows %)))))
       (mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :bucket (discovery-year/discovery-decade-bucket (:prfInformationCarrier %))))))

(def with-bucket (mapcat produce-bucket (vals (group-by :prfInformationCarrier data))))

(def empty-buckets (reduce (fn [o n] (assoc o n 0)) {} (distinct (map :bucket with-bucket))))

(defn process-date
  [production]
  {:pre [(coll? production)]}
  (merge {:date          (:date (first production))
          :sum           (reduce + 0 (mapv :production-12-months-est production))}
         (reduce (fn [org [k v]]
                   (assoc org k
                              (->> production
                                   (filter #(= k (:bucket %)))
                                   (mapv :production-12-months-est)
                                   (reduce + 0)))) {} empty-buckets)))

(def by-date (->> (map process-date (vals (group-by :date with-bucket)))
                  (sort-by :date)
                  (mapv #(assoc % :prfYear (read-string (first (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :prfMonth (read-string (last (str/split (:date %) #"-0?")))))
                  (mapv #(assoc % :eofYear (if (= 12 (:prfMonth %)) (:prfYear %)
                                                                    (dec (:prfYear %)))))))

(def year-end-data (->> by-date
                        (filter #(or (.endsWith (:date %) "-12") (= % (last by-date))))
                        (mapv #(assoc % :diff (Math/abs (- (:sum %)
                                                           (raw-production/sum-for-year (:prfYear %) :prfPrdOeNetMillSm3)))))))

(csvmap/write-csv-format
  "./data/ncs/petro-production-discovery-decade-bucket-stacked-yearly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum :diff])
   :format  (merge {:sum  "%.3f"
                    :diff "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.1f"]) empty-buckets)))
   :data    year-end-data})

(csvmap/write-csv-format
  "./data/ncs/petro-production-discovery-decade-bucket-stacked-monthly.csv"
  {:columns (flatten [:date (sort (keys empty-buckets)) :sum])
   :format  (merge {:sum "%.3f"}
                   (into {} (mapv (fn [[k v]] [k "%.1f"]) empty-buckets)))
   :data    by-date})

(def field-names (->> data
                      (mapv :prfInformationCarrier)
                      (distinct)
                      (sort)
                      (vec)))
