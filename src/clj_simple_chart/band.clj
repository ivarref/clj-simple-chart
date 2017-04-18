(ns clj-simple-chart.band)

; based on
; https://github.com/d3/d3-scale/blob/master/src/band.js
; fd07dd8  on Jan 21, 2016

; var s = require('d3-scale');
; var x = s.scaleBand().domain([1, 2, 3]).range([0, 100]);

(defn scale-band
  [{domain        :domain
    rng           :range
    padding-inner :padding-inner
    padding-outer :padding-outer
    align         :align
    :or           {rng           [0 1]
                   padding-inner 0
                   padding-outer 0
                   align         0.5}
    :as           all}]
  (let [start (first rng)
        stop (second rng)
        n (count domain)
        step (/ (- stop start)
                (max 1 (+ (* 2 padding-outer)
                          (- n padding-inner))))
        start (+ start
                 (* align
                    (- stop start (* step (- n padding-inner)))))
        bandwidth (Math/abs (.doubleValue (* step (- 1 padding-inner))))
        values (mapv (fn [i] (+ start (* i step))) (range 0 n))
        mapp (zipmap (map #(.doubleValue %) domain) values)]
    (with-meta (fn [x]
                 (let [v (get mapp (.doubleValue x) ::none)]
                   (if (not= v ::none)
                     v
                     (throw (ex-info "Input value for scale band is not in scale's domain"
                                     {:value (.doubleValue x)})))))
               (-> all
                   (assoc :bandwidth bandwidth)
                   (assoc :scale-type :band)))))
