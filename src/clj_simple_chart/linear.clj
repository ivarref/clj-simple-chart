(ns clj-simple-chart.linear)

(defn scale-linear
  [{domain :domain range :range :as all}]
  (->
    (fn [x]
      (let [domain-size (- (last domain) (first domain))
            domain-offset (- x (first domain))
            domain-relative (/ domain-offset domain-size)
            range-size (- (last range) (first range))
            range-output (+ (first range) (* domain-relative range-size))]
        range-output))
    (with-meta (-> all
                   (assoc :scale-type :linear)))))
