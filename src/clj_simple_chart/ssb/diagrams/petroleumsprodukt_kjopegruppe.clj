(ns clj-simple-chart.ssb.diagrams.petroleumsprodukt-kjopegruppe
  (:require [clj-simple-chart.ssb.data.petroleum-kjopegruppe :as datasource]
    [clojure.string :as str]
    [clj-simple-chart.translate :refer [translate translate-y]]
    [clj-simple-chart.rect :refer [bars]]
    [clj-simple-chart.chart :as chart]
    [clj-simple-chart.axis.core :as axis]
    [clj-simple-chart.core :as core]
    [clj-simple-chart.colors :refer :all]
    [clojure.string :as string]
    [clj-simple-chart.opentype :as opentype]))

(def data datasource/data)

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (->> (map :dato data)
                             (filter #(str/ends-with? % "-12")))
         :tick-format   (fn [x] (str/replace x "-12" ""))
         :domain        (map :dato data)
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (->> (map :sum data)
                                     (apply max))]})

(def header (opentype/stack
              {:width available-width}
              [{:text "Sal av petroleumsprodukt etter kjøpegruppe" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 15 :text "Heile landet, 12 månadar glidande gjennomsnitt" :font "Roboto Regular" :font-size 18}
               {:margin-bottom 3 :text "Millionar liter" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))


(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: SSB tabell 11174" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def prop->color
  [[:transport brown "Transport"]
   [:boliger-og-næringsbygg red "Boligar og næringsbygg"]
   [:industri-i-alt orange "Industri i alt"]
   [:bygg-og-anlegg blue "Bygg og anlegg"]
   [:andre cyan "Andre"]
   [:netto-direkte-import purple "Netto direkte import"]
   [:fiske-og-fangst pink "Fiske og fangst"]
   [:jordbruk-og-skogbruk green "Jordbruk og skogbruk"]
   [:offentlig-virksomhet yucky-yellow "Offentleg verksemd"]])

(def legend
  (opentype/stack {:fill         "whitesmoke"
                   :fill-opacity 0.9
                   :margin       8
                   :y            (:plot-height c)
                   :x            5
                   :grow-upwards 10}
                  {:text "Kjøpegruppe" :font "Roboto Bold" :font-size 18}
                   ;:right {:text "Mrd. km."}}
                  (for [[prop col txt] (reverse prop->color)]
                    {:rect {:fill col}
                     ;:right {:text (str/replace (format "%.1f" (get (last data) prop)) "." ",")}
                     :text txt :font "Roboto Regular" :font-size 18})))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (bars c {:p :dato :h prop->color} data)
     legend]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "img/ssb-svg/petroleumsprodukt-kjopegruppe.svg" "img/ssb-png/petroleumsprodukt-kjopegruppe.png" (diagram)))
