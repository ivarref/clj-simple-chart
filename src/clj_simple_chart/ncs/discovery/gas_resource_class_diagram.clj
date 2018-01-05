(ns clj-simple-chart.ncs.discovery.gas-resource-class-diagram
  (:require [clj-simple-chart.ncs.discovery :as datasource]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.point :as point]
            [clojure.string :as string]
            [clojure.string :as str]))

(def data datasource/exploded-data-gas-gboe)

(def max-year (apply max (map :year data)))

(def cat-to-value-last-year (->> data
                                 (filter #(= (:year %) max-year))
                                 (map (juxt :c :value))
                                 (into {})))

(def max-y-axis (->> data
                     (filter #(= (:year %) max-year))
                     (map :value)
                     (reduce + 0.0)))

(def number-columns [:shut-down-produced
                     :producing-produced
                     :remaining-reserves
                     :pdo-approved
                     :decided-for-production
                     :clarification
                     :likely
                     :not-evaluated])

(def light-green "#74c476")
(def light-green-two "#31a354")
(def colors {:not-evaluated          hydro-blue
             :likely                 brown
             :clarification          "#ffaa00"
             :decided-for-production orange
             :pdo-approved           "#109618"              ; hard green
             :remaining-reserves     red
             :producing-produced     "#eA6F6F"
             :shut-down-produced     gray})

(def text {:not-evaluated          "Ikkje evaluert"
           :likely                 "Utvinning truleg"
           :clarification          "Under avklåring"
           :decided-for-production "Vedteke for utvinning"
           :pdo-approved           "PUD**-godkjent"
           :remaining-reserves     "Gjenverande reservar, felt i drift"
           :producing-produced     "Kumulativ produksjon frå felt i drift"
           :shut-down-produced     "Kumulativ produksjon frå nedstengde felt"})

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
                                                 (range 1970 2017 5)])))
         ;(last x-domain)])))
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
         :domain             [0 30]})

(def header (opentype/stack
              {:width available-width}
              [
               {:text "Kumulativ gassproduksjon og gjenverande ressursar" :font "Roboto Bold" :font-size 30 :margin-bottom 3}
               {:text (str "Årleg produksjon " datasource/max-complete-year ": "
                           (let [v (production/yearly-production production/field-names datasource/max-complete-year :gas)]
                             (str/replace (format "%.2f" (* 6.29e-3 v)) "." ","))
                           " milliardar fat oljeekvivalentar")
                :font "Roboto Bold" :font-size 16 :margin-bottom 1}
               {:text          "Milliardar fat oljeekvivalentar" :font "Roboto Bold" :font-size 16
                :align         :right
                :valign        :bottom
                :margin-bottom 1}]))


(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 5 :text "*Betinga ressursar i funn, Utvinning ikkje evaluert (RK 7A) og Ikkje oppdaga ressursar er utelatt." :font "Roboto Regular" :font-size 14}
               {:margin-top 2 :text "Kjelde: Oljedirektoratet Faktasider. **Plan for Utvikling og Drift." :font "Roboto Regular" :font-size 14}
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
     (bars c {:p :year :h :value :fill colors} data)
     [:g {:transform (translate 10 15)}
      (opentype/stack {:widht        available-width
                       :fill         "whitesmoke"
                       :fill-opacity 0.8
                       :margin       5}
                      (vec (flatten [{:text  "Ressurskategori*" :font "Roboto Black" :font-size 16
                                      :right {:text "Mrd. fat oljeekvivalentar"}}
                                     (map (fn [col]
                                            {:text      (get text col)
                                             :font      "Roboto Regular"
                                             :font-size 16
                                             :right     {:text (str/replace (format "%.1f" (get cat-to-value-last-year col)) "." ",")}
                                             :rect      {:fill (get colors col)}})
                                          (reverse number-columns))
                                     {:text  "Totalt" :font "Roboto Bold" :font-size 16
                                      :right {:text (str/replace (format "%.1f" (reduce + 0 (vals cat-to-value-last-year))) "." ",")}}])))]]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/discovery-overview-gas.svg" "./img/ncs-png/discovery-overview-gas.png" (diagram)))
