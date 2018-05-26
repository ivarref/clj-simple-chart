(ns clj-simple-chart.ssb.data.koyrelengde
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; 07301 - https://www.ssb.no/statbank/table/07301?rxid=7b60ade9-9818-4a73-b33c-6389472eb597

(def data (->> (ssb/fetch 7301 {[:ContentsCode :as :km] "*"
                                :Kjoretoytype           "*"
                                [:Tid :as :dato]        "*"})
               (drop-columns [:ContentsCodeCategory])
               (column-value->column :Kjoretoytype)
               (rename-keys-remove-whitespace)
               (contract-by-column :dato)
               (keep-columns [:dato
                              :personbiler-i-alt
                              :busser-i-alt
                              :små-godsbiler-i-alt
                              :store-lastebiler-i-alt])
               (div-by-no-round 1000)
               (add-sum-column)))
