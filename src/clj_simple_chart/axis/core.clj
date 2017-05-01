(ns clj-simple-chart.axis.core
  (:require [clj-simple-chart.point :refer [center-point]]
            [clj-simple-chart.axis.ticks :refer [ticks]]
            [clj-simple-chart.opentype :as opentype]))

(defn translate [x y]
  (str "translate(" x "," y ")"))

(def text-axis-properties
  {:font-family "sans-serif"
   :font-size   "12px"})

(def scale-and-argument (fn [scale v] (:type scale)))

(defmulti frmt scale-and-argument)

(defmethod frmt :ordinal
  [scale v]
  v)

;;;; TODO: How does d3 do this?
(defn number-of-decimals [scale]
  (let [domain (:domain scale)
        domain-diff (Math/abs (apply - domain))]
    (cond (>= domain-diff 8) 0
          (>= domain-diff 1) 1
          :else 2)))

(defmethod frmt :linear
  [scale v]
  (format (str "%." (number-of-decimals scale) "f") v))

(def grid-stroke-opacity 0.25)

(defn render-x-axis [scale sign dy]
  (let [color (get scale :color "#000")
        rng (:range scale)
        neg-sign (* -1 sign)
        sign-char (if (= -1 sign) "-" "")]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5," sign-char "6 V0.5 H" (int (apply max rng)) ".5 V" sign-char "6")}]
     (map (fn [d] [:g {:transform (translate (center-point scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 (* sign 6)}]
                   (when (:grid scale)
                     [:line {:stroke         color
                             :stroke-opacity grid-stroke-opacity
                             :y2             (* neg-sign (:height scale))
                             :x1             0.5 :x2 0.5}])
                   (opentype/text {:x           0.5
                                   :dy          dy
                                   :y           (* sign 9)
                                   :text-anchor "middle"
                                   :font-size   12
                                   } (frmt scale d))
                   #_[:text (merge text-axis-properties
                                 {:x           0.5
                                  :text-anchor "middle"
                                  :fill        color
                                  :dy          dy
                                  :y           (* sign 9)})
                    (frmt scale d)]]) (ticks scale))]))

(defn render-y-axis [scale sign text-anchor]
  (let [color (get scale :color "#000")
        sign-char (if (= -1 sign) "-" "")
        neg-sign (* -1 sign)
        rng (:range scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M" sign-char "6," (int (apply max rng)) ".5 H0.5 V" (int (apply min rng)) ".5 H" sign-char "6")}]
     (map (fn [d] [:g {:transform (translate 0 (center-point scale d))}
                   [:line {:stroke color :x2 (* sign 6) :y1 0.5 :y2 0.5}]
                   (when (:grid scale)
                     [:line {:stroke         color
                             :stroke-opacity grid-stroke-opacity
                             :x2             (* neg-sign (:width scale))
                             :y1             0.5 :y2 0.5}])
                   (opentype/text {:x           (* sign 9)
                                   :dy          ".32em"
                                   :y           0.5
                                   :text-anchor text-anchor
                                   :font-size   12
                                   } (frmt scale d))]) (ticks scale))]))

(defmulti render-axis (juxt :axis :orientation))

(defmethod render-axis [:y :left] [scale]
  [:g {:transform (translate 0 0)}
   (render-y-axis scale -1 "end")])

(defmethod render-axis [:y :right] [scale]
  [:g {:transform (translate (:width scale) 0)}
   (render-y-axis scale 1 "start")])

(defmethod render-axis [:y :both] [scale]
  [:g
   (render-axis (-> scale
                    (assoc :orientation :left)
                    (dissoc :grid)))
   (render-axis (assoc scale :orientation :right))])

(defmethod render-axis [:x :bottom] [scale]
  [:g {:transform (translate 0 (:height scale))}
   (render-x-axis scale 1 ".71em")])

(defmethod render-axis [:x :top] [scale]
  [:g {:transform (translate 0 0)}
   (render-x-axis scale -1 "0em")])

(defmethod render-axis [:x :both] [scale]
  [:g
   (render-axis (-> scale
                    (assoc :orientation :top)
                    (dissoc :grid)))
   (render-axis (assoc scale :orientation :bottom))])