(ns clj-simple-chart.ssb.nettokontantstraum.diagram-en-usd-oil-price
  (:require [clj-simple-chart.core :as core]
    [clj-simple-chart.opentype :as opentype]
    [clj-simple-chart.ssb.nettokontantstraum.nettokontantstraum :as nettokontantstraum]
    [clj-simple-chart.ssb.brentoilprice :as brentoilprice]
    [clj-simple-chart.axis.core :as axis]
    [clj-simple-chart.chart :as chart]
    [clj-simple-chart.translate :refer [translate translate-y]]
    [clj-simple-chart.rect :as rect]
    [clj-simple-chart.point :as point]
    [clojure.string :as string]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(defn prev-quarter [s]
  (let [parts (string/split s #"K")
        year (read-string (first parts))
        quarter (read-string (last parts))]
    (if (= quarter 1)
      (str (dec year) "K4")
      (str year "K" (dec quarter)))))

(def data (->> nettokontantstraum/four-quarters-moving-sum-mrd
               (filter #(>= (:year %) 1996))
               (mapv #(assoc % :oilprice
                               (get brentoilprice/brent-4qma-to-usd (prev-quarter (prev-quarter (:dato %))) ::none)))))

(def skatter (keyword "Skatter på utvinning av petroleum"))
(def avgift (keyword "Avgifter på utvinning av petroleum"))
(def utbytte (keyword "Utbytte fra Statoil"))
(def netto-sdoe (keyword "Netto kontantstrøm fra SDØE"))
(def netto-sum (keyword "Statens netto kontantstrøm fra petroleumsvirksomhet"))

(def sub-domain [skatter avgift netto-sdoe utbytte])

; http://bl.ocks.org/aaizemberg/78bd3dade9593896a59d

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")
(def green "rgb(44, 160, 44)")
(def brown "rgb(140, 86, 75)")
(def red "rgb(214, 39, 40)")
(def pink "rgb(227, 119, 194)")
(def gusjegul "rgb(188, 189, 34)")
(def lilla "rgb(148, 103, 189)")
(def cyan "rgb(23, 190, 207)")

(def oil-price-fill
  ;"#8c6d31"
  "#990099")


(def fills {utbytte    orange
            netto-sdoe red
            avgift     green
            skatter    blue})

(def x-domain (map :dato data))

(def available-width (- svg-width (* 2 marg)))

(def x-ticks (filter #(.endsWith % "K4") (map :dato data)))

(def last-data (last data))

(defn k->q [s]
  (string/replace s #"K" "Q"))

(def header (opentype/stack
              {:width available-width}
              [{:text "Net cash flow from petroleum activities, Norwegian Central Government" :font "Roboto Black" :font-size 27}
               {:text          (str "NOK billion, 4 quarters moving sum, nominal. "
                                    "As of " (k->q (:dato last-data)) ": NOK "
                                    (string/replace (format "%.1f" (get last-data netto-sum)) "." ".")
                                    " billion")
                :font          "Roboto Bold" :font-size 16
                :margin-top    2
                :margin-bottom 10}
               {:text "Oil price, USD/barrel, nominal" :fill oil-price-fill :font "Roboto Bold" :font-size 16}
               {:text "4 quarters moving average, 2 quarters expedited" :fill oil-price-fill :font "Roboto Bold" :font-size 16}

               {:text "Net cash flow" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "NOK billion, nominal" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 8 :text "Sources: Statistics Norway, Norges Bank, St. Louis Fed. *State's Direct Financial Interest" :font "Roboto Regular" :font-size 14}
               ;{:text "Kjelder: SSB, Norges Bank, St. Louis Fed" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))


(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (k->q (cond (= x (first x-ticks)) x
                                            (= x (last x-ticks)) x
                                            (.endsWith x "05K4") (subs x 0 4)
                                            (.endsWith x "00K4") (subs x 0 4)
                                            (.endsWith x "10K4") (subs x 0 4)
                                            :else (subs x 2 4))))
         :domain        x-domain
         :sub-domain    sub-domain
         :padding-inner 0.1
         :padding-outer 0.1})

(def yticks (mapv #(* 40.0 %) (range 12)))
(def yyticks (mapv #(* 10.0 %) (range 12)))

(def yy {:type               :linear
         :orientation        :right
         ;:ticks              5
         :tick-values        yticks
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (last yticks)]})

(def yy2 {:type               :linear
          :orientation        :left
          :color              oil-price-fill
          ;:ticks              5
          :tick-values        yyticks
          :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
          :domain             [0 (last yyticks)]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     yy2}))

(def translate-info {netto-sdoe "Net cash flow from SDFI*"
                     utbytte    "Statoil dividend"
                     avgift     "Environmental taxes and area fees"
                     skatter    "Taxes on the extraction of petroleum"})

(def info
  (opentype/stack
    {:width (:plot-width c)}
    (mapv (fn [x] {:text       (get translate-info x (name x))
                   ;:rect {:fill (get fills x)}
                   :margin-top (if (= x avgift) 2 0)
                   :fill       (get fills x)
                   :font       "Roboto Black"
                   :font-size  15})
          (reverse sub-domain))))

(def x (:x c))
(def y (:y c))

(def yfn (partial point/center-point y))
(def y2fn (partial point/center-point (:y2 c)))
(def xfn (partial point/center-point x))

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  (map (fn [[k fill]]
         {:p    (:dato opts)
          :c    k
          :fill fill
          :h    (get opts k)})
       fills))

(def txt-for-years [1996
                    1998
                    2000
                    2002
                    2004
                    2005
                    2006
                    2008
                    2010
                    2012
                    2013
                    2014
                    2015
                    2016])

(def end-of-year-data (->> data
                           (filter #(and (some #{(:year %)} txt-for-years)
                                         (.endsWith (:dato %) "K4")))))

(defn make-txt [{dato :dato year :year :as opts}]
  [:g {:transform (translate (xfn dato) (yfn (get opts netto-sum)))}
   [:circle {:r 2}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" year ")")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.0f" (get opts netto-sum)) "." ",")})])

(defn add-oil-price-line []
  (let [dat data
        first-point (first dat)
        last-point (last dat)
        rest-of-data (drop 1 dat)
        oil-price-stroke-width 4
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
                              :fill         oil-price-fill
                              :r            2.5}]) end-of-year-data)]
    [:g
     [:path {:stroke-width oil-price-stroke-width
             :stroke       oil-price-fill
             :fill         "none"
             :d
                           (str "M" (xfn (:dato first-point)) " " (y2fn (:oilprice first-point))
                                " " line-to)}]
     #_dots]))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header

    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     #_[:rect {:width        (:plot-width c) :height (:plot-height c)
               :fill-opacity "0.1"
               :fill         "steelblue"}]
     (axis/render-axis (:y c))
     (axis/render-axis (:y2 c))
     [:g (bars (mapv make-rect data))]
     [:g (add-oil-price-line)]
     [:g (map make-txt end-of-year-data)]
     (axis/render-axis (:x c))
     [:g {:transform (translate 30 (+ 2 (yfn 400)))} info]]

    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ssb-svg/nettokontantstraum-en-usd-oil-price.svg" "./img/ssb-png/nettokontantstraum-en-usd-oil-price.png" (diagram)))
