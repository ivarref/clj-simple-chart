(ns clj-simple-chart.band-test
  (:require [clojure.test :refer :all]
            [clj-simple-chart.band :refer :all]))

(deftest band-test
  (testing "Band scales 1"
    (let [growing-range (scale-band {:domain [1 2 3 4]
                                     :range  [0 100]
                                     })
          shrinking-range (scale-band {:domain [1 2 3 4]
                                       :range  [100 0]
                                       })]
      (is (= (growing-range 1) 0.0))
      (is (= (growing-range 2) 25.0))
      (is (= (growing-range 3) 50.0))
      (is (= (growing-range 4) 75.0))

      (is (= (shrinking-range 1) 75.0))
      (is (= (shrinking-range 2) 50.0))
      (is (= (shrinking-range 3) 25.0))
      (is (= (shrinking-range 4) 0.0)))))

(deftest band-round-test
  (testing "Band round test"
    (let [s (scale-band {:domain [1 2 3]
                         :round  true
                         :range  [0 100]
                         })]
      (is (= (s 1) 1.0))
      (is (= (s 2) 34.0))
      (is (= (s 3) 67.0)))))