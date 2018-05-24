(ns clj-simple-chart.ssb.data.petroleum-location
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clojure.test :as test]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]
            [clojure.set :as set]))

; 11174: Salg av petroleumsprodukter, etter kjøpegruppe og produkttype (mill. liter). Foreløpige tall (F) 2010M01 - 2017M12
;
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(defn data
  [location]
  (->> (ssb/fetch 11174 {[:ContentsCode :as :salg]               "Salg"
                         "Region"                                location
                         [:PetroleumProd :as :petroleumsprodukt] ["Autodiesel" "Bilbensin"]
                         [:Kjopegrupper :as :kjøpegruppe]        "Transport"
                         [:Tid :as :dato]                        "*"})
       (map #(dissoc % :kjøpegruppe))
       (map #(dissoc % :ContentsCodeCategory))
       (map #(dissoc % :Region))
       (csv/number-or-throw-columns [:salg])
       (column-value->column :petroleumsprodukt)
       (contract-by-column :dato)
       (flat->12-mms)
       (add-sum-column)))
;(relative-to-all-time-high)))

;(def data-relative (relative-to-all-time-high data))

;(test/is (= "2010-12" (:dato (first data))))
;(test/is (= "2018-04" (:dato (last data))))