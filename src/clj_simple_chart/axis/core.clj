(ns clj-simple-chart.axis.core)

(defn translate [x y]
  (str "translate(" x "," y ")"))

(def text-axis-properties
  {:font-family "sans-serif"
   :font-size   "12px"})

(defmulti ticks :type)

(defmethod ticks :ordinal
  [scale]
  (:domain scale))

(defmulti center-pos (fn [scale v] (:type scale)))

(defmethod center-pos :ordinal
  [scale v]
  (double (+ (/ (:bandwidth scale) 2)
             ((:point-fn scale) v))))

(defn render-x-axis [scale sign dy]
  (let [color (get scale :color "#000")
        rng (:range scale)
        sign-char (if (= -1 sign) "-" "")]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5," sign-char "6 V0.5 H" (int (apply max rng)) ".5 V" sign-char "6")}]
     (map (fn [d] [:g {:transform (translate (center-pos scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 (* sign 6)}]
                   [:text (merge text-axis-properties
                                 {:x           0.5
                                  :text-anchor "middle"
                                  :fill        color
                                  :dy          dy
                                  :y           (* sign 9)})
                    (str d)]]) (ticks scale))]))

(defmulti render-axis (juxt :axis :orientation))

(defmethod render-axis [:x :bottom] [scale]
  [:g {:transform (translate 0 (:height scale))}
       (render-x-axis scale 1 ".71em")])

(defmethod render-axis [:x :top] [scale]
  [:g {:transform (translate 0 0)}
   (render-x-axis scale -1 "0em")])

(defmethod render-axis [:x :both] [scale]
  [:g
   (render-axis (assoc scale :orientation :top))
   (render-axis (assoc scale :orientation :bottom))])