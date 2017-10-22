(ns clj-simple-chart.ssb.nettoksproginneverandediagram
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ssb.nettokontantstraum :as nettokontantstraum]
            [clj-simple-chart.ssb.nettokontantstraumprognose :as prognose]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.line :as line]
            [clojure.string :as string]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data nettokontantstraum/four-quarters-moving-sum-eoy)

(def x-domain (mapv str (range 2000 2019)))

(def available-width (- svg-width (* 2 marg)))

(def x-ticks x-domain)

(def header (opentype/stack
              {:width available-width}
              [{:text "Statens netto kontantstraum frå petroleumsverksemda" :font "Roboto Bold" :font-size 28}
               {:text "Røynda og prognose frå Statsbudsjettet" :font "Roboto Bold" :font-size 16 :margin-top 2 :margin-bottom 10}

               {:text "Netto kontantstraum" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "Milliardar kroner" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 8 :text "Kjelder: SSB, Statsbudsjettet 2000–2018" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :domain        x-domain
         :padding-inner 0.4
         :padding-outer 0.4})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 450]})

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")
(def green "rgb(44, 160, 44)")
(def brown "rgb(140, 86, 75)")
(def red "rgb(214, 39, 40)")
(def pink "rgb(227, 119, 194)")
(def gusjegul "rgb(188, 189, 34)")
(def lilla "rgb(148, 103, 189)")
(def cyan "rgb(23, 190, 207)")

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis/render-axis (:y c))
     (rect/bars c {:p    :year
                   :fill green
                   :h    :netto-kontantstraum}
                data)
     [:g {:transform (translate 0 2)}
      (opentype/stack {}
                      [;{:text "Netto kontantstraum" :font "Roboto Bold"}
                       {:text "Røynd netto kontantstraum" :rect {:fill green} :font "Roboto Bold"}
                       {:text "Prognose inneverande år" :circle {:r 4.5 :stroke-width 2 :fill orange} :path {:stroke orange} :font "Roboto Bold"}
                       {:text "Prognose komande år" :circle {:r 4.5 :stroke-width 2 :fill blue} :path {:stroke blue} :font "Roboto Bold"}])
      ]
     (line/line c {:p         :year
                   :h         :netto-kontantstraum
                   :dot       true
                   :dot-style {:r            4.5
                               :fill         orange
                               :stroke       "black"
                               :stroke-width 2.0}
                   :path      {:stroke       orange
                               :stroke-width 3}} prognose/inneverande-aar)
     (line/line c {:p         :year
                   :h         :netto-kontantstraum
                   :dot       true
                   :dot-style {:r            4.5
                               :fill         blue
                               :stroke       "black"
                               :stroke-width 2.0}
                   :path      {:stroke       blue
                               :stroke-width 3}} prognose/gul-bok)
     (axis/render-axis (:x c))]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (core/render "./img/ssb-svg/nettokontantstraum-inneverande-prognose.svg" "./img/ssb-png/nettokontantstraum-inneverande-prognose.png" (diagram)))
