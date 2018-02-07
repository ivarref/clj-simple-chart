(ns clj-simple-chart.ssb.data.gjeld
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.ssb.data.lønnsvekst :as lonnsvekst]
            [clojure.set :as set]
            [clojure.string :as str]))

(def q {:Valuta                          "I alt"
        :Lantaker2                       "Husholdninger mv."
        [:ContentsCode :as :gjeldsvekst] "Transaksjoner, tolvmånedersvekst (prosent)"
        [:Tid :as :tid]                  "*"})

(def data (->> (ssb/fetch 11599 q)
               (remove-nils)
               (keep-columns [:tid :gjeldsvekst])
               (filter #(str/ends-with? (:tid %) "-12"))
               (map #(update % :tid (fn [x] (subs x 0 4))))
               (map #(assoc % :lonnsvekst (get lonnsvekst/data (read-string (:tid %)))))
               (remove-nils)))

(def cumulative (reductions (fn [o v]
                              (-> o
                                  (assoc :tid (:tid v))
                                  (update :gjeldsvekst #(* % (+ 1.0 (/ (:gjeldsvekst v) 100))))
                                  (update :lonnsvekst #(* % (+ 1.0 (/ (:lonnsvekst v) 100))))))
                            {:tid "1999" :gjeldsvekst 100 :lonnsvekst 100} data))