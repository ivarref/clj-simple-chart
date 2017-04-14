(ns clj-simple-chart.exp
  (:require [clj-simple-chart.core :as sc]))

(def width 1200)
(def height 500)

(sc/render-tag [:svg {:width width :height height}
                [:circle {:cx 50 :cy 100 :r 10 :fill "yellow" :stroke "black"}]
                [:circle {:cx 50 :cy 100 :r 1 :fill "yellow" :stroke "black"}]
                [:line {:x1 50 :y1 100 :x2 width :y2 100 :stroke "red"}]
                [:text {:x 50 :y 100 :dy ".335em" :font-size "200px"} "1,234567890"]
                ])
