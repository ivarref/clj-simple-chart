(ns clj-simple-chart.ncs.gas-rp-diagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.production-monthly-field :as production]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clojure.string :as string]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> production/by-date))
(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

(def bucket-to-fill (zipmap (sort (keys production/empty-buckets))
                            ["red"
                             "green"
                             "cyan"
                             "yellow"
                             "blue"
                             "orange"]))

(def sub-domain buckets)

(def x-domain (map :date data))

(def x-ticks (filter #(or (= (first x-domain) %)
                          (.endsWith % "5-12")
                          (.endsWith % "0-12"))
                     (map :date data)))

(def available-width (- svg-width (* 2 marg)))

(def last-data (last data))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (read-string (last parts)))
         " " (first parts))))

(def header (opentype/stack
              {:width available-width}
              [{:text "Gassproduksjon etter R/P" :font "Roboto Bold" :font-size 30}
               {:text       (str "Per " (months-str (:date last-data)) ": "
                                 (string/replace (format "%.1f" (get last-data :sum)) "." ",")
                                 " mrd Sm³")
                :font       "Roboto Bold" :font-size 16
                :margin-top 3}
               {:text       "R/P = Reservar / Produksjon, gjenverande levetid i år"
                :font       "Roboto Bold" :font-size 16
                :margin-top 3}
               {:text "Gassproduksjon, mrd Sm³" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Oljedirektoratet" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}
               ]))

(def xx {:type          :ordinal
         :tick-values   x-ticks
         :tick-format   (fn [x] (if (.endsWith x "5-12")
                                  (subs x 2 4)
                                  (subs x 0 4)))
         :orientation   :bottom
         :domain        x-domain
         :sub-domain    sub-domain
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 130]})

(def available-height (- svg-height (+ two-marg
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

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  (map (fn [[k fill]]
         {:p    (:date opts)
          :c    k
          :fill fill
          :h    (get opts k 0)})
       bucket-to-fill))

(defn make-txt [{dato :date year :year :as opts}]
  [:g {:transform (translate (xfn dato) (yfn (get opts :sum)))}
   [:circle {:r 2}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" (subs dato 0 4) ")")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.0f" (get opts :sum)) "." ",")})])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     [:g (bars (mapv make-rect data))]
     [:g (map make-txt (filter #(some #{(:date %)}
                                      ["1980-12"
                                       "1985-12"
                                       "1990-12"
                                       "1995-12"
                                       "2000-12"
                                       "2005-12"
                                       "2010-12"
                                       ;"2013-12"
                                       "2015-12"]) data))]
     (axis/render-axis (:x c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (core/render "./img/ncs-svg/gas-rp.svg" "./img/ncs-png/gas-rp.png" (diagram)))

