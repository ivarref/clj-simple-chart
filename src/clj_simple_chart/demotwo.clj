(ns clj-simple-chart.demotwo
  (:require [clj-simple-chart.core :refer :all]))

(def margin {:top 60 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

#_(def x (scale {:type          :ordinal
               :axis          :x
               :width         width
               :height        height
               :domain        (range 2000 (inc 2017) 1)
               :padding-inner 0.1
               :padding-outer 0.1}))

#_(def y (scale {:type   :linear
               :axis   :y
               :width  width
               :height height
               :domain [0 100]}))

#_(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Some values")
   (sub-title "1990 - 1995")])

#_(defn render-self []
  (render "hello.scale" (diagram)))