(ns clj-simple-chart.eurostat.charts.london-ny
  (:require [clj-simple-chart.eurostat.data.avia-par-all :as datasource]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.area-center-text :as cat]
            [clj-simple-chart.area :as area]
            [clojure.string :as string]
            [clojure.string :as str]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.line :as line]
            [clj-simple-chart.dateutils :as dateutils]
            [clojure.test :as test]))

; https://ec.europa.eu/eurostat/statistics-explained/index.php/Air_transport_statistics
; Next planned update: November 2018

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def x-domain (->> datasource/data-monthly
                   (map :date)
                   (distinct)
                   (sort)
                   (vec)))

(def x-ticks (filter #(.endsWith % "-12") x-domain))

(def available-width (- svg-width (* 2 marg)))

(def fill "#d62728")

(def header (opentype/stack
              {:width available-width}
              [{:text "Topp ti destinasjonar utanfor Europa" :font "Roboto Bold" :font-size 30}
               {:text "Med avreise frå Europa*" :font "Roboto Bold" :font-size 16
                :margin-bottom 10}

               #_{:text (str "Per " (dateutils/months-str (:date (first (take-last 12 data))))
                             "–" (dateutils/months-str (:date last-data)) ": "
                             (string/replace (format "%.0f" (double (get last-data :value))) "." ",")
                             " personar/dag")
                  :font "Roboto Bold" :font-size 16 :margin-top 1 :margin-bottom 10}

               ;{:text "Årleg vekst" :font "Roboto Bold" :font-size 16 :margin-top 10 :fill yoy-fill :margin-bottom 2}
               ;{:text "5 år glidande gjennomsnitt" :font "Roboto Bold" :font-size 16 :fill yoy-fill :margin-bottom 3}
               {:text "Luftpassasjerar per dag, '000" :font "Roboto Bold" :margin-top 10 :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande gjennomsnitt" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4
                :text (str "*Europa inkluderer EU28, kandidatlanda, Noreg, Island og Sveits. **Inkluderer New Jersey.")
                :font "Roboto Regular" :font-size 14}
               {:margin-top 4
                :text "Kjelde: Eurostat (avia_par_*, passengers carried)."
                :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def color
  ["#2ca02c"
   "#d62728"
   "#ff7f0e"
   "#1f77b4"
   "#17becf"
   "#e377c2"
   "#9467bd"
   "#bcbd22"
   "#7f7f7f"
   "#a1d99b"])

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :sub-domain  (reverse datasource/top-ten-dests)
         :fill        (vec (reverse color))
         :domain      x-domain})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         ;:tick-format        (fn [x] (str/replace (format "%.1f" x) "." ","))
         :domain             [0 250 #_(Math/ceil (* 1.1 (apply max (map :value data))))]})


(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     (assoc yy :grid nil
                                       :orientation :left)}))
;"#d62728",  //red
;"#ff7f0e", //orange
;"#8c564b", //brown
;"#1f77b4", //blue
;"#e377c2", //pink
;"#17becf", //cyan
;"#bcbd22", //gusjegul
;"#9467bd", //purple
;"#7f7f7f", //gray

(def starify {"New York" "**"})

(def city-and-color
  (map-indexed (fn [idx x] [x (nth color idx)]) datasource/top-ten-dests))


(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}

     (axis/render-axis (:y c))

     (concat [:g] (area/area (merge c {:h :value
                                       :c :to
                                       :p :date})
                             datasource/data-monthly))
     (concat [:g] (cat/area-center-text c {:h :value
                                           :c :to
                                           :p :date}
                             (->> datasource/data-monthly
                                  (filter #(= (:date-int %) datasource/max-date))
                                  (mapv #(assoc % :text {:text (format "%d" (Math/round (double (:value %))))
                                                         :font "Roboto Bold"
                                                         :fill "white"})))))
     (axis/render-axis (:y2 c))

     (axis/render-axis (:x c))]
    (let [infotext (opentype/stack {:fill         "white"
                                    :fill-opacity 0.75
                                    :margin       5}
                                   (flatten
                                     [{:text          "Destinasjon" :font "Roboto Black" :font-size 16
                                       :margin-bottom 2}
                                      (mapv (fn [[city fill]]
                                              {:text      (str city (get starify city ""))
                                               :font-size 16
                                               :font      "Roboto Bold"
                                               :rect      {:fill fill
                                                           :size nil}})
                                            city-and-color)]))]
      [:g {:transform (translate (+ (:margin-left c) 5)
                                 (+ (:height (meta header))
                                    (:margin-top c)
                                    ;(:plot-height c)
                                    0
                                    #_(- (:height (meta infotext)))))}
       infotext])
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/eurostat-svg/extra-eu.svg" "./img/eurostat-png/extra-eu.png" (diagram)))
