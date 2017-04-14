(ns clj-simple-chart.core
  (:gen-class
    :extends javafx.application.Application)
  (:require [hiccup.core :as hiccup]
            [digest :as digest])
  (:import (javafx.application Application Platform)
           (java.util.concurrent CountDownLatch)
           (javafx.scene.web WebView)
           (javafx.scene.layout VBox)
           (javafx.scene Scene SnapshotParameters)
           (javafx.geometry Rectangle2D)
           (javafx.embed.swing SwingFXUtils)
           (javax.imageio ImageIO)
           (java.io File)))

(defonce latch (CountDownLatch. 1))
(defonce webview (atom nil))
(defonce stage (atom nil))
(defonce engine (atom nil))

(defn -start [this internal-stage]
  (.setTitle internal-stage "hello world")
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

(defn render-string [s]
  (if @engine
    (do
      (let [ll (CountDownLatch. 1)]
        (Platform/runLater (fn [] (.loadContent @engine s "text/html")))
        (Platform/runLater (fn [] (.countDown ll)))
        (.await ll)))
    (do
      (launch)
      (.await latch)
      (recur s))))

(defn translate [x y]
  (str "translate(" x "," y ")"))

(defn render-tag [t]
  (let [body [:body (style
                      :margin "0 !important"
                      :padding "0 !important"
                      :overflow-x "hidden"
                      :overflow-y "hidden")]
        s (hiccup.core/html (conj body t))]
    (render-string s)
    s))

(defn render-to-file [filename t]
  (let [ll (CountDownLatch. 1)
        attrs (second t)
        width (:width attrs)
        height (:height attrs)]
    (render-tag t)
    (Platform/runLater (fn [] (.sizeToScene @stage)))
    (render-tag t) ; TODO: Can this be simplified?
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


(def width 960)
(def height 500)
(def margin {:top 10 :bottom 10 :left 10 :right 10})

(render-to-file
  "hello.png"
  [:svg {:width  (+ (:left margin) (:right margin) width)
         :height (+ (:top margin) (:bottom margin) height)}
   [:circle {:cx 0 :cy 0 :r 50 :fill "yellow" :stroke "black"}]
   [:g {:transform (translate (:left margin) (:top margin))}
    [:rect {:x 0 :y 0 :width width :height height :fill "none" :stroke "red"}]
    [:circle {:cx 50 :cy 100 :r 10 :fill "yellow" :stroke "black"}]
    [:circle {:cx 50 :cy 100 :r 1 :fill "yellow" :stroke "black"}]
    [:line {:x1 50 :y1 100 :x2 width :y2 100 :stroke "red"}]
    [:text {:x 50 :y 100 :dy ".32em" :font-size "200px"} "1,23456789"]
    [:line {:x1 0 :y1 0 :x2 width :y2 height :stroke "blue"}]
    [:line {:x1 width :y1 0 :x2 0 :y2 height :stroke "blue"}]
    ]])

(defn -main
  "This should be pretty simple."
  []
  (println "file written as a side effect..!"))
