(ns clj-simple-chart.bp.diagrams.renewables-share-sel
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
            [clojure.test :as test]))

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
                       ;"Qatar"
                       ;"Singapore"
                       "Bangladesh"
                       "South Africa"
                       "Brazil"
                       "Indonesia"])

(defn sum-props [v props]
  (apply + (mapv #(get v % 0) props)))

(defn produce-data [dat]
  (->> dat
       (filter :hydro_consumption_mtoe)
       (filter :renewables_consumption_mtoe)
       (mapv #(assoc %
                :total
                (/ (* 100 (sum-props % [:hydro_consumption_mtoe :renewables_consumption_mtoe]))
                   (sum-props % [:coal_consumption_mtoe
                                 :gas_consumption_mtoe
                                 :oil_consumption_mtoe
                                 :nuclear_consumption_mtoe
                                 :hydro_consumption_mtoe
                                 :renewables_consumption_mtoe]))))
       (csv/keep-columns [:country_code :country :total])
       (mapv #(update % :total double))))

(def data-2000 (->> (produce-data bpdata/data-for-2000)
                    (mapv (juxt :country_code :total))
                    (into {})))

(def data (->> (produce-data bpdata/most-recent-data)
               (mapv #(assoc % :share-2000 (get data-2000 (:country_code %))))
               (mapv #(assoc % :change (- (:total %) (:share-2000 %))))
               (filter #(some #{(:country %)} select-countries))
               (sort-by :total)
               #_(take-last 25)))

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
              [{:text "Renewables' Share of Energy Mix" :font "Roboto Black" :font-size 20}
               #_{:text "Selected countries and regions, percentage" :font "Roboto Regular" :font-size 14 :margin-bottom 0}
               {:text "Percentage of Primary Energy Consumption" :font "Roboto Regular" :font-size 14 :margin-bottom 0}
               ]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 10 :text "See BP for conversion details." :font "Roboto Regular" :font-size 14}
               {:margin-top 2 :text "Source: BP (2017)." :font "Roboto Regular" :font-size 14}
               ;{:margin-top 2 :text       "Population data from 2015." :font "Roboto Regular" :font-size 14}
               {:valign :bottom :align :right :text "Diagram: @ivarref" :font "Roboto Regular" :font-size 14}
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
  {:p country :h total :fill colors/pink})

(defn total-text [{country :country
                   total   :total}]
  (opentype/text {:x  (point x total)
                  :dx ".20em"
                  :y  (center-point y country)
                  :dy ".32em"}
                 (format "%.0f" total)))

(defn change-text [{country    :country
                    change     :change
                    value-2000 :share-2000}]
  (let [extra-txt (if (= country (:country (first data)))
                    "Change since 2000:  "
                    "")]
    (opentype/text {:x           (point x 105)
                    :text-anchor "end"
                    :font        "Roboto Bold"
                    :y           (center-point y country)
                    :fill        (if (neg? change) colors/red colors/green)
                    :dy          ".32em"}
                   (str extra-txt (format "%.1f" change)))))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (rect (mapv make-rect data))
     (map total-text data)
     (map change-text data)
     (axis/render-axis y)
     (axis/render-axis x)
     #_[:g {:transform (translate-y (- (:plot-height c) (:height (meta legend))))}
        #_legend]]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (render "./img/bp-svg/renewables-share-sel.svg" "./img/bp-png/renewables-share-sel.png" (diagram)))

