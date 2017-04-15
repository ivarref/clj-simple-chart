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
           (java.io File)
           (javafx.concurrent Worker$State)))

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

(defn render-string [s]
  (if @engine
    (do
      (Platform/runLater (fn [] (.loadContent @engine s "text/html")))
      (wait-for-worker))
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

(defn export-to-file [filename t]
  (let [s (hiccup.core/html t)]
    (spit filename s)))

(defn render-to-file [filename t]
  (let [ll (CountDownLatch. 1)
        attrs (second t)
        width (:width attrs)
        height (:height attrs)]
    (render-tag t)
    (Platform/runLater (fn [] (.setPrefHeight @webview height)))
    (Platform/runLater (fn [] (.setPrefWidth @webview width)))
    (Platform/runLater (fn [] (.sizeToScene @stage)))
    (Platform/runLater (fn [] (.show @stage)))
    (wait-for-worker)
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
  [{domain :domain range :range}]
  (fn [x]
    (let [domain-size (- (last domain) (first domain))
          domain-offset (- x (first domain))
          domain-relative (/ domain-offset domain-size)
          range-size (- (last range) (first range))
          range-output (+ (first range) (* domain-relative range-size))]
      range-output)))

(def width 960)
(def height 500)
(def margin {:top 10 :bottom 10 :left 10 :right 10})

(def y (scale-linear {:domain [0 100] :range [height 0]}))
(def x (scale-linear {:domain [0 100] :range [0 width]}))

(def diagram
  [:svg {:width  (+ (:left margin) (:right margin) width)
         :height (+ (:top margin) (:bottom margin) height)
         :xmlns "http://www.w3.org/2000/svg"}
   [:circle {:cx 0 :cy 0 :r 50 :fill "yellow" :stroke "black"}]
   [:g {:transform (translate (:left margin) (:top margin))}
    [:rect {:x 0 :y 0 :width width :height height :fill "none" :stroke "red"}]
    [:circle {:cx 50 :cy 100 :r 10 :fill "yellow" :stroke "black"}]
    [:circle {:cx (x 25) :cy (y 25) :r 25 :fill "yellow" :stroke-width 5 :stroke "black"}]
    [:circle {:cx (x 50) :cy (y 50) :r 25 :fill "cyan" :stroke-width 5 :stroke "black"}]
    [:circle {:cx (x 75) :cy (y 25) :r 25 :fill "red" :stroke-width 5 :stroke "black"}]
    [:circle {:cx 50 :cy 100 :r 1 :fill "yellow" :stroke "black"}]
    [:line {:x1 50 :y1 100 :x2 width :y2 100 :stroke "red"}]
    [:text {:x 50 :y 100 :dy ".32em" :font-size "200px"} "1,23456789"]
    [:line {:x1 0 :y1 0 :x2 width :y2 height :stroke "blue"}]
    [:line {:x1 width :y1 0 :x2 0 :y2 height :stroke "blue"}]
    ]])
