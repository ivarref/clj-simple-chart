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
           (javafx.concurrent Worker$State)
           (javafx.event EventHandler)
           (javafx.scene.input KeyCode)
           (javafx.beans.value ChangeListener)))

(defonce latch (CountDownLatch. 1))
(defonce zoom (atom 1.0))
(defonce webview (atom nil))
(defonce stage (atom nil))
(defonce engine (atom nil))
(defonce scene (atom nil))

(defonce input-height (atom nil))
(defonce input-width (atom nil))

(defonce window-height (atom nil))
(defonce window-width (atom nil))

(defn update-zoom-and-resize! [zoomvalue]
  (swap! zoom (fn [o] zoomvalue))
  (Platform/runLater (fn [] (.setZoom @webview @zoom)))
  (Platform/runLater (fn [] (.setPrefHeight @webview (* @zoom @input-height))))
  (Platform/runLater (fn [] (.setPrefWidth @webview (* @zoom @input-width))))
  (Platform/runLater (fn [] (.sizeToScene @stage))))

(defn zoom-handler
  [zoomvalue]
  (swap! zoom (fn [o] (* o zoomvalue)))
  (Platform/runLater (fn [] (.setZoom @webview @zoom))))

(def zoom-delta 0.025)

; input-width * zoom = new
; zoom = new / input-width

(defn key-pressed-handler
  [v]
  (cond (and (.isControlDown v)
             (= (.getText v) "0"))
        (update-zoom-and-resize! 1.0)

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/W))
        (update-zoom-and-resize! (/ @window-width @input-width))

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/H))
        (update-zoom-and-resize! (/ @window-height @input-height))

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/MINUS))
        (update-zoom-and-resize! (- @zoom zoom-delta))

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/EQUALS))
        (update-zoom-and-resize! (+ @zoom zoom-delta))

        (and (= (.getCode v) KeyCode/T))
        (.setAlwaysOnTop @stage (not (.isAlwaysOnTop @stage)))

        :else (do #_(println "unhandled" v))))

(defn key-released-handler
  [v]
  (cond (and (.isControlDown v)
             (= (.getText v) "0"))
        (update-zoom-and-resize! 1.0)

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/W))
        (update-zoom-and-resize! (/ @window-width @input-width))

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/MINUS))
        (update-zoom-and-resize! (- @zoom zoom-delta))

        (and (.isControlDown v)
             (= (.getCode v) KeyCode/EQUALS))
        (update-zoom-and-resize! (+ @zoom zoom-delta))

        :else (do #_(println "unhandled" v))))

#_(defn scroll-handler [v]
    (println "hello from scroll-handler" v))

(defn window-width-change-handler [new]
  (when (number? new)
    (swap! window-width (fn [o] new))))

(defn window-height-change-handler [new]
  (when (number? new)
    (swap! window-height (fn [o] new))))

(defn -start [this internal-stage]
  (.setTitle internal-stage "SVG Output")
  (let [internal-webview (WebView.)
        internal-engine (.getEngine internal-webview)
        layout (VBox. 0.0)
        internal-scene (Scene. layout)
        children (.getChildren layout)]
    (.loadContent internal-engine "about:blank")
    (.setAll children [internal-webview])
    (.setScene internal-stage internal-scene)
    (.setOnZoom internal-scene (reify EventHandler
                                 (handle [_ v]
                                   (zoom-handler (.getZoomFactor v)))))
    #_(.setOnScroll internal-scene (reify EventHandler
                                     (handle [_ v]
                                       (scroll-handler v))))
    (.addListener (.widthProperty internal-scene) (reify ChangeListener
                                                    (changed [_ val old new]
                                                      (window-width-change-handler new))))
    (.addListener (.heightProperty internal-scene) (reify ChangeListener
                                                     (changed [_ val old new]
                                                       (window-height-change-handler new))))
    (.setOnKeyPressed internal-scene (reify EventHandler
                                       (handle [_ v]
                                         (key-pressed-handler v))))
    (.setOnKeyReleased internal-scene (reify EventHandler
                                        (handle [_ v]
                                          (key-released-handler v))))
    (.show internal-stage)
    (swap! engine (fn [x] internal-engine))
    (swap! stage (fn [x] internal-stage))
    (swap! scene (fn [x] internal-scene))
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
    (swap! input-height (fn [o] height))
    (swap! input-width (fn [o] width))
    (bootstrap)
    (Platform/runLater (fn [] (.setZoom @webview @zoom)))
    (Platform/runLater (fn [] (.setPrefHeight @webview (* @zoom height))))
    (Platform/runLater (fn [] (.setPrefWidth @webview (* @zoom width))))
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
    (render-svg t)
    (Platform/runLater
      (fn []
        (let [snap (SnapshotParameters.)]
          (.setViewport snap (Rectangle2D. 0 0 (* @zoom width) (* @zoom height)))
          (let [image (.snapshot @webview snap nil)
                bufimage (SwingFXUtils/fromFXImage image nil)]
            (ImageIO/write bufimage "png" (File. filename))))))
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