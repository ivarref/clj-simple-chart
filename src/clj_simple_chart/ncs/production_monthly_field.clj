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

(test/is (= (:columns raw-data) [:prfInformationCarrier
                                 :prfYear
                                 :prfMonth
                                 :prfPrdOilNetMillSm3
                                 :prfPrdGasNetBillSm3
                                 :prfPrdNGLNetMillSm3
                                 :prfPrdCondensateNetMillSm3
                                 :prfPrdOeNetMillSm3
                                 :prfPrdProducedWaterInFieldMillSm3
                                 :prfNpdidInformationCarrier]))

(def numeric-columns [:prfYear
                      :prfMonth
                      :prfPrdOilNetMillSm3
                      :prfPrdGasNetBillSm3
                      :prfPrdNGLNetMillSm3
                      :prfPrdCondensateNetMillSm3
                      :prfPrdOeNetMillSm3
                      :prfPrdProducedWaterInFieldMillSm3])

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
               ; remove unused values
               (map #(dissoc % :prfPrdNGLNetMillSm3))
               (map #(dissoc % :prfPrdCondensateNetMillSm3))
               (map #(dissoc % :prfPrdProducedWaterInFieldMillSm3))
               (map #(dissoc % :prfNpdidInformationCarrier))
               (sort-by :date)
               (vec)))

(defn percentage-produced
  [production reserve item]
  (if (> (get item reserve) 0)
    (* 100 (/ (get item production) (get item reserve)))
    -1.0))

(defn add-prev-rows-last-n [n rows]
  (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 12 (take (inc idx) rows)))) rows))

(defn produce-cumulative
  [production]
  {:pre [(coll? production)]}
  (->> (sort-by :date production)
       ;(reductions (fn [old n] (assoc n :start-production (:start-production old))))
       (reductions (fn [old n] (update n :oil-cumulative (fn [v] (+ v (:oil-cumulative old))))))
       (reductions (fn [old n] (update n :gas-cumulative (fn [v] (+ v (:gas-cumulative old))))))
       (reductions (fn [old n] (update n :oe-cumulative (fn [v] (+ v (:oe-cumulative old))))))
       (add-prev-rows-last-n 12)
       (filter #(= 12 (count (:prev-rows %))))
       (mapv #(assoc % :gas-production-12-months (apply + (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))))
       (mapv #(dissoc % :prev-rows))
       (mapv #(assoc % :gas-remaining (- (:fldRecoverableGas %) (:gas-cumulative %))))
       (filter #(pos? (:gas-production-12-months %)))
       (filter #(pos? (:gas-remaining %)))
       (mapv #(assoc % :gas-rp (/ (:gas-remaining %) (:gas-production-12-months %))))))

(def with-cumulative (mapcat produce-cumulative (vals (group-by :prfInformationCarrier data))))

(defn process-date
  [empty-buckets production]
  {:pre [(coll? production)]}
  (let [buckets (group-by :bucket production)
        days-in-month (:days-in-month (first production))
        mboe (fn [x] (format "%.2f" (/ (* 6.29 x) days-in-month)))
        ;oil-buckets (map #(mboe (reduce + 0.0 (map :prfPrdOilNetMillSm3 %))) (vals buckets))
        gas-buckets (map #(mboe (reduce + 0.0 (map :prfPrdGasNetBillSm3 %))) (vals buckets))
        ;oe-buckets (map #(mboe (reduce + 0.0 (map :prfPrdOeNetMillSm3 %))) (vals buckets))
        ]
    (merge {:date          (:date (first production))
            :days-in-month days-in-month
            :total         (reduce + 0 (map :prfPrdGasNetBillSm3 production))
            :mboed         (mboe (reduce + 0 (map :prfPrdGasNetBillSm3 production)))}
           (merge empty-buckets (zipmap (keys buckets) gas-buckets)))))

(defn mma [prod {date :date}]
  (let [items (take-last 12 (filter #(>= (compare date (:date %)) 0) prod))
        production (->> items (map :total) (reduce + 0))
        days (->> items (map :days-in-month) (reduce + 0))]
    (format "%.2f" (/ (* 6.29 production) days))))

(defn generate-bucket-file
  [filename data bucket-fn]
  (let [with-bucket (->> data (mapv #(assoc % :bucket (bucket-fn %))))
        empty-buckets (reduce (fn [o n] (assoc o n "0.00")) {} (distinct (map :bucket with-bucket)))
        flat-production (->> with-bucket
                             (group-by :date)
                             vals
                             (map (partial process-date empty-buckets))
                             (sort-by :date))
        with-mma (->> flat-production
                      (map #(assoc % :mma (mma flat-production %)))
                      (map #(dissoc % :days-in-month))
                      (map #(dissoc % :total)))]
    (csvmap/write-csv filename
                      {:columns (cons :date (cons :mma (sort (keys empty-buckets))))
                       :data    (filter #(or (.endsWith (:date %) "-12")
                                             (= % (last with-mma))) with-mma)})
    (println "wrote" filename)))

(generate-bucket-file "./data/ncs/gas-production-rp-bucket-stacked.csv"
                      with-cumulative
                      #(cond
                         (< (:gas-rp %) 1) "1- 0 - 1 R/P"
                         (< (:gas-rp %) 2) "2- 1 - 2 R/P"
                         (< (:gas-rp %) 5) "3- 2 - 5 R/P"
                         (< (:gas-rp %) 10) "4- 5 - 10 R/P"
                         (< (:gas-rp %) 15) "5- 10 - 15 R/P"
                         :else "6- >= 15 R/P"))

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
