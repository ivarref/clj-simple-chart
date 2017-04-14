(ns clj-simple-chart.exp
  (:require [clj-simple-chart.core :as sc])
  (:import (javafx.application Platform)))

(defn -main
  []
  (println "waiting for tmr....")
  (.await sc/tmr)
  (println "file written as a side effect..!")
  (Platform/runLater (fn [] (.close @sc/stage))))
