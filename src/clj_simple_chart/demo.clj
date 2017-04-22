(ns clj-simple-chart.demo
  (:require [clj-simple-chart.core :refer :all]))

(def margin {:top 75 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(def x (scale {:type          :ordinal
               :axis          :x
               :orientation   :both
               :width         width
               :height        height
               :grid          true
               :domain        (range 1990 (inc 1995) 1)
               :padding-inner 0.1
               :padding-outer 0.1}))

(def y (scale {:type        :linear
               :axis        :y
               :grid        true
               :orientation :both
               :width       width
               :height      height
               :domain      [0 100]}))

#_(def rect (partial rect/vertical-rect x y))

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Some values")
   (sub-title "1990 - 1995")
   [:g {:transform (translate (:left margin) (:top margin))}
    (render-axis y)
    (render-axis x)
    ;[:g (left-y-axis y)]
    ;[:g {:transform (translate width 0)} (right-y-axis y)]
    ;(rect {:x 1990 :h 5})
    ;(rect {:x 1991 :h 25})
    ;(rect {:x 1992 :h 35})
    ;(rect {:x 1993 :h 50})
    ;[:g {:transform (translate 0 height)} (bottom-x-axis x)]
    ]])

(defn render-self []
  (render "hello.scale" (diagram)))