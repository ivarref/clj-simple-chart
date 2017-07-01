(ns clj-simple-chart.bp.diagrams.render-all)

(defn render-all []
  (clj-simple-chart.bp.diagrams.co-per-capita-sel/render-self)
  (clj-simple-chart.bp.diagrams.gas-consumption-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.gas-production-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.oil-consumption-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.oil-net-imports/render-self)
  (clj-simple-chart.bp.diagrams.oiloil-production-per-capita/render-self)
  (clj-simple-chart.bp.diagrams.renewables-share/render-self)
  (clj-simple-chart.bp.diagrams.renewables-share-sel/render-self)
  )