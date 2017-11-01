(ns clj-simple-chart.ncs.raw-production
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.csv.csvmap :as csvmap])
  (:import (java.time YearMonth Year)))

; Goal:
; Raw production with
; * filled gaps
; * appended 11 empty entries for shut down fields
; * prev-rows property containing 12 last months of same field
; * :prfPrdLiquidsNetMillSm3 which is the sum of :prfPrdOilNetMillSm3 :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3

(defn year-month [s]
  {:pre [(string? s)]}
  (let [parts (string/split s #"-0?")
        year (read-string (first parts))
        month (read-string (last parts))]
    (YearMonth/of year month)))

(defn date-range
  ([start stop] (date-range [] start stop))
  ([sofar start stop]
   (cond
     (string? start) (recur sofar (year-month start) stop)
     (string? stop) (recur sofar start (year-month stop))
     (.equals start stop)
     (mapv #(format "%04d-%02d" (.getYear %) (.getMonthValue %)) (conj sofar stop))
     :else (date-range (conj sofar start) (.plusMonths start 1) stop))))

(defn prev-12-months [s]
  {:pre  [(string? s)]
   :post [(= 12 (count %))]}
  (let [parts (string/split s #"-0?")
        year (read-string (first parts))
        month (read-string (last parts))]
    (date-range (.minusMonths (YearMonth/of year month) 11)
                (YearMonth/of year month))))

(defn prev-12-months-num-days [s]
  (apply + (mapv #(.lengthOfMonth %) (mapv year-month (prev-12-months s)))))

(def field-monthly-production-url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_production_monthly&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=80.213.237.130&CultureCode=en")
(defonce raw-data (-> field-monthly-production-url
                      (client/get)
                      (:body)
                      (csv/csv-map)))

(test/is (= (:columns raw-data) [:prfInformationCarrier :prfYear :prfMonth
                                 :prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3
                                 :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3
                                 :prfPrdOeNetMillSm3 :prfPrdProducedWaterInFieldMillSm3 :prfNpdidInformationCarrier]))

(def numeric-columns [:prfYear :prfMonth :prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3 :prfPrdNGLNetMillSm3
                      :prfPrdCondensateNetMillSm3 :prfPrdOeNetMillSm3 :prfPrdProducedWaterInFieldMillSm3])

(def filled-gaps-for-fields (atom #{}))
(def filled-data (atom #{}))

(defn- fill-gaps [max-date production]
  {:pre [(coll? production)]}
  (let [f (first production)
        field-name (:prfInformationCarrier (first production))
        dates (sort (mapv :date production))
        global-end-date (year-month max-date)
        end-date (.plusMonths (year-month (last dates)) 11)
        end-date (if (.isAfter end-date global-end-date) global-end-date end-date)
        date-range (date-range (first dates) end-date)
        missing-months (remove #(some #{%} dates) date-range)
        filled-gaps (mapv (fn [date]
                            {:prfInformationCarrier             (:prfInformationCarrier f)
                             :date                              date
                             :prfYear                           (.getYear (year-month date))
                             :prfMonth                          (.getMonthValue (year-month date))
                             :prfPrdOilNetMillSm3               0.0
                             :prfPrdGasNetBillSm3               0.0
                             :prfPrdNGLNetMillSm3               0.0
                             :prfPrdCondensateNetMillSm3        0.0
                             :prfPrdOeNetMillSm3                0.0
                             :prfPrdProducedWaterInFieldMillSm3 0.0
                             :prfNpdidInformationCarrier        (:prfNpdidInformationCarrier f)}) missing-months)]
    (when-not (empty? missing-months)
      (swap! filled-data (fn [o] (conj o filled-gaps)))
      (swap! filled-gaps-for-fields (fn [o] (conj o field-name))))
    (concat production filled-gaps)))

(def pre-group-data (->> raw-data
                         :data
                         (csv/read-string-columns numeric-columns)
                         (csv/number-or-throw-columns numeric-columns)
                         (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
                         (map #(assoc % :date-int (+ (* 100 (:prfYear %)) (:prfMonth %))))))

(def max-date (:date (last (sort-by :date-int pre-group-data))))

(def pre-prev-rows (->> pre-group-data
                        (group-by :prfInformationCarrier)
                        (vals)
                        (mapv (partial fill-gaps max-date))
                        (flatten)
                        (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
                        (map #(assoc % :date-int (+ (* 100 (:prfYear %)) (:prfMonth %))))
                        (map #(assoc % :prev-months (prev-12-months (:date %))))
                        (map #(assoc % :prfPrdLiquidsNetMillSm3 (reduce + 0 (mapv % [:prfPrdOilNetMillSm3 :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3]))))
                        (sort-by :date)
                        (vec)))

(defn add-prev-rows-last-n [n rows]
  {:pre [(= 1 (count (distinct (mapv :prfInformationCarrier rows))))]}
  (mapv (fn [x] (assoc x :prev-rows
                         (doall (->> rows
                                     (filter #(some #{(:date %)} (:prev-months x)))
                                     (mapv #(dissoc % :prev-months)))))) rows))

(def data (->> pre-prev-rows
               (group-by :prfInformationCarrier)
               (vals)
               (mapv #(add-prev-rows-last-n 12 %))
               (flatten)
               (sort-by :date)
               (mapv #(dissoc % :prev-months))
               (vec)))

(def frigg-last-dates (->> data
                           (filter #(= "FRIGG" (:prfInformationCarrier %)))
                           (mapv :date)
                           (sort)
                           (take-last 12)
                           (vec)))

(def expected-frigg-last-dates (date-range "2004-10" (.plusMonths (year-month "2004-10") 11)))

(test/is (= frigg-last-dates expected-frigg-last-dates))

(def field-names (->> data
                      (mapv :prfInformationCarrier)
                      (sort)
                      (distinct)
                      (vec)))

(defn sum-for-year [year kind]
  (->> data
       (filter #(= year (:prfYear %)))
       (mapv kind)
       (mapv double)
       (sort)
       (reduce + 0.0)))

(defn sum-for-year-mboed [year kind]
  (/ (* 6.29 (sum-for-year year kind))
     (.length (Year/of year))))

(defn sum-for-year-format [year kind]
  (format "%.3f" (sum-for-year year kind)))

(defn sum-for-year-mboed-format [year kind]
  (format "%.3f" (sum-for-year-mboed year kind)))

(test/is (= "94.009" (sum-for-year-format 2016 :prfPrdOilNetMillSm3)))
(test/is (= "90.965" (sum-for-year-format 2015 :prfPrdOilNetMillSm3)))
(test/is (= "87.741" (sum-for-year-format 2014 :prfPrdOilNetMillSm3)))

;; According to OD this should be 116.649
(test/is (= "116.650" (sum-for-year-format 2016 :prfPrdGasNetBillSm3)))

(test/is (= "117.152" (sum-for-year-format 2015 :prfPrdGasNetBillSm3)))
(test/is (= "108.820" (sum-for-year-format 2014 :prfPrdGasNetBillSm3)))
(test/is (= "108.746" (sum-for-year-format 2013 :prfPrdGasNetBillSm3)))
(test/is (= "79.179" (sum-for-year-format 2004 :prfPrdGasNetBillSm3)))

(def whole-2004 (->> data
                     (filter #(= 2004 (:prfYear %)))
                     (filter #(pos? (:prfPrdGasNetBillSm3 %)))))