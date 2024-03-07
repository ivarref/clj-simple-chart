(ns clj-simple-chart.demo.demo
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.render :as r]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.roughjs :as rough]
            [clojure.string :as str]
            [demo.refresh :as refresh]))

(def margin {:top 75 :bottom 40 :left 40 :right 40})
(def width (- (/ 1024 2) (:left margin) (:right margin)))
(def height (- (/ 512 2) (:top margin) (:bottom margin)))

(refresh/set-focus! *ns*) ;; her set ein det *ns*-et som skal vera i fokus.
;; og som keybinding så gjer Ctrl-S denne demo.refresh/refresh-from-ns!
;; slik det står i REPL-en.
;; og dersom ein endrar i eit NS langt nede i hierarkiet/avhengigheitene,
;; så vil den refreshe frå "djupet" og opp til toppen, som er definert
;; som (refresh/set-focus! *ns*) ... Demo...
;; la oss gå eit stykke ned i stacken...


(def scale-text
  nil #_{:fillStyle      "hachure"
         :stroke         "black"
         :stroke-opacity 1
         ;:fill             "black"
         ;:preserveVertices true
         :simplification 0.8
         :roughness      1})

(def axis-text-style
  (fn [_]
    {:font-size 14
     ;:fill "green"
     ;:stroke "black"
     :font      "Roboto Thin"}))

;=> #{"LondrinaBlack-Regular" "LondrinaShadow-Regular" "LondrinaSketche-Regular" "LondrinaThin-Regular" "LondrinaOutline-Regular" "LondrinaBook-Regular" "LondrinaSolid-Regular"}

(comment
  (->> (keys clj-simple-chart.opentype/font-name-to-font)
       (remove #(str/includes? % "Roboto"))
       (sort)
       (vec)))

(comment
  (clj-simple-chart.opentype/force-reload-fonts!))

(def x
  (scale {:type               :ordinal
          :axis               :x
          :orientation        :bottom
          :width              width
          :height             height
          :domain             [1990 1991 1992 1993]
          :sub-domain         ["cats" "dogs" "birds"]
          :fill               ["red" "green" "blue"]
          :stack              :veritcal
          :stack-opts         {:padding-inner 0.1}
          :rough              {:fillStyle "zigzag"
                               :stroke    "black"}
          :axis-text-style-fn axis-text-style
          :rough-text         scale-text
          :padding-inner      0.2
          :padding-outer      0.1}))

(def y
  (scale {:type               :linear
          :axis               :y
          :grid               true
          :orientation        :left
          :ticks              5
          :width              width
          :height             height
          :domain             [-100 100]
          :axis-text-style-fn axis-text-style
          :rough-text         scale-text
          :rough              {:stroke    "black"
                               :roughness 1.25}}))

(def rects
  [
   {:p 1990 :c "dogs" :h 35}
   {:p 1990 :c "cats" :h 25}
   {:p 1990 :c "birds" :h 25}

   {:p 1991 :c "cats" :h -25}
   {:p 1991 :c "dogs" :h -25}
   {:p 1991 :c "birds" :h -25}

   {:p 1992 :c "cats" :h -25}
   {:p 1992 :c "dogs" :h -25}
   {:p 1992 :c "birds" :h 25}])

(def txt (opentype/text {:text "Slik :-)"
                         :font-size 200
                         :alignment-baseline "hanging"
                         :font "Roboto Thin"
                         :rough {:fillStyle "solid"}}))

(defn diagram []
  [:svg (svg-attrs width height margin)
   [:g {:transform (translate (:left margin) (:top margin))}
    [:g
     txt
     [:rect (->> txt
                 (tree-seq coll? identity)
                 (filter #(and (map? %) (contains? % :d)))
                 (first)
                 :d
                 (rough/get-bounds-rect))]]]])
     ;(render-axis y)
     ;(render-axis x)
     ;(rect/rect-or-stacked-vertical x y rects)]]])

(comment)

;; okey, la oss laste namespacet :-)
;; oops, fyrst starte REPL
;; oops2, ingen klientar å pushe til...
;; det tyder at ein lyt opna nettsida
;; og er er Abc ja :-)

;; ved lagring av fila får ein auto-reload
;; dette er jo intuitivt så lenge ein endrar i "rot-namespacet"
;; men kva om ein gjer noko lengre nede i namespace-hierarkiet..?

(def _render-self (r/render-fn (fn [] (diagram))))
