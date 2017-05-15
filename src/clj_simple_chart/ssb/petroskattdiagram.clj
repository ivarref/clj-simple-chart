(ns clj-simple-chart.ssb.petroskattdiagram
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]))

(def data (drop 1 petroskatt/twelve-mma-mrd-yearly-ytd))

(def x-domain (map :dato data))

(def svg-width 640)
(def svg-height 480)


(def x-ticks ["2010-12"
              "2011-12"
              "2012-12"
              "2013-12"
              "2014-12"
              "2015-12"
              "2016-12"
              (:dato (last data))])

(def x-ticks-str ["2010" "11" "12" "13" "14" "15" "16" "2017*"])

(def ordinaer (keyword "Ordinær skatt på utvinning av petroleum"))
(def saerskatt (keyword "Særskatt på utvinning av petroleum"))

(def xx {:type        :ordinal
         :orientation :bottom
         :tick-values x-ticks
         :tick-format (fn [x] (get (zipmap x-ticks x-ticks-str) x))
         :domain      x-domain
         :sub-domain  [ordinaer saerskatt]
         })

(def yy {:type        :linear
         :orientation :left
         :ticks       5
         :domain      [0 250]})

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
  (core/render "./img/petroskatt.svg" (diagram)))
