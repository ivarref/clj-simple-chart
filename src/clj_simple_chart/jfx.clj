(ns clj-simple-chart.jfx
  (:gen-class
    :extends javafx.application.Application)
  (:require [hiccup.core :as hiccup])
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
  (.start (Thread. (fn [] (Application/launch clj_simple_chart.jfx (into-array String []))))))

(defn bootstrap []
  (when-not @engine
    (launch)
    (.await latch)))

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
  (Platform/runLater (fn [] (.loadContent @engine s "text/html")))
  (wait-for-worker))

(defn export-to-file [filename t]
  (let [s (hiccup.core/html t)]
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
    (bootstrap)
    (Platform/runLater (fn [] (.setPrefHeight @webview height)))
    (Platform/runLater (fn [] (.setPrefWidth @webview width)))
    (render-string s)
    (Platform/runLater (fn [] (.sizeToScene @stage)))
    (Platform/runLater (fn [] (.show @stage)))
    (Platform/runLater (fn [] (.toFront @stage)))
    s))

(defn render-to-png [filename t]
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
            #_(println "\nwrote " filename #_"md5=" #_(digest/md5 (File. filename)))))))
    (Platform/runLater (fn [] (.countDown ll)))
    (.await ll)))

(defn render
  ([svg] (render-svg svg))
  ([f1 f2 svg]
   (render f1 svg)
   (render f2 svg))
  ([filename svg]
   (cond (.endsWith filename ".svg") (export-to-file filename svg)
         (.endsWith filename ".png") (render-to-png filename svg)
         :else (throw (Exception. (str "Unknown file type " filename))))
   (render svg)
   (Platform/runLater (fn [] (.setTitle @stage filename)))))

(defn exit []
  (println "exiting ...")
  (Platform/runLater (fn [] (.close @stage)))
  (Platform/exit))