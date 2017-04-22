(ns clj-simple-chart.demo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.rect :as rect]))

(def margin {:top 75 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(def x (scale {:type          :ordinal
               :axis          :x
               :orientation   :bottom
               :width         width
               :height        height
               :domain        [1990 1991 1992 1993]
               :padding-inner 0.1
               :padding-outer 0.1}))

(def y (scale {:type        :linear
               :axis        :y
               :grid        true
               :orientation :left
               :ticks       5
               :width       width
               :height      height
               :domain      [0 100]}))

(def rect (rect/scaled-rect x y))

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Some values")
   (sub-title "1990 - 1995")
   [:g {:transform (translate (:left margin) (:top margin))}
    (render-axis y)
    (render-axis x)
    (rect [{:x 1990 :h 30 :fill "red"}
           {:x 1990 :h 20 :fill "green"}
           {:x 1990 :h 10 :fill "blue"}])
    (rect [{:x 1991 :h 10 :fill "red"}
           {:x 1991 :h 10 :fill "green"}
           {:x 1991 :h 10 :fill "blue"}])
    (rect [{:x 1992 :h 15 :fill "red"}
           {:x 1992 :h 17 :fill "green"}
           {:x 1992 :h 15 :fill "blue"}])
    (rect [{:x 1993 :h 25 :fill "red"}
           {:x 1993 :h 19 :fill "green"}
           {:x 1993 :h 19 :fill "blue"}])
    #_(rect {:x 1991 :h 35})
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