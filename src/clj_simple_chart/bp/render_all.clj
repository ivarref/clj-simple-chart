(ns clj-simple-chart.bp.render-all
  (:import (java.io File)))

(def files (->> (file-seq (File. "src/clj_simple_chart/bp/diagrams"))
                (filter #(.endsWith (.getName %) ".clj"))))

(defn render-file [fil]
  (let [loaded-file (load-file (.getAbsolutePath fil))
        loaded-ns (:ns (meta loaded-file))
        resolved-fn (ns-resolve loaded-ns (symbol "render-self"))]
    (resolved-fn)
    (println "Executed" resolved-fn "OK")))

(defn render-all []
  (doall (mapv render-file files)))