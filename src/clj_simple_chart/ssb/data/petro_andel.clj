(ns clj-simple-chart.ssb.data.petro-andel
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
                                [:PetroleumProd :as :petroleumsprodukt] "Petroleumsprodukter i alt"
                                [:Kjopegrupper :as :kjopegruppe]        "*"
                                [:Tid :as :dato]                        "*"}))

(def data (->> raw-data
               (map #(dissoc % :petroleumsprodukt))
               (map #(dissoc % :ContentsCodeCategory))
               (map #(dissoc % :Region))
               (csv/number-or-throw-columns [:salg])
               (column-value->column :kjopegruppe)
               (rename-keys-remove-whitespace)
               (contract-by-column :dato)
               (drop-columns [:alle-kjøpegrupper])
               (map #(set/rename-keys % {:fiske-og-fangst :fiske
                                         :jordbruk-og-skogbruk :j&s
                                         :offentlig-virksomhet :off
                                         :industri-i-alt :industri
                                         :netto-direkte-import :import
                                         :boliger-og-næringsbygg :bolig
                                         :bygg-og-anlegg :bygg}))
               (flat->12-mms)
               (filter #(str/ends-with? (:dato %) "-12"))
               (relative-share)))
               ;(keep-columns [:dato :bolig])
               ;(relative-to-all-time-high)))
               ;(yoy-change)))
