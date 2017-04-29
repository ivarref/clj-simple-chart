(ns clj-simple-chart.ncs.goliatdiagram
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.ncs.goliat :as goliat]
            [clj-simple-chart.rect :as rect]))

(def margin {:top 100 :bottom 40 :left 110 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- 400 (:top margin) (:bottom margin)))

(def data goliat/top-ten-plus-goliat)

(def x (scale {:type        :linear
               :axis        :x
               :orientation :top
               :width       width
               :height      height
               :domain      [0 (apply max (map :fldRecoverableOil data))]}))

(def y (scale {:type          :ordinal
               :axis          :y
               :domain        (map :fldName data)
               :orientation   :left
               :reverse       true
               :width         width
               :padding-inner 0.5
               :padding-outer 0.3
               :height        height}))

(def rect (rect/scaled-rect x y))

(defn make-rect [{fldName :fldName
                  oil     :fldRecoverableOil}]
  {:p fldName :h oil :fill "steelblue"})

(defn make-text [{fldName :fldName
                  oil     :fldRecoverableOil}]
  [:text {:x           (point x oil)
          :dx          ".20em"
          :y           (center-point y fldName)
          :dy          ".32em"
          :font-family "sans-serif"
          :font-size   "12px"}
   oil])

(defn diagram []
  [:svg (svg-attrs width height margin)
   (title "Goliat — Ein ekte kjempe?")
   (sub-title "Dei ti største oljefelta på norsk sokkel, pluss Goliat")
   (sub-sub-title "Opprinneleg utvinnbart, millionar fat")
   [:g {:transform (translate (:left margin) (:top margin))}
    (render-axis x)
    (rect (mapv make-rect data))
    (render-axis y)
    (map make-text data)]])

(defn render-self []
  (render "goliat.svg" (diagram)))