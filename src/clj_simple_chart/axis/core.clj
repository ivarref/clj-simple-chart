(ns clj-simple-chart.axis.core
  (:require [clj-simple-chart.ticks :as tiks]))

(defn translate [x y]
  (str "translate(" x "," y ")"))

(def text-axis-properties
  {:font-family "sans-serif"
   :font-size   "12px"})

(defmulti ticks :type)

(defmethod ticks :ordinal
  [scale]
  (:domain scale))

(defmethod ticks :linear
  [scale]
  (tiks/ticks (first (:domain scale))
              (last (:domain scale))
              (get scale :ticks 10)))

(defmulti center-pos (fn [scale v] (:type scale)))

(defmethod center-pos :ordinal
  [scale v]
  (double (+ (/ (:bandwidth scale) 2)
             ((:point-fn scale) v))))

(defmethod center-pos :linear
  [scale v]
  (double ((:point-fn scale) v)))

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
     (map (fn [d] [:g {:transform (translate (center-pos scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 (* sign 6)}]
                   (when (:grid scale)
                     [:line {:stroke         color
                             :stroke-opacity 0.2
                             :y2             (* neg-sign (:height scale))
                             :x1 0.5 :x2 0.5}])
                   [:text (merge text-axis-properties
                                 {:x           0.5
                                  :text-anchor "middle"
                                  :fill        color
                                  :dy          dy
                                  :y           (* sign 9)})
                    (str d)]]) (ticks scale))]))

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
     (map (fn [d] [:g {:transform (translate 0 (center-pos scale d))}
                   [:line {:stroke color :x2 (* sign 6) :y1 0.5 :y2 0.5}]
                   (when (:grid scale)
                     [:line {:stroke         color
                             :stroke-opacity 0.2
                             :x2             (* neg-sign (:width scale))
                             :y1 0.5 :y2 0.5}])
                   [:text (merge text-axis-properties
                                 {:x           (* sign 9)
                                  :text-anchor text-anchor
                                  :fill        color
                                  :dy          ".32em"
                                  :y           0.5})
                    (str d)]]) (ticks scale))]))

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