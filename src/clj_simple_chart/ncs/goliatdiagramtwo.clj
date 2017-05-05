(ns clj-simple-chart.ncs.goliatdiagramtwo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ncs.goliat :as goliat]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]))

(def marg 9.5)

(def header (opentype/stack-downwards-texts
              {:margin-top    marg
               :margin-left   marg
               :margin-bottom 35.0}
              [{:text "Goliat — Ein ekte kjempe?" :font "Roboto Black" :font-size 24}
               {:text "Dei ti største oljefelta, pluss Goliat" :font "Roboto Bold" :font-size 14}
               {:text "Opprinneleg utvinnbart, millionar fat olje" :font-size 14}]))

(def data goliat/top-ten-plus-goliat)
(def domain (mapv :fldName data))

; https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions
; iPhone 4, 4s
(def svg-width 320)
(def svg-height 480)

(def margin {:top    (:height (meta header))
             :bottom (+ marg (+ 7 14))
             :left   (round (+ marg 6 (axis/domain-max-width domain)))
             :right  (round (+ marg (/ (axis/domain-max-width ["4000"]) 2)))})

(def chart-width (round (- svg-width (:left margin) (:right margin))))
(def chart-height (round (- svg-height (:top margin) (:bottom margin))))

(def x (scale {:type        :linear
               :axis        :x
               :orientation :top
               :ticks       5
               :width       chart-width
               :height      chart-height
               :domain      [0 4000]}))

(def y (scale {:type          :ordinal
               :axis          :y
               :domain        domain
               :orientation   :left
               :reverse       true
               :width         chart-width
               :padding-inner 0.5
               :padding-outer 0.3
               :height        chart-height}))

(def rect (rect/scaled-rect x y))

(defn make-rect [{fldName :fldName
                  oil     :fldRecoverableOil}]
  {:p fldName :h oil :fill "steelblue"})

(defn recoverable-oil-text [{fldName :fldName
                  oil                :fldRecoverableOil}]
  (opentype/text {:x         (point x oil)
                  :dx        ".20em"
                  :y         (center-point y fldName)
                  :dy        ".32em"
                  :font-size 14}
                 oil))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   header
   [:g {:transform (translate (:left margin) (:top margin))}
    (render-axis x)
    (rect (mapv make-rect data))
    (render-axis y)
    (map recoverable-oil-text data)]
   [:g {:transform (translate marg (- svg-height marg))}
    (opentype/text {:text "Kjelde: OD"})]
   [:g {:transform (translate (- svg-width marg) (- svg-height marg))}
    (opentype/text {:text "Refsdal.Ivar@gmail.com" :text-anchor "end"})]
   ])

(defn render-self []
  (render "./img/goliat-two.png" "./img/goliat-two.svg" (diagram)))