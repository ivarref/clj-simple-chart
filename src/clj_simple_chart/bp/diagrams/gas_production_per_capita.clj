(ns clj-simple-chart.bp.diagrams.gas-production-per-capita
  (:require [clj-simple-chart.bp.bpdata2 :as bpdata]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.bp.units :as units]
            [clj-simple-chart.colors :as colors]
            [clojure.test :as test]
            [clj-simple-chart.bp.wb-raw :as wb-raw]))

(def data (->> bpdata/most-recent-data-countries
               (filter :gas_production_bm3)
               (csv/keep-columns [:gas_production_bm3 :population :country :country_code])
               (mapv #(assoc % :total (/ (* units/billion (:gas_production_bm3 %))
                                         (* 1000 (:population %)))))
               (csv/drop-columns [:gas_production_bm3])
               (filter #(pos? (:total %)))
               (sort-by :total)
               (take-last 20)))

(def norway (first (filter #(= "Norway" (:country %)) data)))

(def norway-gas-production-bill-sm3
  (/ (* 1000 (:total norway) (:population norway))
     units/billion))
;; OD value: 116.649
;; BP value: 116.649 (!)

(def marg 10)
(def two-marg (* 2 marg))

; https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions
; iPhone 4, 4s
(def svg-width 320)
(def svg-height 480)

(def available-width (- svg-width two-marg))

(def domain (mapv :country data))

(def header (opentype/stack
              {}
              [{:text "Top 20 Gas Producers Per Capita" :font "Roboto Black" :font-size 19}
               {:text "Thousand Cubic Metres Per Capita Per Year" :font "Roboto Regular" :font-size 14 :margin-bottom 0}]))

(def footer (opentype/stack
              {:width available-width}
              [
               {:margin-top 5 :text (str "Sources: BP (" bpdata/bp-release-year "), World Bank (" wb-raw/wb-release-year ").") :font "Roboto Regular" :font-size 14}
               #_{:margin-top 2
                  :text       "Data from 2016." :font "Roboto Regular" :font-size 14}
               {:valign :bottom :align :right :text "@ivarref" :font "Roboto Regular" :font-size 14}
               ]))

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 100]})

(def yy {:type          :ordinal
         :axis          :y
         :domain        domain
         :orientation   :left
         :round         true
         :padding-inner 0.4
         :padding-outer 0.3})

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))
(def x (:x c))
(def y (:y c))

(def rect (rect/scaled-rect x y))

(defn make-rect [{country :country
                  coal    :coal
                  total   :total
                  :as     item}]
  {:p country :h total :fill colors/red})

(defn total-text [{country :country
                   total   :total}]
  (opentype/text {:x  (point x total)
                  :dx ".20em"
                  :y  (center-point y country)
                  :dy ".32em"}
                 (format "%.1f" total)))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (rect (mapv make-rect data))
     (map total-text data)
     (axis/render-axis y)
     (axis/render-axis x)
     #_[:g {:transform (translate-y (- (:plot-height c) (:height (meta legend))))}
        #_legend]]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (render "./img/bp-svg/gas-production-per-capita.svg" "./img/bp-png/gas-production-per-capita.png" (diagram)))
