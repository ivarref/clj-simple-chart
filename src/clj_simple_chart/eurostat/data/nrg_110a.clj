(ns clj-simple-chart.eurostat.data.nrg-110a
  (:require [clj-simple-chart.eurostat.data.pull-eurostat :as pull]))

(defonce data (pull/pull "nrg_110a"))

