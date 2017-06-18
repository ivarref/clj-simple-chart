(ns clj-simple-chart.bp.data-oil-reserves
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def data-url "https://raw.githubusercontent.com/ivarref/EnergyExportDatabrowser/master/StaticData/2017/BP_2017_oil_proved_reserves_gb.csv")

(defonce response (client/get data-url))

(test/is (= 200 (:status response)))

(def expected-columns
  [:YEAR
   :NO
   :IQ
   :IR])

(def columns (:columns (csv/csv-map (:body response))))

(def cc-to-oil-reserve-gb
  (->> (csv/csv-map (:body response))
       (csv/assert-columns expected-columns)
       (:data)
       (csv/read-string-columns columns)
       (csv/number-or-nil-columns columns)
       (filter #(= 2016 (:YEAR %)))
       (first)
       (reduce (fn [o [k v]]
                 (if (= 2 (count (name k)))
                   (assoc o k v)
                   o)) {})))
