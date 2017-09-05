(ns clj-simple-chart.demo.areachart
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.translate :refer [translate]]))

(def marg 0)
(def two-marg (* 2 marg))

(def svg-width 250)
(def svg-height 250)

(def available-width (- svg-width (* 2 marg)))
(def available-height (- svg-height (* 2 marg)))

(def xx {:type        :ordinal-linear
         :orientation :bottom
         :domain      ["a" "b" "c"]})

(def yy {:type        :linear
         :orientation :right
         :domain      [0 50]})

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def xfn (partial point/center-point (:x c)))
(def yfn (partial point/center-point (:y c)))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate (+ (:margin-left c) marg) (+ (:margin-top c) marg))}
    (axis/render-axis (:x c))
    (axis/render-axis (:y c))
    [:g {:transform (translate (xfn "a") (yfn 10))} [:circle {:r 10 :fill "blue"}]]
    [:g {:transform (translate (xfn "b") (yfn 10))} [:circle {:r 10 :fill "red"}]]
    [:g {:transform (translate (xfn "c") (yfn 10))} [:circle {:r 10 :fill "green"}]]]])

(defn render-self []
  (render "./img/demo/meh.png" "./img/demo/meh.svg" (diagram)))
