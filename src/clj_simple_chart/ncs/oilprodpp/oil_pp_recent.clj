(ns clj-simple-chart.ncs.oilprodpp.oil-pp-recent
  (:require [clj-simple-chart.core :as core]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.ncs.oilprodpp.oilppdata :as production]
            [clj-simple-chart.translate :refer [translate translate-y]]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.area :as clj-area]
            [clj-simple-chart.area-center-text :as act]
            [clojure.string :as string]
            [clojure.string :as str]))

(def marg 10)
(def two-marg (* 2 marg))

(def svg-width 900)
(def svg-height 500)

(def data (->> production/by-date
               (filter #(>= (:eofYear %) 2000))))

(def buckets (vec (reverse (sort (keys production/empty-buckets)))))

;"#d62728",  //red
;"#ff7f0e", //orange
;"#8c564b", //brown
;"#1f77b4", //blue
;"#e377c2", //pink
;"#17becf", //cyan
;"#bcbd22", //gusjegul
;"#9467bd", //purple
;"#7f7f7f", //gray
;"#2ca02c", //green

(def bucket-to-fill (zipmap buckets
                            (reverse [
                                      ;"rgb(31, 119, 180)"             ; blue
                                      "#1f77b4"

                                      "#8c564b"             ; brown
                                      "#ff7f0e"             ; orange
                                      "#d62728"             ; red
                                      ;"#9467bd"                      ; purple
                                      ])))

(def sub-domain buckets)

(def x-domain (map :date data))

(def x-ticks (filter #(or (= % "1971-12")
                          (.endsWith % "12")
                          (.endsWith % "0-12"))
                     (map :date data)))

(def available-width (- svg-width (* 2 marg)))

(def last-data (last data))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (read-string (last parts)))
         " " (first parts))))

(def feltmogning-ex-txt (str "Feltmogning: Prosent produsert av opprinneleg utvinnbart"
                             ;(:prfYear (first data)) "–" (:prfYear (last data))

                             ))

(def header (opentype/stack
              {:width available-width}
              [{:text (str "Råoljeproduksjon etter feltmogning")
                :font "Roboto Bold" :font-size 30}
               {:text feltmogning-ex-txt :font "Roboto Bold" :font-size 16 :margin-top 1}
               {:text (str "Produksjon per " (months-str (:date last-data)) ": "
                           (string/replace (format "%.1f" (get last-data :sum)) "." ",")
                           " mill. Sm³")
                :font "Roboto Bold" :font-size 16 :margin-top 3}

               {:text "Råoljeproduksjon, mill. Sm³" :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}
               {:text "12 månadar glidande sum" :margin-top 1 :font "Roboto Bold" :font-size 16 :valign :bottom :align :right}]))

(def footer (opentype/stack
              {:width available-width}
              [{:margin-top 4 :text "Kjelde: Oljedirektoratet" :font "Roboto Regular" :font-size 14}
               {:text "Diagram © Refsdal.Ivar@gmail.com" :font "Roboto Regular" :font-size 14 :valign :bottom :align :right}]))

(def xx {:type        :ordinal-linear
         :tick-values x-ticks
         :tick-format (fn [x] (let [year (subs x 0 4)]
                                (if (or (.endsWith year "5")
                                        (.endsWith year "0"))
                                  year
                                  (subs year 2 4))))
         :orientation :bottom
         :domain      x-domain
         :sub-domain  buckets})

(def yy {:type               :linear
         :orientation        :right
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain             [0 200]})

(def available-height (- svg-height (+ (+ 3 marg)
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


(defn make-txt [{date :date year :year :as opts}]
  (let [year (subs date 0 4)
        fmt (if (some #{year} ["2000" "2001"]) "%.1f" "%.0f")]
    (if (or (some #{year} ["2000" "2005" "2010"])
            (>= (read-string year) 2013))
      [:g {:transform (translate (xfn date) (yfn (get opts :sum)))}
       [:circle {:r 2.5}]
       [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
       (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" (subs date 0 4) ")")})
       (opentype/text {:dy          "-1.71em"
                       :font        "Roboto Bold"
                       :text-anchor "middle"
                       :text        (string/replace (format fmt (get opts :sum)) "." ",")})]
      [:g {:transform (translate (xfn date) (yfn (get opts :sum)))}
       [:circle {:r 2.5}]
       [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
       #_(opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" (subs date 0 4) ")")})
       (opentype/text {:dy          "-.72em"
                       :font        "Roboto Bold"
                       :text-anchor "middle"
                       :text        (string/replace (format fmt (get opts :sum)) "." ",")})])))


  (defn diagram []
    [:svg {:xmlns "http://www.w3.org/2000/svg" :width svg-width :height svg-height}
     [:g {:transform (translate marg marg)}
      header
      [:g {:transform (translate (:margin-left c) (+ (:height (meta header)) (:margin-top c)))}
       (axis/render-axis (:y c))
       (clj-area/area c flat)
       #_(act/area-center-text c last-text)
       [:g (map make-txt (filter #(some #{(:date %)} (mapv (fn [x] (str x "-12")) (range 2000 2017))) data))]
       (axis/render-axis (:x c))]
      (let [offset-xy 20
            infotext (opentype/stack {:fill         "lightgray"
                                      :fill-opacity 0.3
                                      :margin       5}
                                     ;{}
                                     (flatten
                                       [{:text "Feltmogningskategori" :font "Roboto Black" :font-size 16}
                                        (mapv (fn [k]
                                                {:text      (str (subs k 3) "%")
                                                 :font-size 16
                                                 :font      "Roboto Bold"
                                                 :rect      {:fill (get bucket-to-fill k)
                                                             :size nil}})
                                              (sort (keys production/empty-buckets)))
                                        #_{:text "Siste tall for kategori i kvitt" :font "Roboto Regular" :font-size 14}]))]
        [:g {:transform (translate (- (+ (:margin-left c) (:plot-width c))
                                      (:width (meta infotext))
                                      offset-xy)
                                   (+ offset-xy (+ (:height (meta header)) (:margin-top c))))}
         infotext
         ]
        )

      [:g {:transform (translate-y (+ (:height (meta header)) available-height))} footer]]])

  (defn render-self []
    (core/render "./img/ncs-svg/oil-pp-recent.svg" "./img/ncs-png/oil-pp-recent.png" (diagram)))

