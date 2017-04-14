(ns clj-simple-chart.core
  (:gen-class
    :extends javafx.application.Application)
  (:require [hiccup.core :as hiccup])
  (:import (javafx.application Application Platform)
           (java.util.concurrent CountDownLatch)
           (javafx.scene.web WebView)
           (javafx.scene.layout VBox)
           (javafx.scene Scene)))

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
