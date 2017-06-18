(ns clj-simple-chart.bp.bpdata2
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.bp.data-oil-reserves :as oil-reserve]
            [clojure.string :as string]))

(def data-url "https://raw.githubusercontent.com/ivarref/EnergyExportDatabrowser/master/StaticData/2017/BP_2017_oil_production_bbl.csv")

(defonce response (client/get data-url))

(test/is (= 200 (:status response)))

(def expected-columns
  [:YEAR
   :NO
   :IQ
   :IR])

(def columns (:columns (csv/csv-map (:body response))))

(def giga (-> "1_000_000_000" (string/replace "_" "") (read-string)))

(def data (->> (csv/csv-map (:body response))
               (csv/assert-columns expected-columns)
               (:data)
               (csv/read-string-columns columns)
               (csv/number-or-nil-columns columns)
               (filter #(= 2016 (:YEAR %)))
               (first)
               (reduce (fn [o [k v]]
                         (if (= 2 (count (name k)))
                           (conj o {:country k
                                    :oil_production_kbd v
                                    :oil_reserve_gb (get oil-reserve/cc-to-oil-reserve-gb k)})
                           o)) [])
               (mapv #(assoc % :rp (/ (* giga (:oil_reserve_gb %))
                                      (* 1000 365 (:oil_production_kbd %)))))
               (sort-by :oil_production_kbd)
               (take-last 15)
               ))


