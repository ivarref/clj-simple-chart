(ns clj-simple-chart.bilstorleik)

; https://twitter.com/ulriken1/status/1732736335226507347
; Norges mest solgte bil 1992 og 2022.
; https://www.carsized.com/en/cars/compare/volkswagen-golf-1983-5-door-hatchback-vs-tesla-model-y-2021-suv/

(comment
  (- 2022 1992))

(comment
  (let [w1 170
        w2 197.8]
    ; w1*x^30 = w2
    ; x^30 = w2/w1
    ; log(x^30) = log(w2/w1)
    ; 30log(x) = log(w2/w1)
    ; log(x) = log(w2/w1)/30
    ; x = e^(log(w2/w1)/30)
    (Math/pow Math/E
              (/ (- (Math/log w2) (Math/log w1))
                 30))))

(defn extrapolate
  [a b n]
  (let [pow (Math/pow Math/E
                      (/ (- (Math/log b) (Math/log a))
                         30))]
    (* a (Math/pow pow n))))

(defn extrapolate2
  [a b]
  (let [pow (Math/pow Math/E
                      (/ (- (Math/log b) (Math/log a))
                         4))]
    pow))

(comment
  (extrapolate 1.70 1.978 60)) ; høgde (m)

(comment
  (extrapolate 4.05 4.75 60)) ; lengde (m)

(comment
  (extrapolate 0.865 1.909 (+ 30 30))) ; vekt (tonn)


(comment
  (* 170 (Math/pow 1.005 30)))


