(ns clj-simple-chart.eurostat.data.avia-par-no2
  (:require [clj-simple-chart.eurostat.data.avia-par-no :refer [data-monthly]]
            [clj-simple-chart.data.utils :refer :all]
            [clojure.string :as str]))

(def data-prepare (->> data-monthly
                       (drop-columns [:unit :tra_meas :to :codes :airp_pr])
                       (column-value->column :from)
                       (contract-by-column :date)
                       (flat->12-mms)
                       (div-by-no-round 1000)))

(def n 10)

(def top-n (vec (reverse (take-last n (sort-by second (into [] (dissoc (last data-prepare) :date)))))))
(def top-n-names (mapv first top-n))
(def bottom-n (vec (drop-last n (sort-by second (into [] (dissoc (last data-prepare) :date))))))

(def other-names (->> (for [row data-monthly]
                        (keyword (str/lower-case  (:from row))))
                      (distinct)
                      (sort)
                      (remove #(some #{%} top-n-names))
                      (vec)))

(def data (->> (for [row data-prepare]
                 (let [others (->> row
                                   (filter #(some #{(first %)} other-names))
                                   (map second)
                                   (reduce + 0))]
                   (assoc row :others others)))
               (keep-columns (flatten [top-n-names :others :date]))
               (add-sum-column)))