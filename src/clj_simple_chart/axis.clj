(ns clj-simple-chart.axis
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.ticks :as ticks]))

(defn number-of-decimals [scale]
  (let [domain (:domain (meta scale))
        domain-diff (Math/abs (apply - domain))]
    (cond (>= domain-diff 8) 0
          (>= domain-diff 1) 1
          :else 2)))

(defn scale-format [scale v]
  (format (str "%." (number-of-decimals scale) "f") v))

(defn ticks-for-scale [scale]
  (let [domain (:domain (meta scale))
        num-ticks (get (meta scale) :ticks 10)
        tiks (ticks/ticks (first domain) (last domain) num-ticks)]
    tiks))

(defn tick-pos-scale [scale d]
  (let [scale-type (get (meta scale) :scale-type :default)
        bandwidth (get (meta scale) :bandwidth 0)]
    (cond (= scale-type :band) (+ (/ bandwidth 2)
                                  (scale d))
          :else (scale d))))

(def text-axis-properties
  {:font-family "sans-serif"
   :font-size   "12px"})

(defn left-y-axis [scale]
  (let [color (get (meta scale) :color "#000")
        rng (:range (meta scale))
        grid (get (meta scale) :grid false)
        width (get (meta scale) :width ::none)
        fmt (partial scale-format scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M-6," (apply max rng) ".5 H0.5 V" (apply min rng) ".5 H-6")}]
     (map (fn [d] [:g {:transform (core/translate 0 (tick-pos-scale scale d))}
                   [:line {:stroke color :x2 -6 :y1 0.5 :y2 0.5}]
                   (when grid
                     [:line {:stroke color
                             :stroke-opacity 0.2
                             :x2 width :y1 0.5 :y2 0.5}])
                   [:text (merge text-axis-properties
                                 {:x           -9
                                  :text-anchor "end"
                                  :fill        color
                                  :dy          ".32em"
                                  :y           0.5})
                    (fmt d)]]) (ticks-for-scale scale))]))

(defn right-y-axis [scale]
  (let [color (get (meta scale) :color "#000")
        rng (:range (meta scale))
        fmt (partial scale-format scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M6," (apply max rng) ".5 H0.5 V" (apply min rng) ".5 H6")}]
     (map (fn [d] [:g {:transform (core/translate 0 (tick-pos-scale scale d))}
                   [:line {:stroke color :x2 6 :y1 0.5 :y2 0.5}]
                   [:text (merge text-axis-properties
                                 {:x           9
                                  :text-anchor "start"
                                  :fill        color
                                  :dy          ".32em"
                                  :y           0.5})
                    (fmt d)]]) (ticks-for-scale scale))]))

(defn bottom-x-axis [scale]
  (let [color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5,6 V0.5 H" (apply max range) ".5 V6")}]
     (map (fn [d] [:g {:transform (core/translate (tick-pos-scale scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 6}]
                   [:text (merge text-axis-properties
                                 {:x           0.5
                                  :text-anchor "middle"
                                  :fill        color
                                  :dy          ".71em"
                                  :y           9})
                    (fmt d)]]) (ticks-for-scale scale))]))

(defn top-x-axis [scale]
  (let [color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5,-6 V0.5 H" (apply max range) ".5 V-6")}]
     (map (fn [d] [:g {:transform (core/translate (tick-pos-scale scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 -6}]
                   [:text (merge text-axis-properties
                                 {:x           0.5
                                  :text-anchor "middle"
                                  :fill        color
                                  :dy          "0em"
                                  :y           -9})
                    (fmt d)]]) (ticks-for-scale scale))]))
