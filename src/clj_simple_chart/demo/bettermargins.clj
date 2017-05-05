(ns clj-simple-chart.demo.bettermargins
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ncs.goliat :as goliat]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]))

(def marg 9.5)

(def header (opentype/text-stack-downwards
              {:margin-top  marg
               :margin-left marg}
              [{:text "Hello World" :font "Roboto Black" :font-size 24}
               {:text "Here goes my diagram" :font "Roboto Bold" :font-size 14}]))

; https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions
; iPhone 4, 4s
(def svg-width 320)
(def svg-height 480)

(def x (scale {:type             :linear
               :axis             :x
               :orientation      :top
               :color            "red"
               :ticks            5
               :width            100
               :height           100
               ; TODO: Implement:
               :available-width  (- svg-width (* 2 marg))
               :available-height (- svg-height (:height (meta header)))
               :domain           [0 10]}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   header
   [:g {:transform (translate marg (:height (meta header)))}
    (render-axis x)]])

(defn render-self []
  (render "./img/better-margins.svg" (diagram)))