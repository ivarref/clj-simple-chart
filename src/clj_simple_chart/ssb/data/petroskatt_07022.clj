(ns clj-simple-chart.ssb.data.petroskatt-07022
  (:require [clj-simple-chart.ssb.data.ssb-fetch :as sf]
            [clojure.set :as set]
            [clojure.string :as str]))

(def q {"Region"       "*"
        "Skatteart"    ["Ordinær skatt på utvinning av petroleum"
                        "Særskatt på utvinning av petroleum"
                        "Skatteinngang i alt"]
        "ContentsCode" "Skatt"
        "Tid"          "*"})

(def data (->> (sf/pull-parse-cached "07022" q)
               (map #(set/rename-keys % {:Skatt :value :Tid :dato}))
               (map #(update % :dato (fn [d] (str/replace d "M" "-"))))))

