(ns clj-simple-chart.demo.vertical-stack-text
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]))

(defn header-txt [y-offset {path :path idx :idx}]
  [:g {:transform (translate (- (:x1 (meta path))) (reduce + (take idx y-offset)))} path])

(defn header-txts [txts]
  (let [with-baseline (map #(assoc % :alignment-baseline "hanging") txts)
        with-idx (map-indexed (fn [idx x] (assoc x :idx idx)) with-baseline)
        with-path (map (fn [x] (assoc x :path (opentype/text x))) with-idx)
        paths (map :path with-path)
        metas (map meta paths)
        y-offset (map #(let [h-with-margin (+ 4 (:height %))]
                         (Math/min h-with-margin (+ 0 (:font-size %)))) metas)
        height (reduce + y-offset)]
    (with-meta
      [:g (map (partial header-txt y-offset) with-path)]
      {:height height})))

(def header (header-txts [{:text "Hello World" :border-tight "none" :font "Roboto Bold" :font-size 36}
                          {:text "It's gonna stack" :border-tight "none" :font "Roboto Black" :font-size 16}
                          {:text "Just like java.util.Stack?!" :border-tight "none" :font-size 16}]))

(defn diagram []
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width 250 :height 500}
   [:g {:transform (translate 15.5 15.5)}
    header
    [:g {:transform (translate 0 (:height (meta header)))}
     [:line {:stroke "black" :x1 -15.5 :x2 250}]]]])

(defn render-self []
  (render "./img/vertical-stack-text.png" "./img/vertical-stack-text.svg" (diagram)))