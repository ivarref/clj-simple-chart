(ns clj-simple-chart.ncs.all-fields-production-monthly
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clojure.test :as test]
            [clojure.string :as str]
            [clj-http.client :as client])
  (:import (java.time YearMonth)))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_production_totalt_NCS_month__DisplayAllRows&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=77.13.13.136&CultureCode=en")

(defonce raw-data (-> url
                      (client/get)
                      (:body)
                      (csv/csv-map)))

(test/is (= (:columns raw-data) [:prfYear :prfMonth
                                 :prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3
                                 :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3
                                 :prfPrdOeNetMillSm3 :prfPrdProducedWaterInFieldMillSm3]))

(defn dato->num-days [s]
  {:pre [(string? s)]}
  (let [[year month] (str/split s #"-0?")
        year (Integer/valueOf ^String year)
        month (Integer/valueOf ^String month)]
    (.lengthOfMonth (YearMonth/of year month))))

(defn string->double [^String s]
 (if (str/starts-with? s "(")
   (-> s
       (str/replace "(" "-")
       (str/replace ")" "")
       (Double/valueOf))
   (Double/valueOf s)))

(defn double-value [ks row]
  (reduce (fn [o k] (update o k string->double)) row ks))

(defn make-row [{:keys [prfYear prfMonth prfPrdOilNetMillSm3 prfPrdNGLNetMillSm3 prfPrdCondensateNetMillSm3]}]
  {:year prfYear
   :dato (format "%s-%02d" prfYear (Integer/valueOf prfMonth))
   :oil (+ prfPrdOilNetMillSm3 prfPrdNGLNetMillSm3 prfPrdCondensateNetMillSm3)})

(def oil-data
  (->> raw-data
       :data
       (map (partial double-value [:prfPrdOilNetMillSm3 :prfPrdGasNetBillSm3
                                   :prfPrdNGLNetMillSm3 :prfPrdCondensateNetMillSm3
                                   :prfPrdOeNetMillSm3 :prfPrdProducedWaterInFieldMillSm3]))
       (map make-row)
       (sort-by :dato)))

(def last-12-months (take-last 12 oil-data))

(def year-2000 (filter #(= "2000" (:year %)) oil-data))
(def year-2001 (filter #(= "2001" (:year %)) oil-data))
(def year-2002 (filter #(= "2002" (:year %)) oil-data))

(defn per-day [selection kind]
  (let [total (reduce + 0 (map kind selection))
        days (reduce + 0 (map (comp dato->num-days :dato) selection))]
    (/ (* 6.29 total) days)))

(comment
  (do
    #_(println (format "Oljeproduksjon år 2000: %.2f Millionar fat/dag" (per-day year-2000 :oil)))
    (println (format "Oljeproduksjon år 2001: %.2f Millionar fat/dag" (per-day year-2001 :oil)))
    #_(println (format "Oljeproduksjon år 2002: %.2f Millionar fat/dag" (per-day year-2002 :oil)))
    (println (format "Oljeproduksjon siste 12 månadar: %.2f Millionar fat/dag" (per-day last-12-months :oil)))))

; Sverdrup:
; crude oil mill sm3 406.58
; ngl mill sm3 4.50
; condensate mill sm3 0.0

; 2.7 mrd fat oljeekvivalentar (inkl. gass)
