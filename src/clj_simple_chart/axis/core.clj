(ns clj-simple-chart.axis.core
  (:require [clj-simple-chart.point :refer [center-point]]
            [clj-simple-chart.axis.ticks :refer [ticks]]
            [clj-simple-chart.opentype :as opentype]))

(defn translate [x y]
  (str "translate(" x "," y ")"))

(def scale-and-argument (fn [scale v] (:type scale)))

(defmulti frmt scale-and-argument)

(defmethod frmt :ordinal
  [scale v]
  v)

(def axis-font-properties
  {:font-size 12
   :font-name "Roboto Regular"})

(def domain ["Peru" "Iraq" "United States"])

(defn bounding-box-domain [domain]
  (->> domain
       (map (fn [txt] (opentype/get-bounding-box (:font-name axis-font-properties)
                                                 txt
                                                 0
                                                 0
                                                 (:font-size axis-font-properties))))
       (reduce (fn [{ax1 :x1 ax2 :x2 ay1 :y1 ay2 :y2} {bx1 :x1 bx2 :x2 by1 :y1 by2 :y2}]
                 {:x1 (min ax1 bx1)
                  :x2 (max ax2 bx2)
                  :y1 (min ay1 by1)
                  :y2 (max ay2 by2)}))))

(defn domain-max-width [domain]
  (:x2 (bounding-box-domain domain)))

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
                                   :font-size   12}
                                  (frmt scale d))]) (ticks scale))]))

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
                                   :font-size   12}
                                  (frmt scale d))]) (ticks scale))]))

(defn render-y-axis-string-ordinal [scale sign text-anchor]
  (let [color (get scale :color "#000")
        sign-char (if (= -1 sign) "-" "")
        neg-sign (* -1 sign)
        rng (:range scale)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M" sign-char "3," (int (apply max rng)) ".5 H0.5 V" (int (apply min rng)) ".5 H" sign-char "3")}]
     (map (fn [d] [:g {:transform (translate 0 (center-point scale d))}
                   #_[:line {:stroke color :x2 (* sign 6) :y1 0.5 :y2 0.5}]
                   (opentype/text {:x         (- (* sign 6)
                                                 (domain-max-width (:domain scale)))
                                   :dy        ".32em"
                                   :y         0.5
                                   :font-size 12}
                                  (frmt scale d))]) (ticks scale))]))

(defmulti render-axis (juxt :axis :orientation))

(defmethod render-axis [:y :left] [scale]
  [:g {:transform (translate 0 0)}
   (cond (and (every? string? (:domain scale))
              (= :ordinal (:type scale)))
         (render-y-axis-string-ordinal scale -1 "end")
         :else
         (render-y-axis scale -1 "end"))])

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