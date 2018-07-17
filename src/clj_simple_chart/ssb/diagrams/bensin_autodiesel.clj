(ns clj-simple-chart.ssb.diagrams.bensin-autodiesel
  (:require [clojure.string :as str]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :refer [bars]]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clj-simple-chart.data.utils :refer :all]
            [clj-simple-chart.dateutils :as dateutils]
            [clj-simple-chart.point :as point]))

; Tabell 11174
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def data (->> {[:ContentsCode :as :salg]               "Salg"
                "Region"                                "Hele landet"
                [:PetroleumProd :as :petroleumsprodukt] ["Bilbensin" "Autodiesel"]
                [:Kjopegrupper :as :kjopegruppe]        "Alle kjøpegrupper"
                [:Tid :as :dato]                        "*"}
               (ssb/fetch 11174)
               (drop-columns [:ContentsCodeCategory :Region :kjopegruppe])
               (remove #(= "Petroleumsprodukter i alt" (:petroleumsprodukt %)))
               (column-value->column :petroleumsprodukt)
               (contract-by-column :dato)
               (remove-whitespace-in-keys)
               (flat->12-mms)
               (add-sum-column)))


(def data2 (->> {[:ContentsCode :as :salg] "Salg"
                 "Region" "Hele landet"
                 [:PetroleumProd :as :petroleumsprodukt] ["Bilbensin" "Autodiesel"]
                 [:Kjopegrupper :as :kjopegruppe] "Alle kjøpegrupper"
                 [:Tid :as :dato] "*"}
                (ssb/fetch 11174)
                (drop-columns [:ContentsCodeCategory :Region :kjopegruppe])
                (remove #(= "Petroleumsprodukter i alt" (:petroleumsprodukt %)))
                (column-value->column :petroleumsprodukt)
                (contract-by-column :dato)
                (remove-whitespace-in-keys)
                ;(flat->12-mms)
                (add-sum-column)
                (keep-columns [:sum :dato])))

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
         ;:grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 5000]})

(def lst (last data))

(def header (opentype/stack
              {:width available-width}
              [{:text "Sal av petroleumsprodukt etter type" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 15
                :text          (str "Heile landet, 12 månadar glidande sum. Per " (dateutils/months-str (:dato lst)) ": " (format "%.0f" (:sum lst)) " mill. liter.")
                :font          "Roboto Regular" :font-size 18}
               {:margin-bottom 3 :text "Millionar liter" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: SSB tabell 11174" :font "Roboto Regular" :font-size 14}
               {:text "Diagram: Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def prop->color
  [[:autodiesel brown "Autodiesel"]
   ;[:marine-gassoljer red "Marine gassoljer"]
   ;[:jetparafin orange "Jetparafin"]
   [:bilbensin blue "Bilbensin"]])
;[:anleggsdiesel cyan "Anleggsdiesel"]
;[:andre-petroleumsprodukt purple "Andre petroleumsprodukt"]
;[:lett-fyringsolje pink "Lett fyringsolje"]
;[:tungdestillat-og-tungolje green "Tungdestillat og tungolje"]
;[:fyringsparafin yucky-yellow "Fyringsparafin"]])

(def legend
  (opentype/stack {:fill         "whitesmoke"
                   :fill-opacity 0.8
                   :margin       8
                   :y            (:plot-height c)
                   :x            5
                   :grow-upwards 10}
                  {:text "Petroleumsprodukt" :font "Roboto Bold" :font-size 18}
                  (for [[prop col txt] (reverse prop->color)]
                    {:rect {:fill col}
                     :text txt :font "Roboto Regular" :font-size 18})))

(def yfn (partial point/center-point (:y c)))
(def xfn (partial point/center-point (:x c)))

(defn make-txt [{:keys [dato sum]}]
  [:g {:transform (translate (xfn dato) (yfn sum))}
   [:circle {:r 2.5}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (subs dato 0 4)})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (format "%.0f" sum)})])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:x c))
     (axis/render-axis (:y c))
     (bars c {:p :dato :h prop->color} data)
     [:g (->> data
              (filter #(str/ends-with? (:dato %) "-12"))
              (map make-txt))]
     legend]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "img/ssb-svg/bensin-autodiesel.svg" "img/ssb-png/bensin-autodiesel.png" (diagram)))
