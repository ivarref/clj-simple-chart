(ns clj-simple-chart.demo.text-right
  (:require [clj-simple-chart.core :refer :all]
    [clj-simple-chart.opentype :as opentype]
    [clj-simple-chart.translate :refer [translate]]))

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
    (opentype/stack {:fill "#ccc"
                     :fill-opacity 0.7
                     :margin 5}
                    [{:text "Abc" :font "Roboto Bold" :font-size font-size :rect {:fill "red" :size rect-size}
                      :right {:text "123"}}

                     {:text "Def" :font "Roboto Bold" :font-size font-size :rect {:fill "green" :size rect-size}
                      :right {:text "999"}}

                     {:text "Ghi" :font "Roboto Bold" :font-size font-size :rect {:fill "orange" :size rect-size}}
                     {:text "Ip,k" :font "Roboto Bold" :font-size font-size :rect {:fill "cyan" :size rect-size}
                      :right {:text "oo"}}
                     {:text "Oq,MG" :font "Roboto Bold" :font-size font-size :rect {:fill "yellow" :size rect-size}
                      :right {:text "oo"}}])]])

(defn render-self []
  (render "./img/demo/meh.png" "./img/demo/meh.svg" (diagram)))
