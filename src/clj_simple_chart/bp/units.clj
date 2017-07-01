(ns clj-simple-chart.bp.units)

(defn ten-power [x]
  (if (= 0 x)
    1
    (* 10 (ten-power (dec x)))))

(def billion (ten-power 9))
(def trillion (ten-power 12))
(def million (ten-power 6))
