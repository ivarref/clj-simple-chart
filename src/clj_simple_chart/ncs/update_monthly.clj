(ns clj-simple-chart.ncs.update-monthly
  (:require [clj-simple-chart.ncs.liquidprodpp.liquid-pp-diagram-two]
            [clj-simple-chart.ncs.liquidprodpp.liquid-pp-forecast]
            [clj-simple-chart.ncs.gas-rp-diagram]
            [clj-simple-chart.ncs.petroprodpp.petroprodppdiagram]
            [clj-simple-chart.ssb.petroskatt.petroskattdiagrammonthly]
            [clj-simple-chart.ssb.nettokontantstraum.nettokontantstraumdiagram]
            [clj-simple-chart.ssb.diagrams.bensin-autodiesel]
            [clj-simple-chart.ssb.diagrams.koyrelengde-etter-kategori]
            [clj-simple-chart.ssb.diagrams.petroleumsprodukt-kjopegruppe]
            [clj-simple-chart.ssb.diagrams.petroleumsprodukt-type]))


(def namespaces '[clj-simple-chart.ncs.liquidprodpp.liquid-pp-diagram-two
                  clj-simple-chart.ncs.gas-rp-diagram
                  clj-simple-chart.ncs.liquidprodpp.liquid-pp-forecast
                  clj-simple-chart.ncs.petroprodpp.petroprodppdiagram
                  clj-simple-chart.ssb.petroskatt.petroskattdiagrammonthly
                  clj-simple-chart.ssb.nettokontantstraum.nettokontantstraumdiagram
                  clj-simple-chart.ssb.diagrams.bensin-autodiesel
                  clj-simple-chart.ssb.diagrams.koyrelengde-etter-kategori
                  clj-simple-chart.ssb.diagrams.petroleumsprodukt-kjopegruppe
                  clj-simple-chart.ssb.diagrams.petroleumsprodukt-type])


(defn render-self []
  (doseq [ns namespaces]
    (let [f (resolve (symbol (str ns) "render-self"))]
      (println "doing" ns "...")
      (f))))
