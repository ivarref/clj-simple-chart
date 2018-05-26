(ns clj-simple-chart.ssb.data.elbil
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]))

; https://www.ssb.no/statbank/table/07849/?rxid=2b8be3ee-7cb7-4de5-b767-427bfffe1d18
; 07849: Registrerte kjøretøy, etter kjøringens art og drivstofftype (K) 2008 - 2016

(def drivstoff-translate {"El."             "Elektrisk"
                          "Annet drivstoff" "Annet"})

(def data (->> (ssb/fetch 7849 {"Region"                        "Hele landet"
                                "KjoringensArt"                 "*"
                                [:DrivstoffType :as :drivstoff] "*"
                                [:ContentsCode :as :antall]     "*"
                                [:Tid :as :dato]                "*"})
               (drop-columns [:Region :KjoringensArt :ContentsCodeCategory])
               (csv/number-or-throw-columns [:antall])
               (map #(update % :drivstoff (fn [d] (get drivstoff-translate d d))))
               (map #(update % :dato (fn [d] (str d "-12"))))
               (column-value->column :drivstoff)
               (contract-by-column :dato)
               (drop-columns [:parafin :gass])
               (div-by 1000)
               (add-sum-column)))

(def data2 (->> (ssb/fetch 7849 {"Region"                        "Hele landet"
                                 [:KjoringensArt :as :kjoretype] "*"
                                 [:DrivstoffType :as :drivstoff] "*"
                                 [:ContentsCode :as :antall]     "*"
                                 [:Tid :as :dato]                "*"})
                (drop-columns [:Region :drivstoff :kjoretype])
                (csv/number-or-throw-columns [:antall])
                ;(map #(update % :drivstoff (fn [d] (get drivstoff-translate d d))))
                (map #(update % :dato (fn [d] (str d "-12"))))
                (column-value->column :ContentsCodeCategory)
                (contract-by-column :dato)
                (rename-keys-remove-whitespace)
                (div-by 1000)
                (add-sum-column)))

(def fossil (->> (ssb/fetch 7849
                            {"Region"                        "Oslo"
                             "KjoringensArt"                 "Egentransport"
                             [:DrivstoffType :as :drivstoff] ["Bensin" "Diesel" "Annet drivstoff"]
                             [:ContentsCode :as :antall]     "Personbiler"
                             [:Tid :as :dato]                "*"})
                 (drop-columns [:Region :KjoringensArt :ContentsCodeCategory])
                 (csv/number-or-throw-columns [:antall])
                 (map #(update % :drivstoff (fn [d] (get drivstoff-translate d d))))
                 (column-value->column :drivstoff)
                 (contract-by-column :dato)
                 (add-sum-column)
                 (yoy-change)
                 (drop-while #(not= "2013" (:dato %))) (map :sum) (reduce + 0)))
;(keep-columns [:dato :sum])))

;(yoy-change)))
;(column-value->column :drivstofftype)
;(contract-by-column :dato)
;(yoy-change)))
