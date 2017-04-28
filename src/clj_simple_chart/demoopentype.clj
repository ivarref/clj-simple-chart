(ns clj-simple-chart.demoopentype
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]))

(def margin {:top 75 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(defn diagram []
  [:svg (svg-attrs width height margin)
   [:path {:d (opentype/get-path-data "Roboto Black"
                                      "Opentype.js integration on the JVM is working!"
                                      10
                                      30
                                      23)}]
   [:path {:d (opentype/get-path-data "Roboto Regular"
                                      "Wow — It just works!"
                                      10
                                      70
                                      30)}]
   ])

(defn render-self []
  (render "hello.svg" (diagram)))