(ns clj-simple-chart.ssb.data.sal-av-petroleumsprodukt-11174
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clojure.test :as test]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]))

; 11174: Salg av petroleumsprodukter, etter kjøpegruppe og produkttype (mill. liter). Foreløpige tall (F) 2010M01 - 2017M12
;
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def raw-data (ssb/fetch 11174 {"ContentsCode"  "Salg"
                                "Region"        "Hele landet"
                                "PetroleumProd" ["Autodiesel" "Bilbensin"]
                                "Kjopegrupper"  "Alle kjøpegrupper"
                                "Tid"           "*"}))

(test/is (= [:kjøpegruppe :petroleumsprodukt :region :salg] (:columns raw-data)))

(def data (->> (:data raw-data)
               (map #(dissoc % :kjøpegruppe))
               (map #(dissoc % :region))
               (csv/number-or-throw-columns [:salg])
               (column-value->column :petroleumsprodukt)
               (contract-by-column :dato)))

(test/is (= "2010-01" (:dato (first data))))