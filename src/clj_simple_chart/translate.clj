(ns clj-simple-chart.translate)

(defn translate [x y]
  {:pre [(number? x)
         (number? y)]}
  (str "translate(" (double x) "," (double y) ")"))

(defn translate-y [y]
  {:pre [(number? y)]}
  (translate 0 y))