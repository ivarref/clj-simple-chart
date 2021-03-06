(ns clj-simple-chart.ncs.liquidprodpp.liquid-pp-forecast
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.liquidprodpp.liquidppdata :as production]
            [clj-simple-chart.ncs.raw-production :refer [date-range]]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.area :as clj-area]
            [clj-simple-chart.area-center-text :as act]
            [clojure.string :as string]
            [clojure.string :as str]
            [clj-simple-chart.ncs.oilforecast.forecastdata :as forecastdata]
            [clj-simple-chart.line :as line]
            [clojure.edn :as edn]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> production/by-date
               (filter #(>= (:eofYear %) 2000))))

(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

;"#d62728",  //red
;"#ff7f0e", //orange
;"#8c564b", //brown
;"#1f77b4", //blue
;"#e377c2", //pink
;"#17becf", //cyan
;"#bcbd22", //gusjegul
;"#9467bd", //purple
;"#7f7f7f", //gray
;"#2ca02c", //green

(def bucket-to-fill (zipmap buckets
                            (reverse [
                                      "#e377c2"             ; pink
                                      "#2ca02c"             ;green
                                      "#9467bd"             ; purple
                                      "#17becf"             ; cyan
                                      "#1f77b4"             ; blue

                                      "#8c564b"             ; brown
                                      "#ff7f0e"             ; orange
                                      "#d62728"])))             ; red
                                      ;"#9467bd"                      ; purple


(def sub-domain buckets)

(def forecast (forecastdata/forecast-monthly-eoy forecastdata/current-forecast))

(def x-domain (date-range (:date (first data)) "2023-12"))

(def x-ticks (filter #(or (= % "1971-12")
                          (.endsWith % "5-12")
                          (.endsWith % "0-12"))
                     x-domain))

(def available-width (- svg-width (* 2 marg)))

(def last-data (last data))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (edn/read-string (last parts)))
         " " (first parts))))

(def feltmogning-ex-txt (str "Nedtappingsgrad: Prosent produsert av opprinneleg utvinnbart"))
                             ;(:prfYear (first data)) "–" (:prfYear (last data))



(def header (opentype/stack
              {:width available-width}
              [{:text (str "Oljeproduksjon etter nedtappingsgrad")
                :font "Roboto Black" :font-size 30}
               {:text (str "Og ODs prognose for 2018–2022")
                :font "Roboto Bold" :font-size 24}
               {:text (str "Produksjon per " (months-str (:date last-data)) ": "
                           (string/replace (format "%.2f" (get last-data :sum)) "." ",")
                           " millionar fat/dag")
                :font "Roboto Bold" :font-size 16 :margin-top 1}
               {:text feltmogning-ex-txt :font "Roboto Regular" :font-size 16 :margin-top 3}

               {:text "Oljeproduksjon, millionar fat/dag" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande gjennomsnitt" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Oljedirektoratet. \"Olje\" inkluderer råolje, kondensat og NGL." :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :domain      x-domain
         :sub-domain  buckets})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :tick-format        (fn [x] (str/replace (format "%.1f" x) "." ","))
         :domain             [0 3.75]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def x (:x c))
(def y (:y c))

(def yfn (partial point/center-point y))
(def xfn (partial point/center-point x))

(defn make-rect [opts]
  (map (fn [[k fill]]
         {:p                (:date opts)
          :c                k
          :stroke           "black"
          :stroke-width-top 1.4
          :fill             fill
          :text
                            {:fill      "white"
                             :font      "Roboto Black"
                             :font-size 15
                             :text      (str/replace (format "%.1f" (double (get opts k 0)))
                                                     "." ",")}
          :h                (get opts k 0)})
       bucket-to-fill))

(def flat (vec (flatten (mapv make-rect data))))

(def last-text (vec (flatten (make-rect (last data)))))


(defn make-txt [{dato :date year :year :as opts}]
  [:g {:transform (translate (xfn dato) (yfn (get opts :sum)))}
   [:circle {:r 2.5}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "'" (subs dato 2 4) "")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.2f" (get opts :sum)) "." ",")})])

(def forecast-color "black")

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (clj-area/area c flat)
     (line/line c {:p         :date
                   :path      {:stroke forecast-color :stroke-opacity 1.0 :stroke-width 3}
                   :dot       true
                   :dot-style {:fill "yellow" :stroke "black" :stroke-width 1 :r 4}
                   :h         :prfPrdLiquidsNetMillMboed} forecast)
     [:g {:transform (translate (xfn "2020-12") (yfn 1.6))}
      (opentype/text {:text "OD Prognose" :font "Roboto Black" :font-size 14 :text-anchor "middle"})
      (opentype/text {:dy 20 :text "Per 11.01.2018" :font "Roboto Regular" :font-size 14 :text-anchor "middle"})]
     [:g (map make-txt (filter #(some #{(:date %)}
                                      [;"1971-12"
                                       "1975-12"
                                       "1980-12"
                                       "1985-12"
                                       "1990-12"
                                       "1995-12"
                                       ;"2000-12"
                                       "2001-12"
                                       "2005-12"
                                       "2010-12"
                                       "2013-12"
                                       ;"2015-12"
                                       "2016-12"
                                       "2017-12"]) data))]
     [:g (map make-txt (->> forecast (mapv #(assoc % :sum (:prfPrdLiquidsNetMillMboed %)))))]
     (axis/render-axis (:x c))]
    (let [infotext (opentype/stack {:fill         "white"
                                    :fill-opacity 0.75
                                    :margin       5}
                                   (flatten
                                     [{:text          "Nedtappingsgrad" :font "Roboto Black" :font-size 16
                                       :margin-bottom 2}
                                      (mapv (fn [k]
                                              {:text      (str (subs k 3) "%")
                                               :font-size 16
                                               :font      "Roboto Bold"
                                               :rect      {:fill (get bucket-to-fill k)
                                                           :size nil}})
                                            (sort (keys production/empty-buckets)))
                                      #_{:text "Siste tall for kategori i kvitt" :font "Roboto Regular" :font-size 14}]))]
      [:g {:transform (translate (+ (:margin-left c) 15)
                                 (+ (:height (meta header))
                                    (:margin-top c)
                                    (:plot-height c)
                                    -15
                                    (- (:height (meta infotext)))))}
       infotext])

    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/liquids-pp-forecast.svg" "./img/ncs-png/liquids-pp-forecast.png" (diagram)))
