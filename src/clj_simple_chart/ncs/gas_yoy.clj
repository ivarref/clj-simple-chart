(ns clj-simple-chart.ncs.gas-yoy
  (:require [clj-simple-chart.ncs.production-monthly-field :as pmf]
            [clj-simple-chart.csv.csvmap :as csv]))

(def data (->> pmf/data
               (csv/drop-columns [:prfYear :prfMonth
                                  :prfPrdOilNetMillSm3
                                  :prfPrdProducedWaterInFieldMillSm3
                                  :prfPrdOeNetMillSm3
                                  :prfPrdNGLNetMillSm3
                                  :prfPrdCondensateNetMillSm3])
               (group-by :date)
               (mapv (fn [[k v]]
                       {:date                k
                        :prfPrdGasNetBillSm3 (apply + (mapv :prfPrdGasNetBillSm3 v))}))
               (sort-by :date)))

(def data-with-12-mms (->> data
                           (map-indexed (fn [idx row] (assoc row :prev-rows (take-last 12 (take (inc idx) data)))))
                           (filter #(= 12 (count (:prev-rows %))))
                           (mapv #(assoc % :prfPrdGasNetBillSm3 (apply + (mapv :prfPrdGasNetBillSm3 (:prev-rows %)))))
                           (mapv #(dissoc % :prev-rows))))

(def data-with-12-mms-ytd (->> data-with-12-mms
                               (filter #(or (= (last data-with-12-mms) %)
                                            (.endsWith (:date %) "-12")))))

(csv/write-csv-format "data/ncs/gas-12-mms-ytd.csv"
                      {:columns [:date :prfPrdGasNetBillSm3]
                       :format  {:prfPrdGasNetBillSm3 "%.3f"}
                       :data    data-with-12-mms-ytd})
