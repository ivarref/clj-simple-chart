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



(defn mma [dat]
  (->> dat
       (map #(assoc % :prev-rows (take-last 12 (take (inc (:idx %)) dat))))
       (map #(assoc % :value (/ (apply + (map :value (:prev-rows %)))
                                (dateutils/prev-12-months-num-days (:date %)))))
       (filter #(= 12 (count (:prev-rows %))))))

(defn do-mma [from to]
  (mma (->> datasource/data-monthly
            (filter #(and (= from (:from %))
                          (= to (:to %))))
            (map-indexed (fn [idx x] (assoc x :idx idx)))
            (vec))))

(def data-pre (->> datasource/data-monthly
                   ;(filter #(= "NO_ENCN_NO_ENGM" (:airp_pr %)))
                   (filter #(and (= "Oslo" (:from %))
                                 (= "Trondheim" (:to %))))
                   (map-indexed (fn [idx x] (assoc x :idx idx)))
                   (vec)))

(test/is (= (count (dateutils/date-range (:date (first data-pre)) (:date (last data-pre))))
            (count data-pre)))

(def data (do-mma "Oslo" "Trondheim"))

(def oslo-bergen)

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
              [{:text "Antall luftpassasjerar per dag"
                #_(str "Antall luftpassasjerar, "
                       (:from (first data))
                       " - "
                       (:to (first data)))
                :font "Roboto Bold" :font-size 30}
               #_{:text (str "Per " (dateutils/months-str (:date (first (take-last 12 data))))
                             "–" (dateutils/months-str (:date last-data)) ": "
                             (string/replace (format "%.0f" (double (get last-data :value))) "." ",")
                             " personar/dag")
                  :font "Roboto Bold" :font-size 16 :margin-top 1 :margin-bottom 10}

               ;{:text "Årleg vekst" :font "Roboto Bold" :font-size 16 :margin-top 10 :fill yoy-fill :margin-bottom 2}
               ;{:text "5 år glidande gjennomsnitt" :font "Roboto Bold" :font-size 16 :fill yoy-fill :margin-bottom 3}
               {:text "Antall luftpassasjerar per dag" :font "Roboto Bold" :margin-top 10 :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande gjennomsnitt" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Eurostat (avia_par_no, passengers carried). London inkluderer Heathrow, Gatwick og Stansted." :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :domain      x-domain})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         ;:tick-format        (fn [x] (str/replace (format "%.1f" x) "." ","))
         :domain             [0 (Math/ceil (* 1.1 (apply max (map :value data))))]})


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

(def city-and-color
  [["Trondheim" "#2ca02c"]
   ["Bergen" "#d62728"]
   ["Stavanger" "#ff7f0e"]
   ["København" "#1f77b4"]
   ["Stockholm" "#17becf"]
   ["Tromsø" "#e377c2"]
   ["London" "#9467bd"]
   ["Bodø" "#bcbd22"]
   ["Amsterdam" "#7f7f7f"]])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (axis/render-axis (:y2 c))

     (concat [:g] (mapv (fn [[city fill]]
                          (line/line c {:h    :value
                                        :p    :date
                                        :path {:stroke       fill
                                               :stroke-width 3.5}}
                                     (do-mma "Oslo" city))) city-and-color))

     (axis/render-axis (:x c))]
    (let [infotext (opentype/stack {:fill         "white"
                                    :fill-opacity 0.75
                                    :margin       5}
                                   (flatten
                                     [{:text          "Rute" :font "Roboto Black" :font-size 16
                                       :margin-bottom 2}
                                      (mapv (fn [[city fill]]
                                              {:text      (str "Oslo - " city)
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
  (core/render "./img/eurostat-svg/norsk-flytrafikk.svg" "./img/eurostat-png/norsk-flytrafikk.png" (diagram)))
