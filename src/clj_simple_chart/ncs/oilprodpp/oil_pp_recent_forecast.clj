(ns clj-simple-chart.ncs.oilprodpp.oil-pp-recent-forecast
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.oilprodpp.oilppdata :as production]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.ncs.oilprognosis.forecastdata :as forecastdata]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.dots :as dots]
            [clj-simple-chart.area :as clj-area]
            [clj-simple-chart.area-center-text :as act]
            [clojure.string :as string]
            [clojure.string :as str]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> production/by-date
               (filter #(>= (:eofYear %) 2000))))

(def forecast (forecastdata/forecast-monthly forecastdata/current-forecast))
(def forecast-eoy (->> forecast
                       (filter #(= 12 (:prfMonth %)))))

(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

(def bucket-to-fill (zipmap buckets (reverse ["#1f77b4"
                                              "#8c564b"     ; brown
                                              "#ff7f0e"     ; orange
                                              "#d62728"     ; red
                                              ])))

(def sub-domain buckets)

(def x-domain (vec (sort (distinct
                           (flatten [(map :date data)
                                     (map :date forecast)
                                     (map :date (forecastdata/yearly-forecast-to-months
                                                  {:prfYear (inc (last (sort (map :prfYear forecast))))
                                                   :prfPrdOilNetMillSm3 0.0}))
                                     ])))))

(def x-ticks (filter #(or (= % "1971-12")
                          (.endsWith % "-12"))
                     x-domain))

(def available-width (- svg-width (* 2 marg)))

(def last-data (last data))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (read-string (last parts)))
         " " (first parts))))

(def feltmogning-ex-txt (str "Feltmogning: Prosent produsert av opprinneleg utvinnbart"))

(def header (opentype/stack
              {:width available-width}
              [{:text (str "Råoljeproduksjon etter feltmogning")
                :font "Roboto Bold" :font-size 30}
               {:text feltmogning-ex-txt :font "Roboto Bold" :font-size 16 :margin-top 1}
               {:text (str "Produksjon per " (months-str (:date last-data)) ": "
                           (string/replace (format "%.1f" (get last-data :sum)) "." ",")
                           " mill. Sm³")
                :font "Roboto Bold" :font-size 16 :margin-top 3}

               {:text "Råoljeproduksjon, mill. Sm³" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Oljedirektoratet" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (let [year (subs x 0 4)]
                                (if (or (.endsWith year "5")
                                        (.endsWith year "0"))
                                  year
                                  (subs year 2 4))))
         :orientation :bottom
         :domain      x-domain
         :sub-domain  buckets})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 200]})

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


(defn make-txt [{date :date year :year :as opts}]
  (let [year (subs date 0 4)
        fmt (if (some #{year} ["2000" "2001"]) "%.1f" "%.0f")]
    (if (or (some #{year} ["2000" "2005" "2010" "2013" ;"2016"
                           "2017"
                           "2020"])
            #_(>= (read-string year) 2017))
      [:g {:transform (translate (xfn date) (yfn (get opts :sum)))}
       [:circle {:r 2.5}]
       [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
       (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" (subs date 0 4) ")")})
       (opentype/text {:dy          "-1.71em"
                       :font        "Roboto Bold"
                       :text-anchor "middle"
                       :text        (string/replace (format fmt (get opts :sum)) "." ",")})]
      [:g {:transform (translate (xfn date) (yfn (get opts :sum)))}
       [:circle {:r 2.5}]
       [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
       #_(opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" (subs date 0 4) ")")})
       (opentype/text {:dy          "-.72em"
                       :font        "Roboto Bold"
                       :text-anchor "middle"
                       :text        (string/replace (format fmt (get opts :sum)) "." ",")})])))

(defn forecast-line []
  (->> forecast-eoy
       (mapv (fn [{:keys [date prfPrdOilNetMillSm3]}]
               {:p date :h prfPrdOilNetMillSm3}))
       (dots/dots c)))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (clj-area/area c flat)
     (forecast-line)
     [:g (map make-txt forecast-eoy)]
     [:g (map make-txt (filter #(some #{(:date %)} (mapv (fn [x] (str x "-12")) (range 2000 2017))) data))]
     (axis/render-axis (:x c))]
    (let [offset-xy 20
          infotext (opentype/stack {:fill         "lightgray"
                                    :fill-opacity 0.3
                                    :margin       5}
                                   (flatten
                                     [{:text "Feltmogningskategori" :font "Roboto Black" :font-size 16}
                                      (mapv (fn [k]
                                              {:text      (str (subs k 3) "%")
                                               :font-size 16
                                               :font      "Roboto Bold"
                                               :rect      {:fill (get bucket-to-fill k)
                                                           :size nil}})
                                            (sort (keys production/empty-buckets)))]))]
      [:g {:transform (translate (- (+ (:margin-left c) (:plot-width c))
                                    (:width (meta infotext))
                                    offset-xy)
                                 (+ offset-xy (+ (:height (meta header)) (:margin-top c))))}
       infotext])
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/oil-pp-recent-forecast.svg" "./img/ncs-png/oil-pp-recent-forecast.png" (diagram)))


