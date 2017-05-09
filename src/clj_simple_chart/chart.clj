(ns clj-simple-chart.chart
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.scale.core :as scale]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.axis.ticks :refer [ticks]]))



(defn make-tmp-scale [opts]
  (let [new-opts (merge {:width 100 :height 100} opts)]
    (scale/scale new-opts)))

(defn margins [{margin-left   :margin-left
                margin-right  :margin-right
                margin-top    :margin-top
                margin-bottom :margin-bottom
                :or           {margin-left   0
                               margin-right  0
                               margin-top    0
                               margin-bottom 0}}]
  {:margin-left   margin-left
   :margin-right  margin-right
   :margin-top    margin-top
   :margin-bottom margin-bottom})

(defn chart-inner [{width  :width
                    height :height
                    x      :x
                    y      :y}]
  (let [y-scale-tmp (make-tmp-scale y)
        y-scale-rendered (axis/render-axis y-scale-tmp)
        y-margins (margins (meta y-scale-rendered))
        ;x-scale-tmp (make-tmp-scale x)
        ;x-scale-rendered (axis/render-axis x-scale-tmp)

        margin-left (:margin-left y-margins)
        x-axis-space-used (+ (:margin-left y-margins)
                             (:margin-right y-margins))
        y-axis-space-used 0

        chart-width (- width x-axis-space-used)
        chart-height (- height y-axis-space-used)

        new-opts {:width  chart-width
                  :height chart-height}

        x-scale (scale/scale (merge x new-opts))
        y-scale (scale/scale (merge y new-opts))]
    {:margin-left margin-left
     :plot-width  chart-width
     :x           x-scale
     :y           y-scale}))

(defn chart [{width  :width
              height :height
              x      :x
              y      :y
              :as    config}]
  (cond (= ::none (get x :axis ::none)) (recur (assoc-in config [:x :axis] :x))
        (= ::none (get y :axis ::none)) (recur (assoc-in config [:y :axis] :y))
        :else (chart-inner config)))

(def margin 0)
(def svg-width 320)
(def svg-height 240)

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 10]})

(def yy {:type        :ordinal
         :axis        :y
         :orientation :both
         :reverse     true
         :domain      ["Peru"
                       "Iraq"
                       "Norway"
                       "United States"]})

(def cfg {:width  (- svg-width (* 2 margin))
          :height (- svg-height (* 2 margin))
          :x      xx :y yy})

(def c (chart cfg))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:line {:x1 margin :x2 margin :y1 0 :y2 svg-height :stroke "black" :stroke-width "1"}]
   [:g {:transform (core/translate margin margin)}
    [:circle {:r 5 :fill "red" :stroke "black" :stroke-width 3}]
    [:g {:transform (core/translate (:margin-left c) 0)}
     [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]

     [:g {:transform (core/translate (:plot-width c) 0)}
      [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]
      ]

     (axis/render-axis (:y c))

     ]]
   ])

(defn render-self []
  (core/render "./img/chart.svg" (diagram)))