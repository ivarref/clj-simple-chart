(ns clj-simple-chart.core
  (:gen-class
    :extends javafx.application.Application)
  (:require [hiccup.core :as hiccup]
            [digest :as digest]
            [clojure.string :as string]
            [clj-simple-chart.ticks :as ticks]
            )
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

(defn number-of-decimals [scale]
  (let [domain (:domain (meta scale))
        domain-diff (Math/abs (apply - domain))]
    (cond (>= domain-diff 8) 0
          (>= domain-diff 1) 1
          :else 2)))

(defn scale-format [scale v]
  (format (str "%." (number-of-decimals scale) "f") v))

(defn left-y-axis [scale]
  (let [domain (:domain (meta scale))
        color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)
        tiks (apply ticks/ticks domain)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M-6," (apply max range) ".5 H0.5 V0.5 H-6")}]
     (map (fn [d] [:g {:transform (translate 0 (scale d))}
                   [:line {:stroke color :x2 -6 :y1 0.5 :y2 0.5}]
                   [:text {:x           -9
                           :text-anchor "end"
                           :fill        color
                           :dy          ".32em"
                           :y           0.5}
                    (fmt d)]]) tiks)]))

(defn right-y-axis [scale]
  (let [domain (:domain (meta scale))
        color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)
        tiks (apply ticks/ticks domain)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M6," (apply max range) ".5 H0.5 V0.5 H6")}]
     (map (fn [d] [:g {:transform (translate 0 (scale d))}
                   [:line {:stroke color :x2 6 :y1 0.5 :y2 0.5}]
                   [:text {:x           9
                           :text-anchor "start"
                           :fill        color
                           :dy          ".32em"
                           :y           0.5}
                    (fmt d)]]) tiks)]))

(defn bottom-x-axis [scale]
  (let [domain (:domain (meta scale))
        color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)
        tiks (apply ticks/ticks domain)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5,6 V0.5 H" (apply max range) ".5 V6")}]
     (map (fn [d] [:g {:transform (translate (scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 6}]
                   [:text {:x           0.5
                           :text-anchor "middle"
                           :fill        color
                           :dy          ".71em"
                           :y           9}
                    (fmt d)]]) tiks)]))

(defn top-x-axis [scale]
  (let [domain (:domain (meta scale))
        color (get (meta scale) :color "#000")
        range (:range (meta scale))
        fmt (partial scale-format scale)
        tiks (apply ticks/ticks domain)]
    [:g
     [:path {:stroke       color
             :stroke-width "1"
             :fill         "none"
             :d            (str "M0.5,-6 V0.5 H" (apply max range) ".5 V-6")}]
     (map (fn [d] [:g {:transform (translate (scale d) 0)}
                   [:line {:stroke color :x1 0.5 :x2 0.5 :y2 -6}]
                   [:text {:x           0.5
                           :text-anchor "middle"
                           :fill        color
                           :dy          "0em"
                           :y           -9}
                    (fmt d)]]) tiks)]))


(def width 470)
(def height 470)
(def margin {:top 50 :bottom 50 :left 60 :right 60})

(def y (scale-linear {:color "red" :domain [0 100] :range [height 0]}))
(def y2 (scale-linear {:color "blue" :domain [0 1.69] :range [height 0]}))
(def x (scale-linear {:color "green" :domain [0 100] :range [0 width]}))
(def x-top (scale-linear {:color "fuchsia" :domain [0 8] :range [0 width]}))

(defn path [points]
  (str "M"
       (string/join " L" points)))

(defn line
  ([points]
   (line {:fill "none" :stroke "#000" :stroke-width 1} points))
  ([props points]
   [:path (assoc props :d (path (map (fn [[x y]] (str x "," y)) points)))]))

(defn dotted-line [{fill :fill stroke :stroke} points]
  [:g
   (line {:fill "none" :stroke stroke :stroke-width 2} points)
   (map (fn [[x y]]
          [:circle
           {:fill fill
            :stroke stroke
            :stroke-width 2
            :r 5
            :cx x
            :cy y}])
        points)])

(defn diagram
  []
  [:svg {:width  (+ (:left margin) (:right margin) width)
         :height (+ (:top margin) (:bottom margin) height)
         :xmlns  "http://www.w3.org/2000/svg"}
   [:g {:transform (translate (:left margin) (:top margin))}
    [:g (left-y-axis y)]
    [:g {:transform (translate width 0)} (right-y-axis y2)]
    [:g {:transform (translate 0 height)} (bottom-x-axis x)]
    [:g (top-x-axis x-top)]
    (dotted-line {:fill "yellow"
                  :stroke "black"}
                 (map (fn [d] [(x d) (y d)]) (range 0 (+ 10 100) 10)))
    ]])
