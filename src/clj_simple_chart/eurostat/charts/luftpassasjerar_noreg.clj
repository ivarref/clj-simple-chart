(ns clj-simple-chart.eurostat.charts.luftpassasjerar-noreg
  (:require [clj-simple-chart.eurostat.data.avia-par-no2 :refer [data]]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.data.utils :refer :all]
            [clojure.string :as str]))


(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   (filter #(str/ends-with? % "-12") (map :date data))
         :domain        (map :date data)
         :tick-format   (fn [s] (subs s 0 4))
         :padding-inner 0.1
         :padding-outer 0.1})

(def yy {:type               :linear
         :orientation        :right
         :grid               true
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 (->> (map :sum data)
                                     (apply max))]})

(def header (text
              {:width available-width}
              [{:text "Antall luftpassasjerar etter avreiselufthavn" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 10 :text "12 månadar glidande sum" :font "Roboto Regular" :font-size 18}
               {:margin-bottom 3 :text "Antall luftpassasjerar, '000" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (text
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: Eurostat, tabell avia-par-no, \"passengers carried\"." :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart {:width  available-width
               :height available-height
               :x      xx
               :y      yy}))

(def prop->color
  [[:others brown]
   [:harstad red]
   [:kristiansand orange]
   [:ålesund blue]
   [:torp cyan]
   [:bodø purple]
   [:tromsø pink]
   [:stavanger green]
   [:trondheim yucky-yellow]
   [:bergen hydro-blue]
   [:oslo dark-purple]])

(def tx {:others "Andre"
         :ålesund "Aalesund"})

(def legend
  (text {:fill         "whitesmoke"
         :fill-opacity 0.75
         :margin       8
         :y            15
         :x            15}
        {:text  "Avreiselufthavn" :font "Roboto Bold" :font-size 18
         :right {:text ""}}
        (for [[prop col] (reverse prop->color)]
          {:rect  {:fill col}
           :right {:text (str (Math/round (double (get (last data) prop))))}
           :text  (let [n (name prop)]
                    (get tx prop (str (str/upper-case (first n))
                                      (subs n 1))))
           :font  "Roboto Regular" :font-size 18})
        {:text  "Totalt"
         :right {:text (str (Math/round (double (:sum (last data)))))}
         :font  "Roboto Regular" :font-size 18}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis (:x c))
     (axis (:y c))
     (bars c {:p :date :h prop->color} data)
     legend]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (render "./img/eurostat-svg/luftpassasjerar.svg" "./img/eurostat-png/luftpassasjerar.png" (diagram)))
