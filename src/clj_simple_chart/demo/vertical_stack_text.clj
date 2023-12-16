(ns clj-simple-chart.demo.vertical-stack-text
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]))

(def header (opentype/text-stack-downwards
              {:margin-top  15.5
               :margin-left 15.5
               :margin-bottom 3.0}
              [{:text "Hello World" :border-tight "none" :font "Roboto Bold" :font-size 36}
               {:text "It's going to stack downwards..." :border-tight "none" :font "Roboto Black" :font-size 16}
               {:text "Just like that!" :border-tight "none" :font-size 16}]))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width 250 :height 500}
   header
   [:g {:transform (translate 0 (:height (meta header)))}
    [:line {:stroke "black" :x1 0 :x2 250}]]])

(def _render-self
  (render #_"./img/vertical-stack-text.png" "./img/vertical-stack-text.svg" (diagram)))
