(ns clj-simple-chart.ssb.data.sal-av-petroleumsprodukt-11174
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clojure.test :as test]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]
            [clojure.set :as set]))

; 11174: Salg av petroleumsprodukter, etter kjøpegruppe og produkttype (mill. liter). Foreløpige tall (F) 2010M01 - 2017M12
;
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def raw-data (ssb/fetch 11174 {[:ContentsCode :as :salg]               "Salg"
                                "Region"                                "Hele landet"
                                [:PetroleumProd :as :petroleumsprodukt] ["Petroleumsprodukter i alt" "Autodiesel" "Bilbensin"]
                                [:Kjopegrupper :as :kjøpegruppe]        "Alle kjøpegrupper"
                                [:Tid :as :dato]                        "*"}))

(def data (->> raw-data
               (map #(dissoc % :kjøpegruppe))
               (map #(dissoc % :ContentsCodeCategory))
               (map #(dissoc % :Region))
               (csv/number-or-throw-columns [:salg])
               (column-value->column :petroleumsprodukt)
               (contract-by-column :dato)
               (map #(set/rename-keys % {(keyword "petroleumsprodukter i alt") :sum-petroleum}))
               (flat->12-mms)))

;(def data-relative (relative-to-all-time-high data))

(test/is (= "2010-12" (:dato (first data))))
(test/is (= "2018-04" (:dato (last data))))