;; Basert på data frå
;; https://www.ssb.no/skatteregn
;; Her står òg neste publiseringsdato (typisk midten av kvar månad).

(ns clj-simple-chart.ssb.petroskattdiagrammonthly
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.ssb.brentoilprice :as brentoilprice]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer [translate]]
            [clojure.test :as test]
            [clojure.string :as string])
  (:import (java.time YearMonth)))

(def ordinaer (keyword "Ordinær skatt på utvinning av petroleum"))
(def saerskatt (keyword "Særskatt på utvinning av petroleum"))

(def oil-date-to-usd brentoilprice/brent-12-mma-dato-to-nok)

(defn six-months-ago [s]
  {:pre [(string? s)]}
  (let [parts (string/split s #"-0?")
        year (read-string (first parts))
        month (read-string (last parts))
        six-m-ago (.minusMonths (YearMonth/of year month) 6)]
    (format "%04d-%02d" (.getYear six-m-ago) (.getMonthValue six-m-ago))))

(def data (->> petroskatt/twelve-mms-mrd
               (mapv #(assoc % :year (read-string (subs (:dato %) 0 4))))
               (mapv #(assoc % :sum (+ (get % ordinaer) (get % saerskatt))))
               (mapv #(assoc % :oilprice (get oil-date-to-usd (six-months-ago (:dato %)) ::none)))
               #_(filter #(>= (:year %) 2010))))

(test/is (= 0 (count (filter #(= ::none (:oilprice %)) data))))

(def x-domain (map :dato data))

(def svg-width 900)
(def svg-height 500)

(def marg 10)

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")
(def green "rgb(44, 160, 44)")
(def brown "rgb(140, 86, 75)")
(def red "rgb(214, 39, 40)")

(def saerskatt-fill green)
(def ordinaer-fill blue)

(def oil-fill red)

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn date-readable [d]
  (let [pieces (string/split d #"-0?")
        year (first pieces)
        month (nth months (read-string (second pieces)))]
    (str month " " year)))

(def last-data (last data))

(def siste-verdi-str
  (str "Per " (date-readable (:dato last-data)) ": "
       (string/replace (format "%.1f" (:sum last-data)) "." ",")
       " mrd. NOK"))

(def header (opentype/stack
              {:margin-top  5
               :margin-left marg}
              [{:text "Innbetalt petroleumsskatt" :font "Roboto Bold" :font-size 36}
               {:text (str "Milliardar NOK, 12 månadar glidande sum. " siste-verdi-str) :font "Roboto Black" :font-size 16}
               ;{:text siste-verdi-str :font-size 16 :font "Roboto Regular" :margin-bottom 10}
               #_{:text "Særskatt på utvinning av petroleum" :fill saerskatt-fill :font "Roboto Black" :font-size 16}
               #_{:text "Ordinær skatt på utvinning av petroleum" :fill ordinaer-fill :font "Roboto Black" :font-size 16}
               {:margin-top 16 :text "Oljepris, NOK/fat" :fill oil-fill :font "Roboto Black" :font-size 16}
               {:text "12 månadar glidande gjennomsnitt, 6 månadar framskyvd" :fill oil-fill :font "Roboto Black" :font-size 14}
               ]))

(def info-right
  [:g {:transform (translate (- svg-width marg) (- (:height (meta header)) 2))}
   (opentype/text
     {:text        "Milliardar NOK"
      :text-anchor "end"
      :dy          "-2em"
      :fill        "black"
      :font        "Roboto Black"
      :font-size   16})
   (opentype/text
     {:text        "Særskatt på utvinning av petroleum"
      :text-anchor "end"
      :fill        saerskatt-fill
      :font        "Roboto Black"
      :dy          "-1em"
      :font-size   16})
   (opentype/text
     {:text        "Ordinær skatt på utvinning av petroleum"
      :text-anchor "end"
      :dy          "-0em"
      :fill        ordinaer-fill
      :font        "Roboto Black"
      :font-size   16})])

(def footer (opentype/text-stack-downwards
              {:margin-left   marg
               :margin-top    8
               :margin-bottom 3}
              [{:text "Kjelder: SSB, Norges Bank, St. Louis Fed" :font-size 14}]))

(def footer2 (opentype/text
               {:text               "Diagram © Refsdal.Ivar@gmail.com"
                :alignment-baseline "hanging"
                :dy                 ".5em"
                :dx                 (- svg-width marg)
                :text-anchor        "end" :font-size 14}))

(def x-ticks (filter #(.endsWith % "-01") (map :dato data)))
(def end-of-year-data (filter #(.endsWith (:dato %) "-12") data))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (str "Jan " (subs x 0 4)))
         :domain        x-domain
         :sub-domain    [ordinaer saerskatt]
         :padding-inner 0.1
         :padding-outer 0.2
         })

(def yy {:type        :linear
         :orientation :right
         :grid        true
         :grid-stroke-opacity 0.35
         :tick-values  (mapv #(* 25.0 %) (range 11))
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain      [0 250 #_(apply max (map :sum data))]})

(def yy2 {:type        :linear
          :orientation :left
          :color       oil-fill
          :tick-values (mapv #(* 70.0 %) (range 11))
          :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
          :domain      [0 700 #_(apply max (map :oilprice data))]})

(def available-height (- svg-height (:height (meta header)) (:height (meta footer))))

(def c (chart/chart {:width  (- svg-width (* 2 marg))
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     yy2}))

(def x (:x c))
(def y (:y c))
(def y2 (:y2 c))

(def xfn (partial point/center-point x))
(def yfn (partial point/center-point y))
(def y2fn (partial point/center-point y2))

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  [{:p (:dato opts) :c ordinaer :h (get opts ordinaer) :fill ordinaer-fill}
   {:p (:dato opts) :c saerskatt :h (get opts saerskatt) :fill saerskatt-fill}])

(defn make-txt [{dato :dato year :year summ :sum}]
  [:g {:transform (translate (xfn dato) (yfn summ))}
   [:circle {:r 2}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" year ")")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.0f" summ) "." ",")})])

(defn add-oil-price-line []
  (let [dat data
        first-point (first dat)
        last-point (last dat)
        rest-of-data (drop 1 dat)
        oil-price-stroke-width 3
        line-to (reduce (fn [o v] (str o " "
                                       "L"
                                       (xfn (:dato v))
                                       " "
                                       (y2fn (:oilprice v)))) "" rest-of-data)
        dots (map (fn [{dato :dato oilprice :oilprice}]
                    [:circle {:cx           (xfn dato)
                              :cy           (y2fn oilprice)
                              :stroke       "black"
                              :stroke-width oil-price-stroke-width
                              :fill         oil-fill
                              :r            4}]) end-of-year-data)]
    [:g
     [:path {:stroke-width oil-price-stroke-width
             :stroke       oil-fill
             :fill         "none"
             :d
                           (str "M" (xfn (:dato first-point)) " " (y2fn (:oilprice first-point))
                                " " line-to)}]
     #_dots]))


(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   header
   info-right
   ;[:g {:transform (core/translate 0 (+ (:height (meta header)) (:margin-top c)))} info-right]
   [:g {:transform (translate (+ marg (:margin-left c)) (+ (:height (meta header)) (:margin-top c)))}
    (axis/render-axis (:y c))
    (axis/render-axis (:y2 c))
    [:g (bars (mapv make-rect data))]
    [:g (add-oil-price-line)]
    [:g (map make-txt end-of-year-data)]
    #_detail
    (axis/render-axis (:x c))]
   [:g {:transform (translate 0 (+ (:height (meta header)) available-height))} footer]
   [:g {:transform (translate 0 (+ (:height (meta header)) available-height))} footer2]
   ])


(defn render-self []
  (core/render "./img/ssb-svg/petroskatt-mms.svg" "./img/ssb-png/petroskatt-mms.png" (diagram)))
