(ns clj-simple-chart.demo.vertical-stack-text
  (:require [clj-simple-chart.core :refer :all]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.axis.core :as axis]))

(defn stack-vertically-text
  [txt-org]
  (let [txt txt-org
        txt (map #(assoc % :font-size (get % :font-size 14)) txt)
        txt (map #(assoc % :y (:font-size %)) txt)
        txt (reductions (fn [p n] (assoc n :y (+ (:y n) (:y p)))) txt)]
    (with-meta
      (map opentype/text txt)
      {:height (:y (last txt))})))

(def header
  (stack-vertically-text [{:text "Hello World" :font "Roboto Bold" :font-size 36}
                          {:text "Some more details"}
                          {:text "Even more details ..."}
                          {:text ""}
                          {:text ""}]))

(def footer
  (stack-vertically-text [{:text ""}
                          {:text "Source: Bla bla bla"}]))

(def header-height (:height (meta header)))
(def footer-height (:height (meta footer)))

(def outer-margin 10)

(def svg-width 250)
(def svg-height 400)

(def chart-width (- svg-width
                    (* 2 outer-margin)))

(def chart-height (- svg-height
                     (* 2 outer-margin)
                     footer-height
                     header-height))

(def footer2
  (stack-vertically-text [{:text ""}
                          {:dx chart-width :text-anchor "end" :text "@ivarref"}]))

(defn diagram []
  [:svg {:xmlns  "http://www.w3.org/2000/svg"
         :width  svg-width
         :height svg-height}
   [:g {:transform (translate outer-margin outer-margin)}
    header
    [:g {:transform (translate 0 header-height)}
     [:line {:x1 0 :x2 chart-width :y1 0 :y2 0 :stroke "black"}]
     [:line {:x1 0 :x2 chart-width :y1 chart-height :y2 chart-height :stroke "black"}]]
    [:g {:transform (translate 0 (+ header-height chart-height 14 14))}
     (opentype/text {} "Source bla bla bla")
     (opentype/text {:dx chart-width :text-anchor "end"} "@ivarref")]]])

(defn render-self []
  (render "./img/vertical-stack-text.png" "./img/vertical-stack-text.svg" (diagram)))