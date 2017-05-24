(ns clj-simple-chart.demoopentype
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]))

(defn bounding-box-lines [bb margin stroke]
  [:g
   [:line {:x1 (- (:x1 bb) margin) :x2 (+ (:x2 bb) margin) :y1 (- (:y1 bb) margin) :y2 (- (:y1 bb) margin) :stroke stroke}]
   [:line {:x1 (- (:x1 bb) margin) :x2 (+ (:x2 bb) margin) :y1 (+ (:y2 bb) margin) :y2 (+ (:y2 bb) margin) :stroke stroke}]
   [:line {:x1 (- (:x1 bb) margin) :x2 (- (:x1 bb) margin) :y1 (- (:y1 bb) margin) :y2 (+ (:y2 bb) margin) :stroke stroke}]
   [:line {:x1 (+ (:x2 bb) margin) :x2 (+ (:x2 bb) margin) :y1 (- (:y1 bb) margin) :y2 (+ (:y2 bb) margin) :stroke stroke}]])

(def txt-with-args ["Roboto Regular" "Wow — It Just Works™!" 10 70 30])

(defn diagram []
  [:svg {:width 510 :height 256}
   [:path {:d (opentype/get-path-data "Roboto Black"
                                      "Opentype.js integration on the JVM is working!"
                                      10
                                      30
                                      23)}]
   [:path {:d (apply opentype/get-path-data txt-with-args)}]
   (bounding-box-lines (apply opentype/get-bounding-box txt-with-args) 5 "red")])

(defn render-self []
  (render "hello.svg" (diagram)))