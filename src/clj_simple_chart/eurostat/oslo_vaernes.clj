(ns clj-simple-chart.eurostat.oslo-vaernes
  (:require [clj-simple-chart.eurostat.avia-par-no :as datasource]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clojure.string :as string]
            [clojure.string :as str]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.line :as line]
            [clj-simple-chart.dateutils :as dateutils]
            [clojure.test :as test]))

(def data-pre (->> datasource/data-monthly
                   ;(filter #(= "NO_ENCN_NO_ENGM" (:airp_pr %)))
                   (filter #(and (= "Bergen" (:from %))
                                 (= "Oslo" (:to %))))
                   (map-indexed (fn [idx x] (assoc x :idx idx)))
                   (vec)))

(test/is (= (count (dateutils/date-range (:date (first data-pre)) (:date (last data-pre))))
            (count data-pre)))

(def data (->> data-pre
               (map #(assoc % :prev-rows (take-last 12 (take (inc (:idx %)) data-pre))))
               (map #(assoc % :value (/ (apply + (map :value (:prev-rows %)))
                                        (dateutils/prev-12-months-num-days (:date %)))))
               (filter #(= 12 (count (:prev-rows %))))))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def x-domain (map :date data))

(def x-ticks (filter #(.endsWith % "-12") x-domain))

(def available-width (- svg-width (* 2 marg)))
(def last-data (last data))

(def fill "#d62728")

(def header (opentype/stack
              {:width available-width}
              [{:text (str "Antall Luftpassasjerar, "
                           (:from (first data))
                           " - "
                           (:to (first data)))
                :font "Roboto Bold" :font-size 30}
               {:text (str "Per " (dateutils/months-str (:date (first (take-last 12 data))))
                           "–" (dateutils/months-str (:date last-data)) ": "
                           (string/replace (format "%.0f" (double (get last-data :value))) "." ",")
                           " personar/dag")
                :font "Roboto Bold" :font-size 16 :margin-top 1 :margin-bottom 10}

               ;{:text "Årleg vekst" :font "Roboto Bold" :font-size 16 :margin-top 10 :fill yoy-fill :margin-bottom 2}
               ;{:text "5 år glidande gjennomsnitt" :font "Roboto Bold" :font-size 16 :fill yoy-fill :margin-bottom 3}
               {:text "Antall luftpassasjerar per dag" :font "Roboto Bold" :margin-top 30 :font-size 16 :fill fill :valign :bottom :align :right}
               {:text "12 månadar glidande gjennomsnitt" :margin-top 1 :font "Roboto Bold" :font-size 16 :fill fill :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Eurostat (datasett avia_par_no, passengers carried)" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :domain      x-domain})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold" :fill fill})
         ;:tick-format        (fn [x] (str/replace (format "%.1f" x) "." ","))
         :domain             [0 (Math/ceil (* 1.1 (apply max (map :value data))))]})


(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     ;:y2     y2
                     }))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (line/line c {:h         :value
                   ;:dot       (fn [x] (str/ends-with? (:date x) "12"))
                   :dot-style {:fill         fill
                               :r            4.5
                               :stroke       "black"
                               :stroke-width 2.0}
                   :path      {:stroke       fill
                               :stroke-width 3.5}
                   :p         :date} data)
     (axis/render-axis (:x c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/eurostat-svg/NO_ENGM_NO_ENVA.svg" "./img/eurostat-png/NO_ENGM_NO_ENVA.png" (diagram)))
