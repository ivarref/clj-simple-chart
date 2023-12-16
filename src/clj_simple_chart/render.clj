(ns clj-simple-chart.render
  (:require [clj-simple-chart.jfx :as jfx]))

(defonce last-fn (atom nil))

(defn render-fn [f]
  (reset! last-fn f)
  (jfx/export-to-file "temp.svg" (f)))

(defn re-render []
  (try
    (when-let [f @last-fn]
      (println "re-rendering")
      (jfx/export-to-file "temp.svg" (f)))
    (catch Throwable t
      (binding [*out* *err*]
        (println "Error during re-render:" (ex-message t))))))
