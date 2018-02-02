(ns clj-simple-chart.ssb.data.elbil
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; https://www.ssb.no/statbank/table/07849/?rxid=2b8be3ee-7cb7-4de5-b767-427bfffe1d18
; 07849: Registrerte kjøretøy, etter kjøringens art og drivstofftype (K) 2008 - 2016

(def q {"Region"        "Hele landet"
        "KjoringensArt" "Egentransport"
        "DrivstoffType" ["El." "Bensin" "Diesel"]
        "ContentsCode"  "Personbiler"
        "Tid"           "*"})

(def data (->> (ssb/fetch 7849 q)
               (:data)
               (drop-columns [:region (keyword "type kjøring")])
               (column-value->column :drivstofftype)
               (contract-by-column :dato)
               (yoy-change)))
