(ns clj-simple-chart.ssb.data.petroleum-kjopegruppe
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d
; 11174

(def data (->> (ssb/fetch
                 11174
                 {[:ContentsCode :as :salg]               "Salg"
                  "Region"                                "Hele landet"
                  [:PetroleumProd :as :petroleumsprodukt] "Petroleumsprodukter i alt"
                  [:Kjopegrupper :as :kjopegruppe]        "*"
                  [:Tid :as :dato]                        "*"})
               (drop-columns [:ContentsCodeCategory :Region :petroleumsprodukt])
               (remove #(= "Alle kjøpegrupper" (:kjopegruppe %)))
               (column-value->column :kjopegruppe)
               (contract-by-column :dato)
               (rename-keys-remove-whitespace)
               (flat->12-mms)
               (add-sum-column)))
