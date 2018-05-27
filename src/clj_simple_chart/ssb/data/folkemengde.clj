(ns clj-simple-chart.ssb.data.folkemengde
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]))

; 05231
; https://www.ssb.no/statbank/table/05231?rxid=59da4113-216c-4c9e-b2b7-a4829792a4dd

(def yyyy-mm->folkemengde (->> (ssb/fetch
                                "05231"
                                {[:Region]                   "Hele landet"
                                 [:ContentsCode :as :antall] "Beregnet folkemengde 31. desember"
                                 [:Tid :as :dato]            "*"})
                               (drop-columns [:ContentsCodeCategory :Region])
                               (add-column-postfix :dato "-12")
                               (map (juxt :dato :antall))
                               (into {})))

(defn folkemengde
  [dato]
  (if (= 4 (count dato))
    (get yyyy-mm->folkemengde (str dato "-12"))
    (get yyyy-mm->folkemengde dato)))
