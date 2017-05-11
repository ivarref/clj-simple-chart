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
  (let [y-scale-rendered (axis/render-axis (make-tmp-scale y))
        x-scale-rendered (axis/render-axis (make-tmp-scale x))

        y-margins (margins (meta y-scale-rendered))
        x-margins (margins (meta x-scale-rendered))

        max-right (max (:margin-right x-margins) (:margin-right y-margins))
        max-left (max (:margin-left x-margins) (:margin-left y-margins))
        max-top (max (:margin-top x-margins) (:margin-top y-margins))
        max-bottom (max (:margin-bottom x-margins) (:margin-bottom y-margins))

        chart-width (- width (+ max-left max-right))
        chart-height (- height (+ max-top max-bottom))

        new-opts {:width  chart-width
                  :height chart-height}

        x-scale (scale/scale (merge x new-opts))
        y-scale (scale/scale (merge y new-opts))]
    {:margin-left max-left
     :margin-top  max-top
     :plot-width  chart-width
     :plot-height chart-height
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
         :orientation :both
         :ticks       5
         :domain      [77 88]})

(def yy {:type        :ordinal
         :orientation :left
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

    [:g {:transform (core/translate (:margin-left c) (:margin-top c))}

     [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]

     [:g {:transform (core/translate (:plot-width c) 0)}
      [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]
      ]

     (axis/render-axis (:y c))
     (axis/render-axis (:x c))

     ]]
   ])

(defn render-self []
  (core/render "./img/chart.svg" (diagram)))