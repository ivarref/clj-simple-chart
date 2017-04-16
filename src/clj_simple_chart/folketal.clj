(ns clj-simple-chart.folketal
  (:require [clj-simple-chart.core :refer :all]))

(def margin {:top 60 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(def dimensions
  {:width width
   :height height
   :margin margin})

(def y (scale-linear {:color "red" :domain [0 100] :range [height 0]}))
(def y2 (scale-linear {:color "blue" :domain [0 1.69] :range [height 0]}))
(def x (scale-linear {:color "green"
                      :ticks 5
                      :domain [1990 2016]
                      :range [0 width]}))
#_(def x-top (scale-linear {:color "fuchsia" :domain [0 8] :range [0 width]}))

(defn diagram
  []
  [:svg {:width  (+ (:left margin) (:right margin) width)
         :height (+ (:top margin) (:bottom margin) height)
         :xmlns  "http://www.w3.org/2000/svg"}
   (title "Folketal, Noreg")
   (sub-title "1990 - 2016")
   [:g {:transform (translate (:left margin) (:top margin))}
    #_[:g (left-y-axis y)]
    #_[:g {:transform (translate width 0)} (right-y-axis y2)]
    [:g {:transform (translate 0 height)} (bottom-x-axis x)]
    #_[:g (top-x-axis x-top)]
    (dotted-line {:fill   "yellow"
                  :stroke "black"}
                 (map (fn [d] [(x d) (y d)]) (range 0 (+ 10 100) 10)))]])
