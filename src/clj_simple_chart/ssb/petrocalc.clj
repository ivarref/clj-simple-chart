(ns clj-simple-chart.ssb.petrocalc
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.csv.csvmap :as csv]))

(def skatt (mapv keyword (take 3 petroskatt/skattart)))
(def data petroskatt/twelve-mma)

(def summed-data (->> data
                      (mapv (fn [x]
                              (assoc x :sum
                                       (reduce + 0 (mapv (partial get x) (take 2 skatt))))))
                      (mapv (fn [x] {:dato (:dato x) :sum (:sum x)}))))

(def mx (apply max (map :sum summed-data)))

(def sum-kw (keyword "Sum petroleumsskatt mrd kr"))
(def relative-kw (keyword "Relativt til toppnivå"))

(def relative (->> summed-data
                   (mapv (fn [x] {:dato       (:dato x)
                                  sum-kw      (format "%.1f" (/ (:sum x) 1000))
                                  relative-kw (format "%.1f" (* 100 (/ (:sum x) mx)))}))))

(csv/write-csv "7022-mrd-relative-12-mms.csv" {:columns [:dato relative-kw sum-kw]
                                               :data    relative})