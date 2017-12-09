(ns clj-simple-chart.ncs.discovery.resource-class-diagram
  (:require [clj-simple-chart.ncs.discovery :as datasource]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.point :as point]
            [clojure.string :as string]))

(def data datasource/exploded-data-liquids-gboe)

(def max-y-axis (->> data
                     (filter #(= (:year %) (apply max (map :year data))))
                     (map :value)
                     (reduce + 0.0)))

(def number-columns [:shut-down-produced
                     :producing-produced
                     :remaining-reserves
                     :pdo-approved
                     :clarification
                     :likely
                     :not-evaluated])

(def light-green "#74c476")
(def light-green-two "#31a354")
(def colors {:not-evaluated      hydro-blue
             :likely             brown
             :clarification      orange
             :pdo-approved       red ;"#fd8d3c"
             :remaining-reserves "#109618"                  ; hard green
             :producing-produced light-green
             :shut-down-produced gray})






(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def available-width (- svg-width (* 2 marg)))

(def x-domain (->> data
                   (map :year)
                   (distinct)
                   (sort)
                   (vec)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (vec (distinct (flatten [(first x-domain)
                                                 (range 1970 2017 5)
                                                 (last x-domain)])))
         :tick-format   (fn [x] (cond (= x 1967) "67"
                                      (= x 2017) "17"
                                      :else x))
         :domain        x-domain
         :sub-domain    number-columns
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 40]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Funn, felt og produksjon av olje på norsk sokkel" :font "Roboto Bold" :font-size 30}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 8 :text "Kjelde: Oljedirektoratet" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))


(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))


(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (bars c {:p :year :h :value :fill colors} data)]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/discovery-overview.svg" "./img/ncs-png/nettokontantstraum.png" (diagram)))
