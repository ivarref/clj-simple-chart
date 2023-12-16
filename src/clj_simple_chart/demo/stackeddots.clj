(ns clj-simple-chart.demo.stackeddots
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.dots :as clj-dots]))

(def marg 0)
(def two-marg (* 2 marg))

(def svg-width 250)
(def svg-height 250)

(def available-width (- svg-width (* 2 marg)))
(def available-height (- svg-height (* 2 marg)))

(def xx {:type        :ordinal-linear
         :orientation :bottom
         :domain      ["2000" "2001" "2002" "2003" "2004"]
         :sub-domain  ["a" "b" "c" "d"]
         :fill        {"a" "red"
                       "b" "blue"
                       "c" "green"}})

(def yy {:type        :linear
         :orientation :right
         :domain      [-50 50]})

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def x (:x c))

(def my-great-data
  [{:p "2000" :c "a" :h 10}
   {:p "2000" :c "b" :h 20}
   {:p "2000" :c "c" :h 10}

   {:p "2001" :c "a" :h 5}
   {:p "2001" :c "b" :h 10}
   {:p "2001" :c "c" :h 5}

   {:p "2002" :c "a" :h 10}
   {:p "2002" :c "b" :h 15}
   {:p "2002" :c "c" :h 10}

   {:p "2003" :c "a" :h -10} ; red correctly on bottom
   {:p "2003" :c "b" :h 10}
   {:p "2003" :c "c" :h 10}

   {:p "2004" :c "a" :h 25}
   {:p "2004" :c "b" :h -10} ; green
   {:p "2004" :c "c" :h 15}])

(def xfn (partial point/center-point (:x c)))
(def yfn (partial point/center-point (:y c)))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate (+ (:margin-left c) marg) (+ (:margin-top c) marg))}
    (axis/render-axis (:x c))
    (axis/render-axis (:y c))
    (clj-dots/dots c my-great-data)]])

(def _render-self (render "./img/demo/meh.svg" (diagram)))
