(ns clj-simple-chart.ssb.data.gjeld
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.ssb.data.lønnsvekst :as lonnsvekst]
            [clojure.set :as set]
            [clojure.string :as str]))

(def q {"Valuta"       "I alt"
        "Lantaker2"    "Husholdninger mv."
        "ContentsCode" "Transaksjoner, tolvmånedersvekst (prosent)"
        "Tid"          "*"})

(def data (->> (ssb/fetch 11599 q)
               (remove-nils)
               (keep-columns [:Tid :ContentsCode])
               (filter #(str/ends-with? (:Tid %) "-12"))
               (map #(set/rename-keys % {:ContentsCode :gjeldsvekst}))
               (map #(update % :Tid (fn [x] (subs x 0 4))))
               (map #(assoc % :lonnsvekst (get lonnsvekst/data (read-string (:Tid %)))))
               (remove-nils)))

(def cumulative (reductions (fn [o v]
                              (-> o
                                  (assoc :Tid (:Tid v))
                                  (update :gjeldsvekst #(* % (+ 1.0 (/ (:gjeldsvekst v) 100))))
                                  (update :lonnsvekst #(* % (+ 1.0 (/ (:lonnsvekst v) 100))))))
                            {:Tid "1999" :gjeldsvekst 100 :lonnsvekst 100} data))