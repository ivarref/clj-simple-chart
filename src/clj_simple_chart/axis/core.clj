(ns clj-simple-chart.axis.core
  (:require [clj-simple-chart.point :refer [center-point]]
            [clj-simple-chart.roughjs :as rough]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.axis.ticks :refer [ticks]]
            [clj-simple-chart.opentype :as opentype]))

(def scale-and-argument (fn [scale v] (:type scale)))

(defmulti frmt scale-and-argument)

(defmethod frmt :ordinal
  [scale v]
  (if (:tick-format scale)
    ((:tick-format scale) v)
    v))

(defmethod frmt :ordinal-linear
  [scale v]
  (if (:tick-format scale)
    ((:tick-format scale) v)
    v))

;;;; TODO: How does d3 do this?
(defn number-of-decimals [scale]
  (let [domain (:domain scale)
        domain-diff (Math/abs (apply - domain))]
    (cond (>= domain-diff 8) 0
          (>= domain-diff 1) 1
          :else 2)))

(defmethod frmt :linear
  [scale v]
  (if-let [tf (:tick-format scale)]
    (tf v)
    (format (str "%." (number-of-decimals scale) "f") v)))

(def axis-font-properties
  {:font-size 14
   :font-name "Roboto Regular"})

(def domain ["Peru" "Iraq" "United States"])

(defn apply-axis-text-style-fn [opts scale d]
  (let [default-fn (fn [x] {})
        f (get scale :axis-text-style-fn default-fn)]
    (merge axis-font-properties
           (merge opts (f d)))))

(defn meta-texts-for-scale [scale]
  (let [txts (map #(opentype/text
                     (apply-axis-text-style-fn {} scale %)
                     (frmt scale %))
                  (ticks scale))]
    (map meta (if (:reverse scale) (reverse txts) txts))))

(def grid-stroke-opacity 0.25)

(defn render-x-axis [{:keys [rough] :as scale} sign dy margin-direction]
  (let [color (get scale :color "#000")
        rng (:range scale)
        neg-sign (* -1 sign)
        sign-char (if (= -1 sign) "-" "")
        txt-meta (meta-texts-for-scale scale)
        tiks (ticks scale)
        x-pos (mapv (partial center-point scale) tiks)
        x-pos (if (:reverse scale) (reverse x-pos) x-pos)
        max-height-font (apply max (mapv :height txt-meta))
        spacing-left (double (/ (:width (first txt-meta)) 2))
        overflow-left (Math/max 0.0 (- spacing-left (first x-pos)))
        spacing-right (double (/ (:width (last txt-meta)) 2))
        overflow-right (Math/max 0.0 (- (+ (last x-pos) spacing-right)
                                        (:width scale)))]
    ;(println "overflow-left is" overflow-left)
    (with-meta
      [:g
       (rough/path {:stroke       color
                    :stroke-width "1"
                    :rough        rough
                    :fill         "none"
                    :d            (str "M0.5," sign-char "6 V0.5 H" (int (apply max rng)) ".5 V" sign-char "6")})
       (map (fn [d]
              [:g {:transform (translate (center-point scale d) 0)}
               (rough/line {:rough rough :stroke color :x1 0.5 :x2 0.5 :y2 (* sign 6)})
               (when (:grid scale)
                 (rough/line {:rough          rough
                              :stroke         color
                              :stroke-opacity (or (:grid-stroke-opacity scale) grid-stroke-opacity)
                              :y2             (* neg-sign (:height scale))
                              :x1             0.5 :x2 0.5}))
               (let [v (opentype/text
                         (-> (apply-axis-text-style-fn
                               {:x           0.5
                                :dy          dy
                                :y           (* sign 9)
                                :fill        color
                                :text-anchor "middle"} scale d)
                             (assoc :rough (get scale :rough-text)))
                         (frmt scale d))]
                 v)])
            (ticks scale))]
      {margin-direction (+ 9 max-height-font)
       :margin-left     overflow-left
       :margin-right    (+ 0.5 overflow-right)})))

