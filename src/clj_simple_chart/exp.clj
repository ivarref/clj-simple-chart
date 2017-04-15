(ns clj-simple-chart.exp
  (:require [clj-simple-chart.core :as sc]
            [clj-simple-chart.headless :as headless])
  (:import (javafx.application Platform)))

(defn -main
  []
  (println "hello from c.exp.-main")
  (headless/init-headless)
  (sc/render-to-file "hello.png" sc/diagram)
  (sc/exit))
