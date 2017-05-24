(ns clj-simple-chart.demo.boxdemo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.opentype :as opentype]))

(def svg-width 320)
(def svg-height 240)

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:rect {:width "100%" :height "100%" :fill "#ffaa00"}]
   (opentype/stack {:width svg-width}
                   [{:text "Hello world"}
                    {:text "Hei Verden"}
                    {:text "Swoosh" :margin-top 20}

                    {:text "Hello world" :valign :bottom :align :right}
                    {:text "Yolo!" :valign :bottom :align :right}])])

(defn render-self []
  (render "./img/boxdemo.svg" (diagram)))