(ns clj-simple-chart.ssb.petroskattdiagrammonthly
  (:require [clj-simple-chart.ssb.petroskatt :as petroskatt]
            [clj-simple-chart.ssb.brentoilprice :as brentoilprice]
            [clj-simple-chart.core :as core]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.point :as point]
            [clj-simple-chart.opentype :as opentype]
            [clojure.test :as test]
            [clojure.string :as string]))

(def ordinaer (keyword "Ordinær skatt på utvinning av petroleum"))
(def saerskatt (keyword "Særskatt på utvinning av petroleum"))

(def oil-date-to-usd brentoilprice/brent-12-mma-dato-to-nok)

(def data (->> petroskatt/twelve-mma-mrd
               (mapv #(assoc % :year (read-string (subs (:dato %) 0 4))))
               (mapv #(assoc % :sum (+ (get % ordinaer) (get % saerskatt))))
               (mapv #(assoc % :oilprice (get oil-date-to-usd (:dato %) ::none)))
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
  (let [pieces (string/split d #"-")
        year (first pieces)
        month (nth months (read-string (second pieces)))]
    (str month " " year)))

(def last-data (last data))

(def siste-verdi-str
  (str "Per " (date-readable (:dato last-data)) ": "
       (string/replace (format "%.1f" (:sum last-data)) "." ",")
       " mrd kr."))

(def header (opentype/text-stack-downwards
              {:margin-top  5
               :margin-left marg}
              [{:text "Skatteinngang frå utvinning av petroleum" :font "Roboto Bold" :font-size 36}
               {:text (str "Milliardar kroner (løpande), 12 månadar glidande sum. " siste-verdi-str)
                :font "Roboto Black" :font-size 16}
               {:text "Særskatt på utvinning av petroleum" :fill saerskatt-fill :font "Roboto Black" :font-size 16}
               {:text "Ordinær skatt på utvinning av petroleum" :fill ordinaer-fill :font "Roboto Black" :font-size 16}

               #_{:text "(Årstall): Sum ved årsslutt" :font "Roboto Black" :font-size 16}]))

(def oljepris-info
  [:g {:transform (core/translate (- svg-width marg) (:height (meta header)))}
   (opentype/text
     {:text        "Oljepris, NOK/fat"
      :text-anchor "end"
      :fill        oil-fill
      :font        "Roboto Black"
      :dy          "-1em"
      :font-size   16})
   (opentype/text
     {:text        "12 mnd. glidande gjennomsnitt"
      :text-anchor "end"
      :dy          "0em"
      :fill        oil-fill
      :font        "Roboto Black"
      :font-size   14})])

(def detail (opentype/text-stack-downwards
              {:margin-left 8}
              [{:text "Særskatt på utvinning av petroleum" :fill saerskatt-fill :font "Roboto Black" :font-size 16}
               {:text "Ordinær skatt på utvinning av petroleum" :fill ordinaer-fill :font "Roboto Black" :font-size 16}
               #_{:text "(Årstall): Sum ved årsslutt" :font "Roboto Black" :font-size 16}]))

(def footer (opentype/text-stack-downwards
              {:margin-left   marg
               :margin-top    8
               :margin-bottom 3}
              [{:text "Kjelder: SSB, Norges Bank, St. Louis Fed" :font-size 16}]))

(def footer2 (opentype/text
               {:text               "Diagram © Refsdal.Ivar@gmail.com"
                :alignment-baseline "hanging"
                :dy                 ".5em"
                :dx                 (- svg-width marg)
                :text-anchor        "end" :font-size 16}))

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
         :orientation :left
         ;:ticks       5
         :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
         :domain      [0 (apply max (map :sum data))]})

(def yy2 {:type        :linear
          :orientation :right
          :color       oil-fill
          :axis-text-style-fn (fn [x] {:font "Roboto Bold"})
          :domain      [0 (apply max (map :oilprice data))]})

(def available-height (- svg-height (:height (meta header)) (:height (meta footer))))

(def c (chart/chart {:width  (- svg-width (* 2 marg))
                     :height available-height
                     :x      xx
                     :y      yy
                     :y2     yy2}))

(def info-right (opentype/text
                  {:text               "Oljepris brent, USD/fat"
                   :alignment-baseline "hanging"
                   :dy                 ".5em"
                   :dx                 (- svg-width marg (:margin-right c))
                   :text-anchor        "end" :font-size 16}))

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
  [:g {:transform (core/translate (xfn dato) (yfn summ))}
   [:circle {:r 2}]
   [:line {:stroke "black" :stroke-width 1 :fill "black" :y2 -8}]
   (opentype/text {:dy "-.71em" :text-anchor "middle" :text (str "(" year ")")})
   (opentype/text {:dy          "-1.71em"
                   :font        "Roboto Bold"
                   :text-anchor "middle"
                   :text        (string/replace (format "%.1f" summ) "." ",")})])

(defn add-oil-price-line []
  (let [dat data
        first-point (first dat)
        last-point (last dat)
        rest-of-data (drop 1 dat)
        oil-price-stroke-width 2
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
   oljepris-info
   ;[:g {:transform (core/translate 0 (+ (:height (meta header)) (:margin-top c)))} info-right]
   [:g {:transform (core/translate (+ marg (:margin-left c)) (+ (:height (meta header)) (:margin-top c)))}
    (axis/render-axis (:y c))
    (axis/render-axis (:y2 c))
    [:g (bars (mapv make-rect data))]
    [:g (add-oil-price-line)]
    [:g (map make-txt end-of-year-data)]
    #_detail
    (axis/render-axis (:x c))]
   [:g {:transform (core/translate 0 (+ (:height (meta header)) available-height))} footer]
   [:g {:transform (core/translate 0 (+ (:height (meta header)) available-height))} footer2]
   ])


(defn render-self []
  (core/render "./img/petroskatt-mms.svg" "./img/petroskatt-mms.png" (diagram)))
