(ns clj-simple-chart.point)

(def scale-and-argument (fn [scale v] (:type scale)))

(defmulti center-point scale-and-argument)

(defmethod center-point :ordinal
  [scale v]
  (double (+ (/ (:bandwidth scale) 2)
             ((:point-fn scale) v))))

(defmethod center-point :ordinal-linear
  [scale v]
  (double ((:point-fn scale) v)))

(defmethod center-point :linear
  [scale v]
  (double ((:point-fn scale) v)))

(defn point [scale v]
  (double ((:point-fn scale) v)))