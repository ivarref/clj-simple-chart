(ns clj-simple-chart.core
  (:gen-class
    :extends javafx.application.Application)
  (:require [hiccup.core :as hiccup]
            [digest :as digest]
            [clojure.string :as string]
            [clj-simple-chart.ticks :as ticks])
  (:import (javafx.application Application Platform)
           (java.util.concurrent CountDownLatch)
           (javafx.scene.web WebView)
           (javafx.scene.layout VBox)
           (javafx.scene Scene SnapshotParameters)
           (javafx.geometry Rectangle2D)
           (javafx.embed.swing SwingFXUtils)
           (javax.imageio ImageIO)
           (java.io File)
           (javafx.concurrent Worker$State)))

(defonce latch (CountDownLatch. 1))
(defonce webview (atom nil))
(defonce stage (atom nil))
(defonce engine (atom nil))

(defn -start [this internal-stage]
  (.setTitle internal-stage "SVG Output")
  (let [internal-webview (WebView.)
        internal-engine (.getEngine internal-webview)
        layout (VBox. 0.0)
        children (.getChildren layout)]
    (.loadContent internal-engine "about:blank")
    (.setAll children [internal-webview])
    (.setScene internal-stage (Scene. layout))
    (.show internal-stage)
    (swap! engine (fn [x] internal-engine))
    (swap! stage (fn [x] internal-stage))
    (swap! webview (fn [x] internal-webview)))
  (Platform/runLater (fn [] (.countDown latch))))

(defn launch []
  (.start (Thread. (fn [] (Application/launch clj_simple_chart.core (into-array String []))))))

(defn style [& info]
  {:style (.trim (apply str (map #(let [[kwd val] %]
                                    (str (name kwd) ":" val "; "))
                                 (apply hash-map info))))})

(defn get-worker-state []
  (let [res (atom nil)
        ll (CountDownLatch. 1)]
    (Platform/runLater
      (fn []
        (swap! res (fn [x] (.getState (.getLoadWorker @engine))))
        (.countDown ll)))
    (.await ll)
    @res))

(defn wait-for-worker []
  (let [state (get-worker-state)]
    (cond (= Worker$State/SUCCEEDED state) state
          (= Worker$State/FAILED state) state
          (= Worker$State/CANCELLED state) state
          :else (do (Thread/sleep 10) (recur)))))

(defn bootstrap []
  (if @engine
    nil
    (do
      (launch)
      (.await latch))))

(defn render-string [s]
  (Platform/runLater (fn [] (.loadContent @engine s "text/html")))
  (wait-for-worker))

(defn translate [x y]
  (str "translate(" x "," y ")"))

(defn export-to-file [filename t]
  (let [s (hiccup.core/html t)]
    (spit filename s)))

(defn render
  ([filename t]
   (export-to-file filename t)
   (render t)
   (Platform/runLater (fn [] (.setTitle @stage filename))))
  ([t]
   (let [body [:body (style
                       :margin "0 !important"
                       :padding "0 !important"
                       :overflow-x "hidden"
                       :overflow-y "hidden")]
         attrs (second t)
         width (:width attrs)
         height (:height attrs)
         s (hiccup.core/html (conj body t))]
     (bootstrap)
     (Platform/runLater (fn [] (.setPrefHeight @webview height)))
     (Platform/runLater (fn [] (.setPrefWidth @webview width)))
     (render-string s)
     (Platform/runLater (fn [] (.sizeToScene @stage)))
     (Platform/runLater (fn [] (.show @stage)))
     (Platform/runLater (fn [] (.toFront @stage)))
     s)))

(defn render-to-file [filename t]
  (let [ll (CountDownLatch. 1)
        attrs (second t)
        width (:width attrs)
        height (:height attrs)]
    (render t)
    (Platform/runLater
      (fn []
        (let [snap (SnapshotParameters.)]
          (.setViewport snap (Rectangle2D. 0 0 width height))
          (let [image (.snapshot @webview snap nil)
                bufimage (SwingFXUtils/fromFXImage image nil)]
            (ImageIO/write bufimage "png" (File. filename))
            (println "\nwrote " filename "md5=" (digest/md5 (File. filename)))))))
    (Platform/runLater (fn [] (.countDown ll)))
    (.await ll)))

(defn exit []
  (println "exiting ...")
  (Platform/runLater (fn [] (.close @stage)))
  (Platform/exit))

(defn scale-linear
  [{domain :domain range :range :as all}]
  (->
    (fn [x]
      (let [domain-size (- (last domain) (first domain))
            domain-offset (- x (first domain))
            domain-relative (/ domain-offset domain-size)
            range-size (- (last range) (first range))
            range-output (+ (first range) (* domain-relative range-size))]
        range-output))
    (with-meta all)))

(defn domain
  [scale]
  (get (meta scale) :domain))

(defn scale-range
  [scale]
  (get (meta scale) :range))

(defn path [points]
  (str "M"
       (string/join " L" points)))

(defn line
  ([points]
   (line {:fill "none" :stroke "#000" :stroke-width 1} points))
  ([props points]
   [:path (assoc props :d (path (map (fn [[x y]] (str (.doubleValue x) "," (.doubleValue y))) points)))]))

(defn title
  [text]
  [:g [:text {:x                  15
              :y                  15
              :alignment-baseline "hanging"
              :font-family        "Arial"
              :font-size          "20px"
              :font-weight        "bold"}
       text]])

(defn sub-title
  [text]
  [:g [:text {:x                  15
              :y                  (+ 22 15)
              :alignment-baseline "hanging"
              :font-family        "Arial"
              :font-size          "14px"
              :font-weight        "normal"}
       text]])

(defn svg-attrs
  [width height margin]
  {:width  (+ (:left margin) (:right margin) width)
   :height (+ (:top margin) (:bottom margin) height)
   :xmlns  "http://www.w3.org/2000/svg"})