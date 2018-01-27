(ns clj-simple-chart.ssb.data.sal-av-petroleumsprodukt-11174
  (:require [clj-simple-chart.ssb.data.ssb-pull :as sf]))

(def data (sf/pull-parse 11174 {"ContentsCode" "Salg"
                                "Region" "Hele landet"
                                "PetroleumProd" ["Autodiesel" "Bilbensin"]
                                "Kjopegrupper" "Alle kjøpegrupper"
                                "Tid" "*"}))
