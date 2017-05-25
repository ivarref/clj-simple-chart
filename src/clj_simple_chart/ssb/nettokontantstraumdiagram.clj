(ns clj-simple-chart.ssb.nettokontantstraumdiagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.nettokontantstraum :as nettokontantstraum]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.translate :refer [translate]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clojure.string :as string]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> nettokontantstraum/four-quarters-moving-sum-adjusted-mrd
               (filter #(>= (:year %) 1996))))

(def skatter (keyword "Skatter på utvinning av petroleum"))
(def avgift (keyword "Avgifter på utvinning av petroleum"))
(def utbytte (keyword "Utbytte fra Statoil"))
(def sdoe (keyword "Netto kontantstrøm fra SDØE"))

(def sub-domain [skatter avgift sdoe utbytte])

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")
(def green "rgb(44, 160, 44)")
(def brown "rgb(140, 86, 75)")
(def red "rgb(214, 39, 40)")

(def fills {skatter blue
            avgift  green
            utbytte orange
            sdoe    red})

(def netto-sum (keyword "Statens netto kontantstrøm fra petroleumsvirksomhet"))

(def x-domain (map :dato data))

(def available-width (- svg-width (* 2 marg)))

(def x-ticks (filter #(.endsWith % "K4") (map :dato data)))

(def last-data (first (filter #(= (last x-ticks) (:dato %)) data)))

(def header (opentype/stack
              {:width available-width}
              [{:text "Statens netto kontantstraum frå petroleumsverksemda" :font "Roboto Bold" :font-size 30}
               {:text "Milliardar 2016-kroner, 4 kvartal glidande sum" :font "Roboto Bold" :font-size 16}
               {:text (str "Sum per " (:dato last-data) ": "
                           (string/replace (format "%.1f" (get last-data netto-sum)) "." ",")
                           " mrd kr") :font "Roboto Bold" :font-size 16}

               {:text "Milliardar 2016-kroner" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               ]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "*Statens direkte økonomiske engasjement" :font "Roboto Regular" :font-size 16}
               {:text "Kjelde: SSB" :font "Roboto Regular" :font-size 16}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 16 :valign :bottom :align :right}
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

(def yy {:type               :linear
         :orientation        :right
         :ticks              5
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (apply max (map netto-sum data))]})

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def translate-info {sdoe    (str "Netto kontantstraum frå SDØE*")
                     utbytte "Utbytte frå Statoil"
                     skatter "Skattar på utvinning av petroleum"})

(def info
  (opentype/stack
    {:width (:plot-width c)}
    (mapv (fn [x] {:text      (get translate-info x (name x))
                   :fill      (get fills x)
                   :font      "Roboto Bold"
                   :font-size 16})
          (reverse sub-domain))))

(def x (:x c))
(def y (:y c))

(def yfn (partial point/center-point y))
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
                   :text        (string/replace (format "%.1f" (get opts netto-sum)) "." ",")})])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}

    #_[:rect {:width        (- svg-width two-marg) :height (- svg-height two-marg)
            :fill-opacity "0.2"
            :fill         "steelblue"}]
    header

    [:g {:transform (translate 0 (+ (:height (meta header)) (:margin-top c)))}
     #_[:rect {:width        (:plot-width c) :height (:plot-height c)
             :fill-opacity "0.2"
             :fill         "#ffaa00"}]
     (axis/render-axis (:y c))
     [:g (bars (mapv make-rect data))]
     [:g (map make-txt end-of-year-data)]
     (axis/render-axis (:x c))
     [:g {:transform (translate 0 (+ 7 (yfn 500)))} info]]

    [:g {:transform (translate 0 (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (core/render "./img/nettokontantstraum.svg" "./img/nettokontantstraum.png" (diagram)))
