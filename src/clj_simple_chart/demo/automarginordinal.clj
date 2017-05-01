(ns clj-simple-chart.demo.automarginordinal
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.axis.core :as axis]))

(def domain ["Peru"
             "Iraq"
             "Norway"
             "Sweden"
             "Australia"
             "Uzbekistan"
             "United States"
             ])

(def margin {:top    75
             :bottom 40
             :left   (+ 15 6 (axis/domain-max-width domain))
             :right  40})

(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(def y (scale {:type          :ordinal
               :axis          :y
               :orientation   :left
               :width         width
               :height        height
               :domain        domain
               :reverse       true
               :padding-inner 0.2
               :padding-outer 0.2}))

(def x (scale {:type        :linear
               :axis        :x
               :orientation :top
               :ticks       5
               :width       width
               :height      height
               :domain      [0 (apply max (map count domain))]}))

(def rect (rect/scaled-rect x y))

(def rects
  #_[{:p "Peru" :h 35}
     {:p "Iraq" :h 25}
     {:p "United States" :h 55}]
  (mapv (fn [x] {:p x :h (count x)}) domain)
  )

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Automatic margins")
   #_[:line {:x1 15 :x2 15
           :y1 0
           :y2 (:height (svg-attrs width height margin))
           :stroke "black"}]
   [:g {:transform (translate (:left margin) (:top margin))}
    (rect rects)
    (render-axis y)
    (render-axis x)]])

(defn render-self []
  (render "automargins.svg" (diagram)))
