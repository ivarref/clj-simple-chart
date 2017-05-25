(ns clj-simple-chart.ncs.goliatdiagramthree
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.ncs.goliat :as goliat]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]))

(def marg 10)
(def two-marg (* 2 marg))

; https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions
; iPhone 4, 4s
(def svg-width 320)
(def svg-height 480)

(def data goliat/top-ten-plus-goliat)
(def domain (mapv :fldName data))

(def available-width (- svg-width (* 2 marg)))

(def header (opentype/stack
              {}
              [{:text "Goliat — ein ekte kjempe?" :font "Roboto Black" :font-size 24}
               {:text "Dei ti største oljefelta, pluss Goliat" :font "Roboto Bold" :font-size 16}
               {:text          "Opprinneleg utvinnbart, millionar fat olje" :font-size 16
                :margin-bottom 10}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 10 :text "Kjelde: OD" :font "Roboto Regular" :font-size 16}
               {:text "© Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 16 :valign :bottom :align :right}
               ]))

(def available-height (- svg-height (+ two-marg
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def xx {:type        :linear
         :axis        :x
         :orientation :top
         :ticks       5
         :domain      [0 4000]})

(def yy {:type               :ordinal
         :axis               :y
         :domain             domain
         :orientation        :left
         :reverse            true
         :axis-text-style-fn (fn [x]
                               (if (= "Goliat" x)
                                 {:font      "Roboto Black"
                                  :font-size 18}
                                 {}))
         :round              true
         :padding-inner      0.5
         :padding-outer      0.3})


(def c (chart/chart {:width  available-width
                     :height available-height
                     :x      xx
                     :y      yy}))
(def x (:x c))
(def y (:y c))

(def rect (rect/scaled-rect x y))

(defn make-rect [{fldName :fldName
                  oil     :fldRecoverableOil}]
  {:p fldName :h oil :fill "steelblue"})

(defn recoverable-oil-text [{fldName :fldName
                             oil     :fldRecoverableOil}]
  (opentype/text {:x         (point x oil)
                  :dx        ".20em"
                  :y         (center-point y fldName)
                  :dy        ".32em"
                  :font      (if (= "Goliat" fldName)
                               "Roboto Black"
                               "Roboto Regular")
                  :font-size (if (= "Goliat" fldName)
                               18
                               14)}
                 oil))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (rect (mapv make-rect data))
     (map recoverable-oil-text data)
     (axis/render-axis y)
     (axis/render-axis x)]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]
    ]])

(defn render-self []
  (render "./img/goliatthree.svg" "./img/goliatthree.png" (diagram)))