(defn render-y-axis [scale sign text-anchor margin-direction]
  (let [color (get scale :color "#000")
        sign-char (if (= -1 sign) "-" "")
        neg-sign (* -1 sign)
        rng (:range scale)
        meta-txts (meta-texts-for-scale scale)
        tiks (ticks scale)
        y-pos (mapv (partial center-point scale) tiks)
        y-pos (if (:reverse scale) (reverse y-pos) y-pos)

        last-y-pos (last y-pos)
        offset-height-top (* 0.32 (:font-size (last meta-txts)))
        used-space-above-center (- (:height (last meta-txts)) offset-height-top)
        top-pos (- last-y-pos used-space-above-center)
        margin-top (Math/abs (Math/min 0.0 top-pos))

        first-y-pos (first y-pos)
        offset-height-bottom (* 0.32 (:font-size (first meta-txts)))
        used-space-above-center (- (:height (first meta-txts)) offset-height-bottom)
        used-space-below-center (- (:height (first meta-txts)) used-space-above-center)
        bottom-pos (+ first-y-pos used-space-below-center)
        margin-bottom (Math/max 0.0 (- bottom-pos (:height scale)))

        axis-label-max-width (apply max (map :width meta-txts))
        width (+ 9 axis-label-max-width)]
    (with-meta
      [:g
       (rough/path {:rough        (get scale :rough)
                    :stroke       color
                    :stroke-width "1"
                    :fill         "none"
                    :d            (str "M" sign-char "6," (int (apply max rng)) ".5 H0.5 V" (int (apply min rng)) ".5 H" sign-char "6")})
       (map (fn [d] [:g {:transform (translate 0 (center-point scale d))}
                     (rough/line {:rough (get scale :rough) :stroke color :x2 (* sign 6) :y1 0.5 :y2 0.5})
                     (when (:grid scale)
                       (rough/line {:rough          (get scale :rough)
                                    :stroke         color
                                    :stroke-opacity (or (:grid-stroke-opacity scale) grid-stroke-opacity)
                                    :x2             (* neg-sign (:width scale))
                                    :y1             0.5 :y2 0.5}))
                     (opentype/text
                       (-> (apply-axis-text-style-fn
                             {:x           (* sign 9)
                              :dy          ".32em"
                              :fill        color
                              :y           0.5
                              :text-anchor text-anchor} scale d)
                           (assoc :rough (get scale :rough-text)))
                       (frmt scale d))]) (ticks scale))]
      {margin-direction width
       :margin-top      (- margin-top 0.5)
       :margin-bottom   (+ 0.5 margin-bottom)})))

(defn render-y-axis-ordinal [scale sign margin]
  (let [color (get scale :color "#000")
        sign-char (if (= -1 sign) "-" "")
        neg-sign (* -1 sign)
        rng (:range scale)
        meta-txts (meta-texts-for-scale scale)
        axis-label-max-width (apply max (map :width meta-txts))
        width (+ 6 axis-label-max-width)]
    (with-meta
      [:g
       [:path {:stroke       color
               :stroke-width "1"
               :fill         "none"
               :d            (str "M" sign-char "3," (int (apply max rng)) ".5 H0.5 V" (int (apply min rng)) ".5 H" sign-char "3")}]
       (map (fn [d] [:g {:transform (translate 0 (center-point scale d))}
                     (opentype/text
                       (apply-axis-text-style-fn {:x           (if (= 1 sign)
                                                                 6
                                                                 (- (Math/ceil width)))
                                                  :text-anchor "start"
                                                  :dy          ".32em"
                                                  :y           0.5} scale d)
                       (frmt scale d))]) (ticks scale))]
      {margin         width
       :margin-bottom 0.5})))

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

(defmethod render-axis [:y :linear :left] [scale]
  (render-y-axis scale -1 "end" :margin-left))

(defmethod render-axis [:y :linear :right] [scale]
  (transform-with-meta (:width scale) 0
                       (render-y-axis scale 1 "start" :margin-right)))

(defmethod render-axis [:y :linear :both] [scale]
  (let [ax-left (render-axis (assoc scale :orientation :left))
        ax-right (render-axis (assoc scale :orientation :right))]
    (with-meta [:g ax-left ax-right]
               (merge (meta ax-left) (meta ax-right)))))

(defmethod render-axis [:x :linear :top] [scale]
  (render-x-axis scale -1 "0em" :margin-top))

(defmethod render-axis [:x :linear :bottom] [scale]
  (transform-with-meta 0 (:height scale)
                       (render-x-axis scale 1 ".71em" :margin-bottom)))

(defmethod render-axis [:x :ordinal-linear :bottom] [scale]
  (transform-with-meta 0 (:height scale)
                       (render-x-axis scale 1 ".71em" :margin-bottom)))

(defmethod render-axis [:x :ordinal :bottom] [scale]
  (transform-with-meta 0 (:height scale)
                       (render-x-axis scale 1 ".71em" :margin-bottom)))

(defmethod render-axis [:x :ordinal :top] [scale]
  (transform-with-meta 0 0
                       (render-x-axis scale -1 ".0em" :margin-top)))

;(defmethod render-axis [:x :bottom] [scale]
;  [:g {:transform (translate 0 (:height scale))}
;   (render-x-axis scale 1 ".71em")])

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
