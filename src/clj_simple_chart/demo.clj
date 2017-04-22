(ns clj-simple-chart.demo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.axis :refer :all]))

(def margin {:top 60 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(def x (scale {:type          :ordinal
               :axis          :x
               :width         width
               :height        height
               :domain        (range 1990 (inc 1993) 1)
               :padding-inner 0.1
               :padding-outer 0.1}))

(def y (scale {:type   :linear
               :axis   :y
               :width  width
               :height height
               :domain [0 100]}))

(def rect (partial rect/vertical-rect x y))

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Some values")
   (sub-title "1990 - 1995")
   [:g {:transform (translate (:left margin) (:top margin))}
    [:g (left-y-axis y)]
    [:g {:transform (translate width 0)} (right-y-axis y)]
    (rect {:x 1990 :h 5})
    (rect {:x 1991 :h 25})
    (rect {:x 1992 :h 35})
    (rect {:x 1993 :h 50})
    [:g {:transform (translate 0 height)} (bottom-x-axis x)]]])

(defn render-self []
  (render "hello.scale" (diagram)))