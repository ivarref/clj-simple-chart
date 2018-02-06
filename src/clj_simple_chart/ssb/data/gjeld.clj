(ns clj-simple-chart.ssb.data.gjeld
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clojure.set :as set]
            [clojure.string :as str]))

(def q {"Valuta"       "I alt"
        "Lantaker2"    "Husholdninger mv."
        "ContentsCode" "Transaksjoner, tolvmånedersvekst (prosent)"
        "Tid"          "*"})

(def data (->> (ssb/fetch 11599 q)
               (remove-nils)
               (filter #(str/ends-with? (:Tid %) "-12"))))
