(ns clj-simple-chart.ncs.update-monthly
  (:require [clj-simple-chart.ncs.liquidprodpp.liquid-pp-diagram-two]
            [clj-simple-chart.ncs.gas-rp-diagram]
            [clj-simple-chart.ncs.petroprodpp.petroprodppdiagram]))


(def namespaces '[clj-simple-chart.ncs.liquidprodpp.liquid-pp-diagram-two
                  clj-simple-chart.ncs.gas-rp-diagram
                  clj-simple-chart.ncs.petroprodpp.petroprodppdiagram])

(defn update-monthly []
  (doseq [ns namespaces]
    (let [f (resolve (symbol (str ns) "render-self"))]
      (println "doing" ns "...")
      (f))))
