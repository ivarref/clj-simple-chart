(ns clj-simple-chart.ssb.petroskattdiagrammonthly
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]))

(def data (->> petroskatt/twelve-mma-mrd
               (mapv #(assoc % :year (read-string (subs (:dato %) 0 4))))
               #_(filter #(>= (:year %) 2010))))

(def x-domain (map :dato data))

(def svg-width 960)
(def svg-height 480)


(def x-ticks (filter #(.endsWith % "-01") (map :dato data)))

(def ordinaer (keyword "Ordinær skatt på utvinning av petroleum"))
(def saerskatt (keyword "Særskatt på utvinning av petroleum"))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (subs x 0 4))
         :domain        x-domain
         :sub-domain    [ordinaer saerskatt]
         :padding-inner 0.1
         :padding-outer 0.2
         })

(def yy {:type        :linear
         :orientation :left
         :ticks       5
         :domain      [0 275]})

(def c (chart/chart {:width  svg-width
                     :height svg-height
                     :x      xx
                     :y      yy}))

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  [{:p (:dato opts) :c ordinaer :h (get opts ordinaer) :fill "steelblue"}
   {:p (:dato opts) :c saerskatt :h (get opts saerskatt) :fill "red"}])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (core/translate (:margin-left c) (:margin-top c))}
    [:g (bars (mapv make-rect data))]
    (axis/render-axis (:y c))
    (axis/render-axis (:x c))]])


(defn render-self []
  (core/render "./img/petroskatt-monthly-mms.svg" (diagram)))
