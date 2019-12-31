(ns clj-simple-chart.ncs.gas-rp-diagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.production-monthly-field :as production]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.area :as clj-area]
            [clj-simple-chart.area-center-text :as act]
            [clojure.string :as string]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (filter #(<= 1990 (:eofYear %)) production/by-date))
(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

;"#d62728",  //red
;"#ff7f0e", //orange
;"#8c564b", //brown
;"#1f77b4", //blue
;"#17becf", //cyan
;"#e377c2", //pink
;"#bcbd22", //gusjegul
;"#9467bd", //purple
;"#7f7f7f", //gray
;"#2ca02c", //green

(def bucket-to-fill (zipmap (sort (keys production/empty-buckets))
                            [
                             "#d62728"                      ; red
                             "#ff7f0e"                      ; orange
                             "#8c564b"                      ; brown
                             "#1f77b4"                      ; blue
                             "#e377c2"                      ; pink
                             "#9467bd"]))                   ; purple


(def sub-domain buckets)

(def x-domain (map :date data))

(def x-ticks (filter #(or (.endsWith % "5-12")
                          (.endsWith % "0-12"))
                     (map :date data)))

(def available-width (- svg-width (* 2 marg)))

(def last-data (last data))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (edn/read-string (last parts)))
         " " (first parts))))

(def header (opentype/stack
              {:width available-width}
              [{:text "Gassproduksjon etter R/P" :font "Roboto Bold" :font-size 30}
               {:text "R/P: Reservar / Produksjon, gjenverande levetid i år" :font "Roboto Bold" :font-size 16 :margin-top 1}
               {:text (str "Produksjon per " (months-str (:date last-data)) ": "
                           (string/replace (format "%.1f" (get last-data :sum)) "." ",")
                           " mrd. Sm³")
                :font "Roboto Bold" :font-size 16 :margin-top 3}

               {:text "Gassproduksjon, mrd. Sm³" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Oljedirektoratet" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (subs x 0 4))
         :orientation :bottom
         :domain      x-domain
         :sub-domain  (reverse sub-domain)})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 140]})

(def available-height (- svg-height (+ (+ 4 marg)
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
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "" (subs dato 0 4) "")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.0f" (get opts :sum)) "." ",")})])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    ;[:g {:transform (translate (:margin-left c) 0)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (clj-area/area c flat)
     ;(act/area-center-text c last-text)
     [:g (map make-txt (filter #(some #{(:date %)}
                                      ["1980-12"
                                       "1985-12"
                                       "1990-12"
                                       "1995-12"
                                       "2000-12"
                                       "2005-12"
                                       "2010-12"
                                       ;"2013-12"
                                       "2015-12"
                                       "2017-12"
                                       "2018-12"]) data))]
     (axis/render-axis (:x c))]
    [:g {:transform (translate 0 (+ 0 (+ (:height (meta header)) (:margin-top c))))}
     (opentype/stack {:width available-width}
                     (flatten
                       [{:text "R/P-kategori" :font "Roboto Black" :font-size 16}
                        (mapv (fn [k]
                                {:text      (if (= "TROLL" (subs k 3))
                                              (str "Troll, R/P = " (format "%.0f" (:gas-rp production/troll)) "")
                                              (subs k 3))
                                 :font-size 16
                                 :font      "Roboto Bold"
                                 :rect      {:fill (get bucket-to-fill k)
                                             :size nil}})
                              (reverse (sort (keys production/empty-buckets))))
                        #_{:text "Siste tall for kategori i kvitt" :font "Roboto Regular" :font-size 14}]))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ncs-svg/gas-rp.svg" "./img/ncs-png/gas-rp.png" (diagram)))
