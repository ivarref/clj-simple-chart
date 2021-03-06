(ns clj-simple-chart.ncs.reserves.petroleum-reserves-over-time
  (:require [clj-simple-chart.ncs.johan-highlight-data-petroleum :as datasource]
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

(def data datasource/exploded-data-petroleum-gboe)

(def max-year (apply max (map :year data)))

(def colors {"EKOFISK"           blue
             "STATFJORD"         red
             "GULLFAKS"          orange
             "SNORRE"            "#ffaa00"
             "OSEBERG"           "#9467bd"
             "TROLL"             green
             "ÅSGARD"            cyan
             "SNØHVIT"           hydro-blue
             "ORMEN LANGE"       "#bcbd22"
             "OTHERS_PRE_1990"   "#969696"                  ;""#7f7f7f"
             "OTHERS_POSTE_1990" "#a1d99b"                  ;"#B6b6b6"
             "OTHERS_POSTE_2000" "rgb(231, 186, 82)"
             "JOHAN SVERDRUP"    dark-purple
             "JOHAN CASTBERG"    pink})

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
                                                 (range 1970 (last x-domain) 5)
                                                 (last x-domain)])))
         ;(last x-domain)])))
         :tick-format   (fn [x] (cond (= x 1967) "67"
                                      (= x 2018) "18"
                                      :else x))
         :domain        x-domain
         :sub-domain    columns
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 50]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Gjenverande petroleumsreservar* over tid" :font "Roboto Bold" :font-size 30}
               {:text "Dei ti største petroleumsfelta, Johan Castberg og alle andre felt" :font "Roboto Bold" :font-size 16}
               {:text          "Milliardar fat oljeekvivalentar" :font "Roboto Bold" :font-size 16
                :align         :right
                :margin-bottom 3
                :valign        :bottom}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 5 :text "Kjelde: OD. *Inkluderer ressursklassane 1, 2 og 3. \"Petroleum\" inkluderer råolje, gass, kondensat og NGL." :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def txt {"OTHERS"            (str "Alle andre "
                                   (count datasource/reserve-field-names)
                                   " felt")
          "OTHERS_PRE_1990"   (str "Andre felt funne før 1990")
          "OTHERS_POSTE_1990" (str "Andre felt funne 1990–2000")
          "OTHERS_POSTE_2000" (str "Andre felt funne f.o.m. 2000")
          "JOHAN CASTBERG"    "Johan Castberg"
          "JOHAN SVERDRUP"    "Johan Sverdrup"
          "HEIDRUN"           "Heidrun"
          "ÅSGARD"            "Åsgard"
          "SNØHVIT"           "Snøkvit"
          "OSEBERG"           "Oseberg"
          "ORMEN LANGE"       "Ormen Lange"
          "TROLL"             "Troll"
          "SNORRE"            "Snorre"
          "GULLFAKS"          "Gullfaks"
          "VALHALL"           "Valhall"
          "STATFJORD"         "Statfjord"
          "EKOFISK"           "Ekofisk"})

(def cat-to-value-last-year (->> data
                                 (filter #(= (:year %) max-year))
                                 (map (juxt :c :value))
                                 (into {})))

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
                       :fill-opacity 0.6
                       :margin       5}
                      (vec (flatten [{:text "Felt" :font "Roboto Black" :font-size 16}
                                     ;:right {:text "Mrd. fat olje"}}
                                     (map (fn [col]
                                            {:text      (get txt col col)
                                             :font      "Roboto Regular"
                                             :font-size 16
                                             ;:right     {:text (str/replace (format "%.1f" (get cat-to-value-last-year col)) "." ",")}
                                             :rect      {:fill (get colors col)}})
                                          (reverse columns))])))]]
    ;{:text  "Totalt" :font "Roboto Bold" :font-size 16 :right {:text (str/replace (format "%.1f" (reduce + 0 (vals cat-to-value-last-year))) "." ",")}}])))]]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/remaining-reserves-petroleum.svg" "./img/ncs-png/remaining-reserves-petroleum.png" (diagram)))
