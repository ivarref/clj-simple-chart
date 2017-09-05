(ns clj-simple-chart.scale.ordinal-linear)

; ordinal-linear (what a name):
; The goal is to map ordinal values to a linear range

(defn scale-ordinal-linear
  [{domain :domain
    rng    :range
    :as    config}]
  (let [start (apply min rng)
        reverse-values (apply > rng)
        stop (apply max rng)
        n (count domain)
        step (/ (- stop start) (dec (double n)))
        values (mapv (fn [i] (+ start (* i step))) (range 0 n))
        values (if reverse-values (reverse values) values)
        mapp (zipmap domain values)
        point-fn (fn [x]
                   (let [v (get mapp x ::none)]
                     (if (not= v ::none)
                       v
                       (throw (ex-info (str "Input value >" x "< for scale-ordinal-linear is not in scale's domain")
                                       {:value x})))))]
    (merge config {:point-fn point-fn})))
