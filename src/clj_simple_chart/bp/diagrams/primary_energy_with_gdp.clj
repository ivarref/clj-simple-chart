(ns clj-simple-chart.bp.diagrams.primary-energy-with-gdp
  (:require [clj-simple-chart.bp.bpdata2 :as bpdata]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.bp.units :as units]
            [clojure.test :as test]
            [clj-simple-chart.point :as point]))

(def select-countries ["Norway"
                       "Sweden"
                       "Denmark"
                       "United States"
                       "Russia"
                       "Canada"
                       "Germany"
                       "Poland"
                       "Pakistan"
                       "UAE"
                       "Switzerland"
                       "France"
                       "OECD"
                       "Africa"
                       "Eurozone"
                       "World"
                       "Qatar"
                       "Singapore"
                       "Kuwait"
                       ;"Asia-Pacific"
                       "Saudi Arabia"
                       "Middle East"
                       "Non OECD"
                       "China"
                       "India"
                       "South Africa"
                       "Brazil"
                       "Indonesia"])

(def data (->> bpdata/most-recent-data-countries
               (filter #(some #{(:country %)} select-countries))
               (filter :gdp)
               (filter :total_mtoe)
               (mapv #(assoc % :total (/ (* units/million (:total_mtoe %))
                                         (:population %))))
               (mapv #(update % :gdp (fn [gdp] (/ (/ gdp 1000) (:population %)))))
               (sort-by :total)))

;(test/is (= (count select-countries) (count data)))

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
              [{:text "Primary Energy Consumption" :font "Roboto Black" :font-size 22}
               ;{:text "Selected nations and groups of nations" :font "Roboto Bold" :font-size 16}
               {:text "Tonnes of Oil Equivalents Per Capita Per Year" :font-size 14 :margin-bottom 0}]))

(def gdp-per-capita-fill "rgb(214, 39, 40)")

(def footer (opentype/stack
              {:width available-width}
              [{:text "GDP per capita, '000 USD" :fill gdp-per-capita-fill :font "Roboto Regular" :font-size 14}
               {:margin-top 10 :text "Sources: BP (2017), World Bank (2017)." :font "Roboto Regular" :font-size 14}
               {:margin-top 2 :text "© Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14}
               ]))

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 25]})

(def xx2 {:type        :linear
          :axis        :x
          :orientation :bottom
          :color       gdp-per-capita-fill
          :ticks       10
          :domain      [0 (apply max (mapv :gdp data))]})

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
                     :x2     xx2
                     :y      yy}))
(def x (:x c))
(def x2 (:x2 c))
(def y (:y c))

(def rect (rect/scaled-rect x y))

(def x2-fn (partial point/center-point (:x2 c)))
(def y-fn (partial point/center-point (:y c)))

(defn make-rect [{country :country
                  coal    :coal
                  total   :total
                  :as     item}]
  {:p country :h total :fill "steelblue"})

(defn gdp-point [{gdp     :gdp
                  country :country
                  :as     item}]
  [:g {:transform (translate (x2-fn gdp)
                             (y-fn country))}
   [:circle {:r      3
             :fill   gdp-per-capita-fill
             :stroke "black"}]])

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
     (map gdp-point data)
     (map total-text data)
     (axis/render-axis y)
     (axis/render-axis x)
     (axis/render-axis x2)
     ]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (render "./img/bp-svg/primary-energy-per-capita-with-gdp.svg" "./img/bp-png/primary-energy-per-capita-with-gdp.png" (diagram)))
