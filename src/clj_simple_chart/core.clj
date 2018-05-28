(ns clj-simple-chart.core
  (:require [clojure.string :as string]
            [clj-simple-chart.jfx :as jfx]
            [clj-simple-chart.point :as ppoint]
            [clj-simple-chart.scale.core :as scale-core]
            [clj-simple-chart.axis.core :as axis-core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.axis.core :as axis]))

(def scale scale-core/scale)

(defn round [x]
  (Math/round (double x)))

(defn render [& args]
  (apply jfx/render args))

(defn render-axis [& args]
  (apply axis-core/render-axis args))

(defn point [& args]
  (apply ppoint/point args))

(defn center-point [& args]
  (apply ppoint/center-point args))

(defn svg-attrs
  ([width height]
   {:width  (round width)
    :height (round height)
    :xmlns  "http://www.w3.org/2000/svg"})
  ([width height margin]
   {:width  (+ (:left margin) (:right margin) width)
    :height (+ (:top margin) (:bottom margin) height)
    :xmlns  "http://www.w3.org/2000/svg"}))

(defn path [points]
  (str "M"
       (string/join " L" points)))

(defn line
  ([points]
   (line {:fill "none" :stroke "#000" :stroke-width 1} points))
  ([props points]
   [:path (assoc props :d (path (map (fn [[x y]] (str (.doubleValue x) "," (.doubleValue y))) points)))]))

(defn text [& txts]
  (apply opentype/stack txts))

(defn chart [config]
  (chart/chart config))

(defn translate [x y]
  {:pre [(number? x)
         (number? y)]}
  (str "translate(" (double x) "," (double y) ")"))

(defn translate-y [y]
  {:pre [(number? y)]}
  (translate 0 y))

(defn bars [& args]
  (apply rect/bars args))

(defn axis [ax]
  (axis/render-axis ax))