(ns clj-simple-chart.bp.diagrams.coal-per-capita-sel
  (:require [clj-simple-chart.bp.bpdata2 :as bpdata]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.bp.wb-raw :as wb-raw]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.bp.units :as units]
            [clj-simple-chart.colors :as colors]
            [clojure.test :as test]))

(defn produce-data [dat]
  (->> dat
       (filter :coal_consumption_mtoe)
       (filter :population)
       (mapv #(assoc %
                :total
                (/ (* 1000.0 units/million (:coal_consumption_mtoe %))
                   (:population %))))
       (mapv #(dissoc % :gas_consumption_bm3
                      :oil_consumption_kbd
                      :oil_consumption_mtoe
                      :total_mtoe
                      :renewables_consumption_mtoe
                      :co2_emissions_mt
                      ;:coal_consumption_mtoe
                      :coal_production_mtoe
                      :coal_production_ton
                      ;:country
                      :country_code
                      :electricity_generation_twh
                      :gas_consumption_mtoe
                      :gas_production_bm3
                      :gas_production_mtoe
                      :gas_proved_reserves_trillion_cubic_metres
                      :gdp
                      :geo_biomass_other_mtoe
                      :geo_biomass_other_twh
                      :hydro_consumption_mtoe
                      :hydro_consumption_twh
                      :imports-of-goods-and-services-percentage
                      :nuclear_consumption_mtoe
                      :nuclear_consumption_twh
                      :oil_production_kbd
                      :oil_production_mtoe
                      :oil_reserves_gb
                      ;:population
                      :regular_country
                      :renewables_consumption_twh
                      :solar_consumption_mtoe
                      :solar_consumption_twh
                      ;:total
                      :wind_consumption_mtoe
                      :wind_consumption_twh
                      ;:year
                      ))
       (mapv #(update % :total double))
       (mapv #(assoc % :coal_consumption_per_capita_oe (:total %)))))

(def data (->> (produce-data bpdata/most-recent-data)
               (sort-by :total)))

(csv/write-csv-format "data/bp/coal-consumption-per-capita-oe.csv"
                      {:data    (reverse data)
                       :format  {:coal_consumption_per_capita_oe "%.1f"
                                 :coal_consumption_mtoe "%.1f"}
                       :columns [:year
                                 :country
                                 :coal_consumption_per_capita_oe
                                 :population
                                 :coal_consumption_mtoe]})

(def data (->> data
               (take-last 27)
               (remove #(some #{(:country %)}
                              ["Czech Republic"
                               "Bulgaria"
                               "Israel"
                               "Malaysia"
                               "Slovakia"
                               "Chile"
                               "Ireland"
                               "Greece"
                               "Hong Kong"]))))

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
              [{:text "Coal Consumption" :font "Roboto Black" :font-size 20}
               #_{:text "Selected countries and regions, percentage" :font "Roboto Regular" :font-size 14 :margin-bottom 0}
               {:text "Kilograms of Oil Equivalents Per Capita Per Year" :font "Roboto Regular" :font-size 14 :margin-bottom 0}
               ]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 5 :text (str "Sources: BP (" bpdata/bp-release-year "), World Bank (" wb-raw/wb-release-year ").") :font "Roboto Regular" :font-size 14}
               {:valign :bottom :align :right :text "@ivarref" :font "Roboto Regular" :font-size 14}]))

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 2500]})

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
  {:p country :h total :fill "gray"})

(defn total-text [{country :country
                   total   :total}]
  (opentype/text {:x  (point x total)
                  :dx ".20em"
                  :y  (center-point y country)
                  :dy ".32em"}
                 (format "%.0f" total)))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (rect (mapv make-rect data))
     (map total-text data)
     (axis/render-axis y)
     (axis/render-axis x)
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (render "./img/bp-svg/coal-per-capita-sel.svg" "./img/bp-png/coal-per-capita-sel.png" (diagram)))
