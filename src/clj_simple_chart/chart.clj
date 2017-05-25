(ns clj-simple-chart.chart
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.scale.core :as scale]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.axis.ticks :refer [ticks]]))

(defn make-tmp-scale-w-h [w h opts]
  (let [new-opts (merge opts {:width w :height h})]
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
(defn sum-top-bottom [x] (+ (:margin-top x) (:margin-bottom x)))

(defn max-margins [margs]
  {:margin-top    (apply max (mapv :margin-top margs))
   :margin-bottom (apply max (mapv :margin-bottom margs))
   :margin-left   (apply max (mapv :margin-left margs))
   :margin-right  (apply max (mapv :margin-right margs))})

(defn margins-diff [a b]
  (+ (Math/abs (- (sum-left-right a) (sum-left-right b)))
     (Math/abs (- (sum-top-bottom a) (sum-top-bottom b)))))

(defn solve-margins [w h prev-margins axes recursion-limit]
  (let [axes (remove nil? axes)
        ww (- w (sum-left-right prev-margins))
        hh (- h (sum-top-bottom prev-margins))
        scale-fn (partial make-tmp-scale-w-h ww hh)
        margs (->> axes
                   (mapv #(axis/render-axis (scale-fn %)))
                   (mapv meta)
                   (mapv margins)
                   (max-margins))
        diff (margins-diff margs prev-margins)]
    (cond (< diff 0.1) margs
          (pos? recursion-limit) (solve-margins w h margs axes (dec recursion-limit))
          :else (throw (Exception. "Sorry! Diff is still >0.1, but search is exhausted!")))))

(defn chart-inner [{width  :width
                    height :height
                    x      :x
                    y      :y
                    y2     :y2
                    or     {y2 nil}}]
  (let [max-margins (solve-margins width height (margins {}) [x y y2] 10)

        max-right (Math/ceil (:margin-right max-margins))
        max-left (Math/ceil (:margin-left max-margins))
        max-top (Math/ceil (:margin-top max-margins))
        max-bottom (Math/ceil (:margin-bottom max-margins))

        chart-width (- width (+ max-left max-right))
        chart-height (- height (+ max-top max-bottom))

        new-opts {:width chart-width :height chart-height}

        x-scale (scale/scale (merge x new-opts))
        y-scale (scale/scale (merge y new-opts))

        result (cond-> {:margin-left      max-left
                        :margin-top       max-top
                        :margin-right     max-right
                        :margin-bottom    max-bottom
                        :available-width  width
                        :available-height height
                        :plot-width       chart-width
                        :plot-height      chart-height
                        :x                x-scale
                        :y                y-scale}
                       y2 (assoc :y2 (scale/scale (merge y2 new-opts))))]
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
