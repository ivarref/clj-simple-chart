(ns clj-simple-chart.demo.text-with-rect
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            #_[clj-simple-chart.translate :refer [translate]]))

(def svg-width 250)
(def svg-height 250)

(def font-size 25)
(def rect-size nil)

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:rect {:width "100%"
           :height "100%"
           :fill "#ffaa00"
           :fill-opacity 0.5}]
   [:g {:transform (translate 10 10)}
    ;[:path {:d "M0 0 V250"
    ;        :fill "none"
    ;        :stroke "red"
    ;        :stroke-width "1px"}]
    ;[:path {:d "M23 0 V250"
    ;        :fill "none"
    ;        :stroke "red"
    ;        :stroke-width "1px"}]

    (opentype/stack {:fill "#ccc"
                     :fill-opacity 0.7
                     :margin 5}
                    [
                     {:text "My great heading" :font "Roboto Black" :font-size font-size}
                     ;{:text "It's great" :font "Roboto Black" :font-size 16}
                     {:text "\"It's really great\", -Maja" :font "Roboto Black" :font-size 16}

                     {:text "Abc" :font "Roboto Bold" :font-size font-size :rect {:fill "red" :size rect-size}}
                     {:text "Def" :font "Roboto Bold" :font-size font-size :rect {:fill "green" :size rect-size}}
                     {:text "Ghi" :font "Roboto Bold" :font-size font-size :rect {:fill "orange" :size rect-size}}
                     {:text "Ijk" :font "Roboto Bold" :font-size font-size :rect {:fill "cyan" :size rect-size}}
                     {:text "OMLLLLL" :font "Roboto Bold" :font-size font-size :rect {:fill "yellow" :size rect-size}}])]])

(def _render-self
  (render #_"./img/demo/meh.png" "./img/demo/meh.svg" (diagram)))
