(ns clj-simple-chart.ncs.goliatdiagram
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.ncs.goliat :as goliat]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]))

(def data goliat/top-ten-plus-goliat)

(def domain (mapv :fldName data))

(def marg 15)

(def margin {:top    100
             :bottom marg
             :left   (Math/round (+ marg 6 (axis/domain-max-width domain)))
             :right  (Math/round (+ marg (/ (axis/domain-max-width ["4000"]) 2)))})

(def width (Math/round (double (- 512 (:left margin) (:right margin)))))
(def height (Math/round (double (- 400 (:top margin) (:bottom margin)))))

(def x (scale {:type        :linear
               :axis        :x
               :orientation :top
               :width       width
               :height      height
               :domain      [0 4000]}))

(def y (scale {:type          :ordinal
               :axis          :y
               :domain        domain
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
  (opentype/text {:x         (point x oil)
                  :dx        ".20em"
                  :y         (center-point y fldName)
                  :dy        ".32em"
                  :font-size 12}
                 oil))

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
  (render "goliat.png" "goliat.svg" (diagram)))