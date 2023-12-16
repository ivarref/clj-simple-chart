(ns clj-simple-chart.jfx
  (:require [hiccup.core :as hiccup]
            [clj-simple-chart.webserver :as webserver])
  (:import (java.util Locale)))

(defonce _init-locale (Locale/setDefault Locale/US))

(defonce input-height (atom nil))
(defonce input-width (atom nil))


(defn export-to-file [filename t]
  (let [s (hiccup.core/html t)]
    (webserver/push-svg! s)
    (spit filename s)))

(defn style [& info]
  {:style (.trim (apply str (map #(let [[kwd val] %]
                                    (str (name kwd) ":" val "; "))
                                 (apply hash-map info))))})

(defn render-svg [svg]
  (let [body [:body (style
                      :margin "0 !important"
                      :padding "0 !important"
                      :overflow-x "hidden"
                      :overflow-y "hidden")]
        attrs (second svg)
        width (:width attrs)
        height (:height attrs)
        s (hiccup.core/html (conj body svg))]
    (swap! input-height (fn [o] height))
    (swap! input-width (fn [o] width))
    ;(bootstrap)
    ;(Platform/runLater (fn [] (.setZoom @webview @zoom)))
    ;(Platform/runLater (fn [] (.setPrefHeight @webview (* @zoom height))))
    ;(Platform/runLater (fn [] (.setPrefWidth @webview (* @zoom width))))
    ;(render-string s)
    ;(Platform/runLater (fn [] (.sizeToScene @stage)))
    ;(Platform/runLater (fn [] (.show @stage)))
    ;(Platform/runLater (fn [] (.toFront @stage)))
    s))

(defn render
  ([svg] (render-svg svg))
  ([f1 f2 svg]
   (render f1 svg)
   (render f2 svg))
  ([filename svg]
   (cond (.endsWith filename ".svg") (export-to-file filename svg)
         #_(.endsWith filename ".png") #_(render-to-png filename svg)
         :else (throw (Exception. (str "Unknown file type " filename))))
   (render svg)))
