(ns clj-simple-chart.ncs.production-monthly-field
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv])
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
               (csv/read-string-columns numeric-columns)
               (csv/number-or-nil-columns numeric-columns)
               (map #(assoc % :date (str (format "%04d-%02d" (:prfYear %) (:prfMonth %)))))
               (map #(assoc % :days-in-month (. (YearMonth/of (:prfYear %) (:prfMonth %)) lengthOfMonth)))
               (map #(dissoc % :prfNpdidInformationCarrier))
               (sort-by :date)
               (vec)))

(def field-names (->> data
                      (mapv :prfInformationCarrier)
                      (distinct)
                      (sort)
                      (vec)))

(test/is (some #{"ÅSGARD"} field-names))
(test/is (some #{"ØST FRIGG"} field-names))
(test/is (some #{"STATFJORD"} field-names))
(test/is (some #{"EKOFISK"} field-names))
(test/is (some #{"TROLL"} field-names))
(test/is (some #{"SINDRE"} field-names))
(test/is (some #{"GINA KROG"} field-names))
(test/is (some #{"TYRIHANS"} field-names))
(test/is (some #{"REV"} field-names))
(test/is (some #{"GULLFAKS"} field-names))
