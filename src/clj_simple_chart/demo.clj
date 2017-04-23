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
               :sub-domain    ["cats" "dogs" "birds"]
               :fill          ["red" "green" "blue"]
               :stack         :sideways
               :stack-opts    {:padding-inner 0.1}
               :padding-inner 0.2
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

(def rects
  [{:p 1990 :c "dogs" :h 35}
   {:p 1990 :c "cats" :h 25}
   {:p 1990 :c "birds" :h 25}

   {:p 1991 :c "cats" :h 25}
   {:p 1991 :c "dogs" :h 25}
   {:p 1991 :c "birds" :h 25}

   {:p 1992 :c "birds" :h 55}

   {:p 1993 :c "birds" :h 25}
   {:p 1993 :c "dogs" :h 25}
   {:p 1993 :c "cats" :h 25}])

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Some values")
   (sub-title "1990 - 1995")
   [:g {:transform (translate (:left margin) (:top margin))}
    (render-axis y)
    (render-axis x)
    (rect rects)]])

(defn render-self []
  (render "hello.svg" (diagram)))