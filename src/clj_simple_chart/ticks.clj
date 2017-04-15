(ns clj-simple-chart.ticks)

; The following code is ported from d3-array:
; Original source: https://github.com/d3/d3-array/blob/master/src/ticks.js

(def e10 (Math/sqrt 50))
(def e5 (Math/sqrt 10))
(def e2 (Math/sqrt 2))
(def ln10 (Math/log 10))

(defn tick-increment [start stop count]
  (let [step (/ (- stop start) (max 0 count))
        power (Math/floor (/ (Math/log step) ln10))
        error (/ step (Math/pow 10 power))]
    (if (>= power 0)
      (* (Math/pow 10 power) (cond (>= error e10) 10
                                   (>= error e5) 5
                                   (>= error e2) 2
                                   :else 1))
      (/ (- (Math/pow 10 (- power))) (cond (>= error e10) 10
                                           (>= error e5) 5
                                           (>= error e2) 2
                                           :else 1)))))

(defn ticks
  ([start stop] (ticks start stop 10))
  ([start stop count]
  (if (< stop start)
    (reverse (ticks stop start count))
    (let [step (tick-increment start stop count)]
      (cond (or (Double/isInfinite step) (= 0 step)) []
            (> step 0)
            (let [sstart (Math/ceil (/ start step))
                  sstop (Math/floor (/ stop step))
                  n (Math/ceil (inc (- sstop sstart)))]
              (mapv #(* (+ sstart %) step) (range 0 n)))
            :else
            (let [sstart (Math/floor (* start step))
                  sstop (Math/ceil (* stop step))
                  n (Math/ceil (inc (- sstart sstop)))]
              (mapv #(/ (- sstart %) step) (range 0 n))))))))
