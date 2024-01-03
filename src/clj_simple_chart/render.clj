(ns clj-simple-chart.render
  (:require [clj-simple-chart.jfx :as jfx]))

(defonce last-fn (atom nil))

(defn render-fn [f]
  (reset! last-fn f)
  (jfx/export-to-file "temp.svg" (f)))

