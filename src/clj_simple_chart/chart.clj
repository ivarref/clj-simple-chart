(ns clj-simple-chart.chart
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.scale.core :as scale]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.axis.ticks :refer [ticks]]))

(defn make-tmp-scale-w-h [w h opts]
  (let [new-opts (merge opts {:width w :height h})]
    (scale/scale new-opts)))

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

(defn sum-left-right [x] (+ (:margin-right x) (:margin-left x)))

(defn solve-margins-x [w h prev-margins x recursion-limit]
  (let [margs (meta (axis/render-axis (make-tmp-scale-w-h (- w (sum-left-right prev-margins)) h x)))
        diff (Math/abs (- (sum-left-right margs) (sum-left-right prev-margins)))]
    (cond (< diff 0.1) (margins margs)
          (> recursion-limit 0) (solve-margins-x w h margs x (dec recursion-limit))
          :else (throw (Exception. "Sorry! Diff is still >0.1, but search is exhausted!")))))

(defn chart-inner [{width  :width
                    height :height
                    x      :x
                    y      :y
                    y2     :y2
                    or     {y2 nil}}]
  (let [y-margins (margins (meta (axis/render-axis (make-tmp-scale y))))
        y2-margins (if y2 (margins (meta (axis/render-axis (make-tmp-scale y2)))) (margins {}))
        x-margins (solve-margins-x width height (margins {}) x 10)

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

        result (cond-> {:margin-left   max-left
                        :margin-top    max-top
                        :margin-right  max-right
                        :margin-bottom max-bottom
                        :plot-width    chart-width
                        :plot-height   chart-height
                        :x             x-scale
                        :y             y-scale}
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

;(def svg-width 200)
;(def svg-height 200)
;(def x-domain ["ASDFASDF1" "ASDFASDF2" "ASDFASDF3" "ASDFASDF4" "ASDFASDF5" "ASDFASDF6"])
;
;(def xx {:type          :ordinal
;         :axis          :x
;         :domain        x-domain
;         :tick-values   [(first x-domain) (last x-domain)]
;         :orientation   :bottom})
;
;(def margs (solve-margins-x svg-width svg-height (margins {}) xx 10))
;
;(def x (scale/scale (merge xx {:width  (- svg-width (sum-left-right margs)) :height 100})))
;
;(defn diagram []
;  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
;   [:rect {:width "100%" :height "100%" :fill-opacity 0.5 :fill "#ffaa00"}]
;   [:g {:transform (translate (:margin-left margs) 0)}
;    (axis/render-axis x)]])
;
;(defn render-self []
;  (core/render "./img/dev/chart.svg" (diagram)))
