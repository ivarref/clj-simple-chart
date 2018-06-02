(ns clj-simple-chart.ncs.reserves.top-ten-vs-rest
  (:require [clj-simple-chart.ncs.discovery :as discovery]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.colors :refer :all]
            [clj-simple-chart.data.utils :refer :all]
            [clojure.string :as str]))

(def reserve-type [:pdo-approved :decided-for-production :producing :shut-down])

(def reserves (->> discovery/parsed
                   (filter #(some #{(:status %)} reserve-type))
                   (keep-columns [:name :liquids :year])
                   (update-column :year str)
                   (sort-by :liquids)
                   (multiply-by 6.29)))

(def top-ten (->> (take-last 10 reserves)
                  (column-value->column :name)
                  (remove-whitespace-in-keys)
                  (apply merge)))

(def field->year (->> (take-last 10 reserves)
                      (map (fn [o] [(keyword (str/replace (str/lower-case  (:name o)) " " "-")) (:year o)]))
                      (into {})))

(def others (drop-last 10 reserves))

(def others-summed {:name "OTHERS"
                    :liquids     (double (reduce + 0 (map :liquids others)))})

(def marg 10)
(def two-marg (* 2 marg))
(def svg-width 900)
(def svg-height 500)
(def available-width (- svg-width (* 2 marg)))

(def tx {"OTHERS" (str "Alle andre " (count others) " felt")
         "TOP10" "Dei ti største felta"})

(def xx {:type          :ordinal
         :orientation   :bottom
         :tick-values   ["OTHERS" "TOP10"]
         :domain        ["OTHERS" "TOP10"]
         :tick-format   (fn [x] (get tx x x))
         :padding-inner 0.3
         :padding-outer 0.3})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 25000]})

(def header (text
              {:width available-width}
              [{:text "Dei ti største oljefelta og resten" :font "Roboto Bold" :font-size 30}
               {:margin-bottom 3 :text "Opprinneleg utvinnbart, millionar fat olje." :font "Roboto Regular" :font-size 16}
               {:margin-bottom 3 :text "Millionar fat olje" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (text
              {:width available-width}
              [{:margin-top 6 :text "Kjelde: OD. \"Olje\" inkluderar råolje, kondensat og NGL." :font "Roboto Regular" :font-size 14}
               {:text "Diagram: refsdal.ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def available-height (- svg-height (+ (+ 3 marg)
                                       (:height (meta header))
                                       (:height (meta footer)))))

(def c (chart {:width  available-width
               :height available-height
               :x      xx
               :y      yy}))

(def prop->color
  [[:statfjord brown]
   [:ekofisk red]
   [:oseberg orange]
   [:gullfaks green]
   [:snorre blue]
   [:troll cyan]
   [:johan-sverdrup pink]
   [:heidrun dark-purple]
   [:åsgard purple]
   [:valhall yucky-yellow]])

(def legend-top-ten
  (for [[p color] (reverse prop->color)]
    {:text (str (str/join " " (map capitalize (str/split (kw->human-str (name p)) #" ")))
                " (" (get field->year p) ")")
     :rect {:fill color}
     :right {:text (str (round (get top-ten p)))}
     :font "Roboto Regular" :font-size 18}))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
   [:g {:transform (translate marg marg)}
    header
    [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
     (axis (:x c))
     (axis (:y c))
     (bars c {:p :name :h :liquids :fill gray} [others-summed])
     (bars c {:p :name :h prop->color} [(assoc top-ten :name "TOP10")])
     (text {:margin 5 :fill "whitesmoke"
            :x 15
            :y 15
            :fill-opacity 0.65}
           {:text "Felt" :font "Roboto Bold" :font-size 18
            :right {:text "Mill. fat olje"}}
           legend-top-ten
           {:text (str "Alle andre " (count others) " felt")
            :rect {:fill gray}
            :right {:text (str (round (:liquids others-summed)))}
            :font "Roboto Regular" :font-size 18}
           {:text "Totalt" :font "Roboto Bold" :font-size 18
            :right {:text (str (round (reduce + 0 (map :liquids reserves))))}})]
    [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

(defn render-self []
  (render "img/ncs-svg/liquids-top-ten.svg" "img/ncs-png/liquids-top-ten.png" (diagram)))
