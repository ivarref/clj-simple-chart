(ns clj-simple-chart.bp.render-all
  (:require [clj-simple-chart.bp.diagrams.co-per-capita-sel]
            [clj-simple-chart.bp.diagrams.gas-consumption-per-capita]
            [clj-simple-chart.bp.diagrams.gas-production-per-capita]
            [clj-simple-chart.bp.diagrams.oil-consumption-per-capita]
            [clj-simple-chart.bp.diagrams.oil-net-imports]
            [clj-simple-chart.bp.diagrams.oiloil-production-per-capita]
            [clj-simple-chart.bp.diagrams.renewables-share]
            [clj-simple-chart.bp.diagrams.renewables-share-sel]
            [clj-simple-chart.bp.diagrams.oil-rp]
            [clj-simple-chart.bp.diagrams.gas-rp]))

(defn render-all []
  (clj-simple-chart.bp.diagrams.co-per-capita-sel/render-self)
  (clj-simple-chart.bp.diagrams.gas-consumption-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.gas-production-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.oil-consumption-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.oil-net-imports/render-self)
  (clj-simple-chart.bp.diagrams.oiloil-production-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.renewables-share/render-self)
  (clj-simple-chart.bp.diagrams.renewables-share-sel/render-self)
  (clj-simple-chart.bp.diagrams.oil-rp/render-self)
  (clj-simple-chart.bp.diagrams.gas-rp/render-self))