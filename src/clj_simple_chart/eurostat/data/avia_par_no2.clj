(ns clj-simple-chart.eurostat.data.avia-par-no2
  (:require [clj-simple-chart.eurostat.data.avia-par-no :refer [data-monthly]]
            [clj-simple-chart.data.utils :refer :all]))

(def data-prepare (->> data-monthly
                       (drop-columns [:unit :tra_meas :to :codes :airp_pr])
                       (column-value->column :from)
                       (contract-by-column :date)
                       (flat->12-mms)
                       (remove-whitespace-in-keys)
                       (div-by-no-round 1000)))

(def top-n-names (top-n-keys-last 10 data-prepare))
(def other-names (other-keys top-n-names data-prepare))

(def data (->> data-prepare
               (sum-but :others top-n-names)
               (add-sum-column)))