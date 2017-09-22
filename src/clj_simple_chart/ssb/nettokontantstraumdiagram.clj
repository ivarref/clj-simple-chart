;; Basert på data frå
;; https://www.ssb.no/statsregn
;; Her står òg neste publiseringsdato (24. november 2017).

(ns clj-simple-chart.ssb.nettokontantstraumdiagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.nettokontantstraum :as nettokontantstraum]
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

(def data (->> nettokontantstraum/four-quarters-moving-sum-adjusted-mrd
               (filter #(>= (:year %) 1996))
               (mapv #(assoc % :oilprice
                               (get brentoilprice/brent-4qma-to-2017-nok (prev-quarter (prev-quarter (:dato %))) ::none)))))

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
  "#990099"
  )

(def fills {utbytte    orange
            netto-sdoe red
            avgift     green
            skatter    blue})

(def x-domain (map :dato data))

(def available-width (- svg-width (* 2 marg)))

(def x-ticks (filter #(.endsWith % "K4") (map :dato data)))

(def last-data (last data))

(def header (opentype/stack
              {:width available-width}
              [{:text "Statens netto kontantstraum frå petroleumsverksemda" :font "Roboto Bold" :font-size 30}
               {:text (str "Milliardar 2017-kroner, 4 kvartal glidande sum. "
                           "Per " (:dato last-data) ": "
                           (string/replace (format "%.1f" (get last-data netto-sum)) "." ",")
                           " mrd kr")
                :font "Roboto Bold" :font-size 16
                :margin-top 2
                :margin-bottom 10}
               {:text "Oljepris, 2017-kroner/fat" :fill oil-price-fill :font "Roboto Bold" :font-size 16}
               {:text "4 kvartal glidande gjennomsnitt, 2 kvartal framskyvd" :fill oil-price-fill :font "Roboto Bold" :font-size 16}

               {:text "Netto kontantstraum" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "Milliardar 2017-kroner" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 8 :text "Kjelder: SSB, Norges Bank, St. Louis Fed. *Statens Direkte Økonomiske Engasjement" :font "Roboto Regular" :font-size 14}
               ;{:text "Kjelder: SSB, Norges Bank, St. Louis Fed" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}
               ]))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (cond (= x (first x-ticks)) x
                                      (= x (last x-ticks)) x
                                      (.endsWith x "05K4") (subs x 0 4)
                                      (.endsWith x "00K4") (subs x 0 4)
                                      (.endsWith x "10K4") (subs x 0 4)
                                      :else (subs x 2 4)))
         :domain        x-domain
         :sub-domain    sub-domain
         :padding-inner 0.1
         :padding-outer 0.1})

; Thanks to Rune Likvern for suggestion about tick values

(def yy {:type               :linear
         :orientation        :right
         ;:ticks              5
         :tick-values        (mapv #(* 50.0 %) (range 12))
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 550 #_(apply max (map netto-sum data))]})


(def yy2 {:type               :linear
          :orientation        :left
          :color              oil-price-fill
          ;:ticks              5
          :tick-values       (mapv #(* 70.0 %) (range 12))
          :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
          :domain             [0 770 #_(apply max (mapv :oilprice data))]})

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     yy2}))

(def translate-info {netto-sdoe "Netto kontantstraum frå SDØE*"
                     utbytte    "Utbytte frå Statoil"
                     skatter    "Skattar på utvinning av petroleum"})

(def info
  (opentype/stack
    {:width (:plot-width c)}
    (mapv (fn [x] {:text      (get translate-info x (name x))
                   ;:rect {:fill (get fills x)}
                   :fill      (get fills x)
                   :font      "Roboto Black"
                   :font-size 15})
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

(def end-of-year-data (->> data
                           (filter #(.endsWith (:dato %) "K4"))
                           (remove #(odd? (:year %)))))

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
     [:g {:transform (translate 30 (+ 2 (yfn 500)))} info]]

    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ssb-svg/nettokontantstraum.svg" "./img/ssb-png/nettokontantstraum.png" (diagram)))
