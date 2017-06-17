(ns clj-simple-chart.bp.bp-co2-per-capita-select
  (:require [clj-simple-chart.bp.bpdata :as bpdata]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]
            [clojure.test :as test]))

(def translate-countries {"Russian Federation" "Russia"
                          "Total Middle East"  "Middle East"
                          "Total Africa"       "Africa"
                          "Total Asia-Pacific" "Asia-Pacific"})

(def select-countries ["Norway"
                       "Sweden"
                       "Denmark"
                       "United States"
                       "Russia"
                       "Canada"
                       "Germany"
                       "Poland"
                       "Pakistan"
                       "Finland"
                       "France"
                       "OECD"
                       "Africa"
                       "Eurozone"
                       "World"
                       ;"Asia-Pacific"
                       "Saudi Arabia"
                       "Middle East"
                       "Non OECD"
                       "China"
                       "India"
                       "South Africa"
                       "Brazil"
                       "Indonesia"])

(def data (->> bpdata/data
               (filter #(= "2016" (:year %)))
               (mapv #(update % :country (fn [c] (get translate-countries c c))))
               (filter #(some #{(:country %)} select-countries))
               (mapv #(merge % (:per-capita %)))
               (mapv #(assoc % :total (:co2 %)))
               (sort-by :total)))

(test/is (= (count select-countries) (count data)))

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
              [{:text "Carbon Dioxide Emissions" :font "Roboto Black" :font-size 22}
               {:text "Tonnes Per Capita Per Year" :font-size 14
                :margin-bottom 0}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 10 :text "Sources: BP (2017), World Bank (2016)" :font "Roboto Regular" :font-size 14}
               {:margin-top 2
                :text "Population data from 2015." :font "Roboto Regular" :font-size 14}
               {:valign :bottom :align :right :text "Diagram: @ivarref" :font "Roboto Regular" :font-size 14}
               ]))

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 22]})

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
  {:p country :h total :fill "steelblue"})

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
  (render "./img/co2-per-capita.svg" "./img/co2-per-capita.png" (diagram)))
