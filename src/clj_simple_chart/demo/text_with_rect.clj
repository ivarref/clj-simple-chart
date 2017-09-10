(ns clj-simple-chart.demo.text-with-rect
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate]]))

(def svg-width 250)
(def svg-height 250)

(def font-size 25)

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate 10 10)}
    (opentype/stack {}
                    [{:text "Abc" :font "Roboto Black" :font-size font-size :rect {:fill "red"}}
                     {:text "Def" :font "Roboto Black" :font-size font-size :rect {:fill "green"}}
                     {:text "Ghi" :font "Roboto Black" :font-size font-size :rect {:fill "orange"}}
                     {:text "Sphinx" :font "Roboto Black" :font-size font-size :rect {:fill "yellow"}}
                     {:text "Jklll" :font "Roboto Black" :font-size font-size :rect {:fill "brown"}}
                     ])
    ]])

(defn render-self []
  (render "./img/demo/meh.png" "./img/demo/meh.svg" (diagram)))
