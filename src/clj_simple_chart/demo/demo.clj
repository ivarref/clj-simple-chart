(ns clj-simple-chart.demo.demo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.render :as r]
            [clj-simple-chart.rect :as rect]
            [demo.refresh :as refresh]))

(def margin {:top 75 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(refresh/set-focus! *ns*)

(defn x []
  (scale {:type          :ordinal
          :axis          :x
          :orientation   :bottom
          :width         width
          :height        height
          :domain        [1990 1991 1992 1993]
          :sub-domain    ["cats" "dogs" "birds"]
          :fill          ["red" "green" "blue"]
          :stack         :veritcal
          :stack-opts    {:padding-inner 0.1}
          :rough         {:fillStyle "zigzag"
                          :stroke    "black"}
          :padding-inner 0.2
          :padding-outer 0.1}))

(defn y []
  (scale {:type        :linear
          :axis        :y
          :grid        true
          :orientation :left
          :ticks       5
          :width       width
          :height      height
          :domain      [-100 100]}))

(def rects
  [
   {:p 1990 :c "dogs" :h 35}
   {:p 1990 :c "cats" :h 25}
   {:p 1990 :c "birds" :h 25}

   {:p 1991 :c "cats" :h -25}
   {:p 1991 :c "dogs" :h -25}
   {:p 1991 :c "birds" :h -25}

   {:p 1992 :c "cats" :h -25}
   {:p 1992 :c "dogs" :h -25}
   {:p 1992 :c "birds" :h 25}])

(defn diagram []
  [:svg (svg-attrs width height margin)
   [:g {:transform (translate (:left margin) (:top margin))}
    (let [x (x)
          y (y)]
      [:g
       (render-axis y)
       (render-axis x)
       (rect/rect-or-stacked-vertical x y rects)])]])

(def _render-self (r/render-fn (fn [] (diagram))))
