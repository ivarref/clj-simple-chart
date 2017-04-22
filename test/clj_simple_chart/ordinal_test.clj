(ns clj-simple-chart.ordinal-test
  (:require [clojure.test :refer :all]
            [clj-simple-chart.scale.core :refer :all]))

(deftest ordinal-test
  (testing "Ordinal scales 1"
    (let [growing-range (scale {:type   :ordinal
                                :axis   :x
                                :domain [1 2 3 4]
                                :width  100
                                :height 100})
          shrinking-range (scale {:type   :ordinal
                                  :axis   :y
                                  :domain [1 2 3 4]
                                  :width  100
                                  :height 100})
          growing-range (:point-fn growing-range)
          shrinking-range (:point-fn shrinking-range)]
      (is (= (growing-range 1) 0.0))
      (is (= (growing-range 2) 25.0))
      (is (= (growing-range 3) 50.0))
      (is (= (growing-range 4) 75.0))
      (is (= (shrinking-range 1) 75.0))
      (is (= (shrinking-range 2) 50.0))
      (is (= (shrinking-range 3) 25.0))
      (is (= (shrinking-range 4) 0.0)))))

(deftest ordinal-round-test
  (testing "Ordinal round test"
    (let [s (scale {:type :ordinal
                    :axis :x
                    :domain [1 2 3]
                    :round true
                    :width 100
                    :height 100})
          f (:point-fn s)]
      (is (= (f 1) 1.0))
      (is (= (f 2) 34.0))
      (is (= (f 3) 67.0)))))