(ns clj-simple-chart.ssb.data.registrerte-kjoretoy
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; https://www.ssb.no/statbank/table/07832?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def q {"Region"       "Hele landet"
        "Kjoretoy"     ["Tesla" "Tesla Motors"]
        "ContentsCode" "Personbiler" ; kan vera fleire/andre
        "Tid"          "*"})

(def data (->> (ssb/fetch 7832 q)
               (:data)
               (drop-columns [:region])
               (column-value->column :merke)
               (contract-by-column :dato)
               (add-sum-column)
               (keep-columns [:dato :sum])
               (vec)))

