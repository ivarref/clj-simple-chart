(ns clj-simple-chart.core
  (:require [clojure.string :as string]
            [clj-simple-chart.jfx :as jfx]
            [clj-simple-chart.scale.core :as scale-core]
            [clj-simple-chart.ticks :as ticks]))

(def scale scale-core/scale)
(def render jfx/render)

(defn svg-attrs
  [width height margin]
  {:width  (+ (:left margin) (:right margin) width)
   :height (+ (:top margin) (:bottom margin) height)
   :xmlns  "http://www.w3.org/2000/svg"})

(defn translate [x y]
  (str "translate(" x "," y ")"))

(defn path [points]
  (str "M"
       (string/join " L" points)))

(defn line
  ([points]
   (line {:fill "none" :stroke "#000" :stroke-width 1} points))
  ([props points]
   [:path (assoc props :d (path (map (fn [[x y]] (str (.doubleValue x) "," (.doubleValue y))) points)))]))

(defn title
  [text]
  [:g [:text {:x                  15
              :y                  15
              :alignment-baseline "hanging"
              :font-family        "Arial"
              :font-size          "20px"
              :font-weight        "bold"}
       text]])

(defn sub-title
  [text]
  [:g [:text {:x                  15
              :y                  (+ 22 15)
              :alignment-baseline "hanging"
              :font-family        "Arial"
              :font-size          "14px"
              :font-weight        "normal"}
       text]])
