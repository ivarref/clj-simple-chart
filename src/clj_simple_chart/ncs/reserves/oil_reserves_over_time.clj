(ns clj-simple-chart.ncs.reserves.oil-reserves-over-time
  (:require [clj-simple-chart.ncs.johan-highlight-data :as datasource]
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

(def data datasource/exploded-data-liquids-gboe)

(def max-year (apply max (map :year data)))

(def colors {"EKOFISK"           blue
             "STATFJORD"         red
             "VALHALL"           orange
             "GULLFAKS"          "#ffaa00"
             "SNORRE"            "#9467bd"
             "TROLL"             green
             "OSEBERG"           cyan
             "ÅSGARD"            hydro-blue
             "HEIDRUN"           "#bcbd22"
             "OTHERS_PRE_1990"   "#969696"
             "OTHERS_POSTE_1990" "#a1d99b"
             "OTHERS_POSTE_2000" "rgb(231, 186, 82)"
             "JOHAN SVERDRUP"    dark-purple
             "JOHAN CASTBERG"    pink})

(def txt {"OTHERS_PRE_1990"   (str "Andre felt funne før 1990")
          "OTHERS_POSTE_1990" (str "Andre felt funne 1990–2000")
          "OTHERS_POSTE_2000" (str "Andre felt funne f.o.m. 2000")
          "JOHAN CASTBERG"    "Johan Castberg"
          "JOHAN SVERDRUP"    "Johan Sverdrup"
          "HEIDRUN"           "Heidrun"
          "ÅSGARD"            "Åsgard"
          "OSEBERG"           "Oseberg"
          "TROLL"             "Troll"
          "SNORRE"            "Snorre"
          "GULLFAKS"          "Gullfaks"
          "VALHALL"           "Valhall"
          "STATFJORD"         "Statfjord"
          "EKOFISK"           "Ekofisk"})

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

(def columns (mapv first datasource/cat-name-and-flds))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (vec (distinct (flatten [(first x-domain)
                                                 (range 1970 2017 5)])))
         :tick-format   (fn [x] (cond (= x 1967) "67"
                                      (= x 2017) "17"
                                      :else x))
         :domain        x-domain
         :sub-domain    columns
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 26]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Gjenverande oljereservar* over tid" :font "Roboto Bold" :font-size 30}
               {:text "Dei ti største oljefelta, Johan Castberg og alle andre felt" :font "Roboto Bold" :font-size 16}
               {:text          "Milliardar fat olje" :font "Roboto Bold" :font-size 16
                :align         :right
                :margin-bottom 3
                :valign        :bottom}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 5 :text "Kjelde: OD. *Inkluderer ressursklassane 1, 2 og 3. \"Olje\" inkluderer råolje, kondensat og NGL." :font "Roboto Regular" :font-size 14}
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
      (opentype/stack {:fill         "whitesmoke"
                       :fill-opacity 0.6
                       :margin       5}
                      (vec (flatten [{:text "Felt" :font "Roboto Black" :font-size 16}
                                     (map (fn [col]
                                            {:text      (get txt col col)
                                             :font      "Roboto Regular"
                                             :font-size 16
                                             :rect      {:fill (get colors col)}})
                                          (reverse columns))])))]]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/remaining-reserves-oil.svg" "./img/ncs-png/remaining-reserves-oil.png" (diagram)))
