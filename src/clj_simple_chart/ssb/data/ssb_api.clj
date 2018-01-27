(ns clj-simple-chart.ssb.data.ssb-api
  (:require [clj-simple-chart.ssb.data.ssb-pull :as pull]
            [clj-simple-chart.ssb.data.ssb-parse :as parse]))

(defn- fetch-inner [table qq]
  (->> (pull/pull table qq)
       (parse/pulled->parsed table)))

(def fetch (memoize fetch-inner))

(defn- dev [] (fetch-inner 11174 {"ContentsCode"  "Salg"
                                  "Region"        "Hele landet"
                                  "PetroleumProd" ["Autodiesel" "Bilbensin"]
                                  "Kjopegrupper"  "Alle kjøpegrupper"
                                  "Tid"           "*"}))