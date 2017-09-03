(ns clj-simple-chart.ncs.gas-rp-diagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.production-monthly-field :as production]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clojure.string :as string]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> production/by-date))
(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

(def bucket-to-fill (zipmap (sort (keys production/empty-buckets))
                            ["red"
                             "green"
                             "cyan"
                             "yellow"
                             "blue"
                             "orange"]))

(def sub-domain buckets)

(def x-domain (map :date data))

(def x-ticks (filter #(or (= (first x-domain) %)
                          (= (last x-domain) %)
                          (.endsWith % "5-12")
                          (.endsWith % "0-12")
                          ) (map :date data)))

(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :tick-values   x-ticks
         :tick-format   (fn [x] (if (.endsWith x "5-12")
                                  (subs x 2 4)
                                  (subs x 0 4)))
         :orientation   :bottom
         :domain        x-domain
         :sub-domain    sub-domain
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 130]})

(def available-height (- svg-height (+ two-marg
                                       #_(:height (meta header))
                                       #_(:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  (map (fn [[k fill]]
         {:p    (:date opts)
          :c    k
          :fill fill
          :h    (get opts k 0)})
       bucket-to-fill))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    [:g {:transform (translate (:margin-left c) (+ #_(:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     [:g (bars (mapv make-rect data))]
     (axis/render-axis (:x c))
     ]]])

(defn render-self []
  (core/render "./img/ncs-svg/gas-rp.svg" "./img/ncs-png/gas-rp.png" (diagram)))

