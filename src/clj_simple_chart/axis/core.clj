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
  {:font-size 14
   :font-name "Roboto Regular"})

(def domain ["Peru" "Iraq" "United States"])

(defn apply-axis-text-style-fn [opts scale d]
  (let [default-fn (fn [x] {})
        f (get scale :axis-text-style-fn default-fn)]
    (merge opts (f d))))

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

(defn render-x-axis [scale sign dy margin-direction]
  (let [color (get scale :color "#000")
        rng (:range scale)
        neg-sign (* -1 sign)
        sign-char (if (= -1 sign) "-" "")
        tiks (ticks scale)
        tiks-str (mapv (partial frmt scale) tiks)
        txts (mapv #(opentype/text {:x           0.5
                                    :dy          dy
                                    :y           (* sign 9)
                                    :fill        color
                                    :text-anchor "middle"
                                    :font-size   14} %) tiks-str)
        txt-meta (mapv meta txts)
        max-height-font (apply max (mapv :height txt-meta))
        spacing-left (double (/ (:width (first txt-meta)) 2))
        spacing-right (double (/ (:width (last txt-meta)) 2))
        ]
    (with-meta
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
                                     :fill        color
                                     :text-anchor "middle"
                                     :font-size   14}
                                    (frmt scale d))]) (ticks scale))]
      {margin-direction (+ 9 max-height-font)
       :margin-left spacing-left
       :margin-right spacing-right})))

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
                                   :font-size   14}
                                  (frmt scale d))]) (ticks scale))]))

(defn render-y-axis-ordinal [scale sign direction]
  (let [color (get scale :color "#000")
        sign-char (if (= -1 sign) "-" "")
        neg-sign (* -1 sign)
        rng (:range scale)
        axis-label-max-width (domain-max-width (:domain scale))
        width (+ 6 axis-label-max-width)]
    (with-meta
      [:g
       [:path {:stroke       color
               :stroke-width "1"
               :fill         "none"
               :d            (str "M" sign-char "3," (int (apply max rng)) ".5 H0.5 V" (int (apply min rng)) ".5 H" sign-char "3")}]
       (map (fn [d] [:g {:transform (translate 0 (center-point scale d))}
                     (opentype/text
                       (apply-axis-text-style-fn {:x         (- (* sign 6)
                                                                (if (= 1 sign)
                                                                  0
                                                                  axis-label-max-width))
                                                  :dy        ".32em"
                                                  :y         0.5
                                                  :font-size 14} scale d)
                       (frmt scale d))]) (ticks scale))]
      {direction width})))

(defn transform-with-meta [x y k]
  (with-meta
    [:g {:transform (translate x y)} k]
    (meta k)))

(defmulti render-axis (juxt :axis :type :orientation))

(defmethod render-axis [:y :ordinal :left] [scale]
  (render-y-axis-ordinal scale -1 :margin-left))

(defmethod render-axis [:y :ordinal :right] [scale]
  (transform-with-meta (:width scale) 0
                       (render-y-axis-ordinal scale 1 :margin-right)))

(defmethod render-axis [:y :ordinal :both] [scale]
  (let [ax-left (render-axis (assoc scale :orientation :left))
        ax-right (render-axis (assoc scale :orientation :right))]
    (with-meta [:g ax-left ax-right]
               (merge (meta ax-left) (meta ax-right)))))

(defmethod render-axis [:x :linear :top] [scale]
  (render-x-axis scale -1 "0em" :margin-top))

(defmethod render-axis [:x :linear :bottom] [scale]
  (transform-with-meta 0 (:height scale)
                       (render-x-axis scale 1 ".71em" :margin-bottom)))

(defmethod render-axis [:x :linear :both] [scale]
  (let [ax-top (render-axis (assoc scale :orientation :top))
        ax-bottom (render-axis (assoc scale :orientation :bottom))]
    (with-meta [:g ax-top ax-bottom]
               (merge (meta ax-top) (meta ax-bottom)))))

;(cond (and (every? string? (:domain scale)) (= :ordinal (:type scale)))
;
;      :else
;      (render-y-axis scale -1 "end")))

;(defmethod render-axis [:y :right] [scale]
;  (let [rendered-axis (render-y-axis scale 1 "start")]
;    (with-meta
;      [:g {:transform (translate (:width scale) 0)} rendered-axis]
;      (meta rendered-axis))))
;
;(defmethod render-axis [:y :both] [scale]
;  [:g
;   (render-axis (-> scale
;                    (assoc :orientation :left)
;                    (dissoc :grid)))
;   (render-axis (assoc scale :orientation :right))])
;
;(defmethod render-axis [:x :bottom] [scale]
;  [:g {:transform (translate 0 (:height scale))}
;   (render-x-axis scale 1 ".71em")])
;
;(defmethod render-axis [:x :top] [scale]
;  [:g {:transform (translate 0 0)}
;   (render-x-axis scale -1 "0em")])
;
;(defmethod render-axis [:x :both] [scale]
;  [:g
;   (render-axis (-> scale
;                    (assoc :orientation :top)
;                    (dissoc :grid)))
;   (render-axis (assoc scale :orientation :bottom))])