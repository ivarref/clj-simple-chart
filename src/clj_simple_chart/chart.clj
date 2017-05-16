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
                    y      :y
                    y2     :y2
                    or     {y2 nil}}]
  (let [y-scale-rendered (axis/render-axis (make-tmp-scale y))
        x-scale-rendered (axis/render-axis (make-tmp-scale x))

        y-margins (margins (meta y-scale-rendered))
        y2-margins (if y2 (margins (meta (axis/render-axis (make-tmp-scale y2)))) (margins {}))
        x-margins (margins (meta x-scale-rendered))

        all-margins [y-margins y2-margins x-margins]

        max-right (Math/ceil (apply max (map :margin-right all-margins)))
        max-left (Math/ceil (apply max (map :margin-left all-margins)))
        max-top (Math/ceil (apply max (map :margin-top all-margins)))
        max-bottom (Math/ceil (apply max (map :margin-bottom all-margins)))

        chart-width (- width (+ max-left max-right))
        chart-height (- height (+ max-top max-bottom))

        new-opts {:width chart-width :height chart-height}

        x-scale (scale/scale (merge x new-opts))
        y-scale (scale/scale (merge y new-opts))

        result (cond-> {:margin-left max-left
                        :margin-top  max-top
                        :margin-right max-right
                        :margin-bottom max-bottom
                        :plot-width  chart-width
                        :plot-height chart-height
                        :x           x-scale
                        :y           y-scale}
                       y2 (assoc :y2 (scale/scale (merge y2 new-opts))))
        ]
    result))

(defn chart [{width  :width
              height :height
              x      :x
              y      :y
              y2     :y2
              :as    config
              or     {y2 nil}}]
  (cond (and (map? y2) (= ::none (get y2 :axis ::none))) (recur (assoc-in config [:y2 :axis] :y))
        (= ::none (get x :axis ::none)) (recur (assoc-in config [:x :axis] :x))
        (= ::none (get y :axis ::none)) (recur (assoc-in config [:y :axis] :y))
        :else (chart-inner config)))

(def svg-width 320)
(def svg-height 240)

(def xx {:type        :linear
         :orientation :both
         :ticks       5
         :domain      [0 1000]})

(def yy {:type        :linear
         :orientation :left
         :ticks       5
         :domain      [77 88]})

(def yy2 {:type        :linear
          :orientation :right
          :ticks       5
          :domain      [0 100]})

(def cfg {:width  svg-width
          :height svg-height
          :x      xx
          :y      yy
          :y2     yy2})

(def c (chart cfg))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:rect {:width "100%" :height "100%" :fill-opacity "0.2" :fill "steelblue"}]
   [:g {:transform (core/translate (:margin-left c) (:margin-top c))}

    [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]
    [:circle {:transform (core/translate 0 (:plot-height c))
              :r         7 :fill "yellow" :stroke "black" :stroke-width 3}]

    [:g {:transform (core/translate (:plot-width c) 0)}
     [:circle {:r 7 :fill "yellow" :stroke "black" :stroke-width 3}]
     [:circle {:transform (core/translate 0 (:plot-height c))
               :r         7 :fill "yellow" :stroke "black" :stroke-width 3}]
     ]

    (axis/render-axis (:y c))
    (axis/render-axis (:y2 c))
    (axis/render-axis (:x c))

    ]])

(defn render-self []
  (core/render "./img/chart.svg" (diagram)))