(ns clj-simple-chart.ssb.petroskattdiagrammonthly
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.opentype :as opentype]))

(def data (->> petroskatt/twelve-mma-mrd
               (mapv #(assoc % :year (read-string (subs (:dato %) 0 4))))
               #_(filter #(>= (:year %) 2010))))

(def x-domain (map :dato data))

(def svg-width 960)
(def svg-height 480)

(def marg 15)

(def ordinaer (keyword "Ordinær skatt på utvinning av petroleum"))
(def saerskatt (keyword "Særskatt på utvinning av petroleum"))

(def blue "rgb(31, 119, 180)")
(def orange "rgb(255, 127, 14)")
(def green "rgb(44, 160, 44)")

(def saerskatt-fill green)
(def ordinaer-fill blue)

(def header (opentype/text-stack-downwards
              {:margin-top  marg
               :margin-left marg
               :margin-bottom 3}
              [{:text "Skatteinngang frå kontinentalsokkelen" :font "Roboto Bold" :font-size 36}
               {:text "Milliardar kroner (løpande), 12 månadar rullande sum" :font "Roboto Black" :font-size 16}
               {:text "Særskatt på utvinning av petroleum" :fill saerskatt-fill :font "Roboto Black" :font-size 16}
               {:text "Ordinær skatt på utvinning av petroleum" :fill ordinaer-fill :font "Roboto Black" :font-size 16}]))

(def footer (opentype/text-stack-downwards
              {:margin-left marg
               :margin-top 8
               :margin-bottom 3}
              [{:text "Kjelde: SSB" :font-size 16}]))

(def footer2 (opentype/text
              {:text "Diagram: Refsdal.Ivar@gmail.com"
               :alignment-baseline "hanging"
               :dy ".5em"
               :dx (- svg-width marg)
               :text-anchor "end" :font-size 16}))

(def x-ticks (filter #(.endsWith % "-01") (map :dato data)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   x-ticks
         :tick-format   (fn [x] (subs x 0 4))
         :domain        x-domain
         :sub-domain    [ordinaer saerskatt]
         :padding-inner 0.1
         :padding-outer 0.2
         })

(def yy {:type        :linear
         :orientation :left
         :ticks       5
         :domain      [0 275]})

(def available-height (- svg-height (:height (meta header)) (:height (meta footer))))

(def c (chart/chart {:width  (- svg-width marg)
                     :height available-height
                     :x      xx
                     :y      yy}))

(def bars (rect/scaled-rect (:x c) (:y c)))

(defn make-rect [opts]
  [{:p (:dato opts) :c ordinaer :h (get opts ordinaer) :fill ordinaer-fill}
   {:p (:dato opts) :c saerskatt :h (get opts saerskatt) :fill saerskatt-fill}])

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   header
   [:g {:transform (core/translate (+ marg (:margin-left c)) (+ (:height (meta header)) (:margin-top c)))}
    [:g (bars (mapv make-rect data))]
    (axis/render-axis (:y c))
    (axis/render-axis (:x c))]
   [:g {:transform (core/translate 0 (+ (:height (meta header)) available-height))} footer]
   [:g {:transform (core/translate 0 (+ (:height (meta header)) available-height))} footer2]
   ])


(defn render-self []
  (core/render "./img/petroskatt-mms.svg" "./img/petroskatt-mms.png" (diagram)))
