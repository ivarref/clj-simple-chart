(ns clj-simple-chart.band-test
  (:require [clojure.test :refer :all]
            [clj-simple-chart.band :refer :all]))

(deftest band-test
  (testing "Band scales 1"
    (let [growing-range (scale-band {:domain [1 2 3]
                                     :range  [0 100]
                                     })
          shrinking-range (scale-band {:domain [1 2 3]
                                       :range  [100 0]
                                       })]
      (is (= (growing-range 1) (shrinking-range 3)))
      ; var s = require('d3-scale');
      ; var x = require('d3-scale').scaleBand().domain([1, 2, 3]).range([0, 100]);

      )))

