(ns clj-simple-chart.roughjs
  (:require [cheshire.core :as json]
            [clj-simple-chart.webserver :as devserver]
            [clojure.core.async :as async]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [dk.cst.xml-hiccup :as xh])
  (:import (java.io BufferedInputStream FileInputStream InputStreamReader)
           (java.nio.charset StandardCharsets)
           (org.mozilla.javascript Context Function NativeObject RhinoException)))

(defonce input (async/chan))
(defonce output (async/chan))
(defonce exception (async/chan))

(defn- js-handle [fn] (fn))

(defn- js-loop []
  (async/thread (while true
                  (let [fn (async/<!! input)]
                    (try
                      (async/>!! output (js-handle fn))
                      (catch Exception e
                        (println "Error in js-loop:" (.getMessage e) (class e))
                        (when (instance? RhinoException e)
                          (println "Line number:" (.lineNumber e)))
                        (async/>!! exception e)))))))

(defn- run-js-thread [f]
  (async/>!! input f)
  (let [[result ch] (async/alts!! [output exception])]
    (if (= ch exception)
      (throw result)
      result)))

(defonce js-loop-thread (js-loop))

;;; ;;; ;;; STATE
(defonce context (atom nil))
(defonce scope (atom nil))
;;; ;;; ;;; END OF STATE

(defn- set-lang-version [context]
  (.setLanguageVersion context Context/VERSION_ES6)
  context)

(defn- load-jvm-npm [context scope]
  (with-open [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader context scope reader "<cmd>" 1 nil)))

(defn- eval-str [x]
  (.evaluateString @context @scope x "<cmd>" 1 nil))

(defn- property-ids [obj]
  (vec (run-js-thread #(NativeObject/getPropertyIds obj))))

(defn print-props [x]
  (let [v (run-js-thread
            (fn []
              (eval-str x)))]
    (if (string? v)
      (prn v)
      (property-ids v))))

(defn- bootstrap-rhino []
  (swap! context (fn [old-context] (set-lang-version (Context/enter))))
  (swap! scope (fn [old-scope] (.initStandardObjects @context)))
  (load-jvm-npm @context @scope)
  (eval-str (slurp "resources/roughlatestes2015.js"))       ; pulled 2023-12-29
  (eval-str (slurp "resources/xmldom.js"))
  (eval-str (slurp "resources/svgpathbounds.js"))
  (eval-str (slurp "resources/roughhelper.js")))
;(eval-str "janei()"))

; how to transpile rough.js: https://stackoverflow.com/questions/34747693/how-do-i-get-babel-6-to-compile-to-es5-javascript
; npm install babel-cli babel-preset-es2015
; npx babel  ./rough.js --out-file ./roughes2015.js --presets babel-preset-es2015

(defn- bootstrap-rhino-if-needed []
  (when-not @context (run-js-thread bootstrap-rhino)))

(defonce _bootstrap (bootstrap-rhino-if-needed))

(def ^:dynamic *dev-mode* false)

; Begin API towards rough.js

(s/def :roughjs/fillStyle #{"hachure" "solid" "zigzag" "cross-hatch"
                            "dots" "dashed" "zigzag-line"})

(s/def :roughjs/opts
  (s/keys
    :opt-un [:roughjs/fillStyle]))

(defn- clean-opts [opts]
  (dissoc opts :d :rough :simplification :fillStyle))

(defn- circle-inner
  [cx cy d opts]
  {:pre [(s/valid? :roughjs/opts opts)]}
  (run-js-thread
    (bound-fn []
      (when *dev-mode*
        (eval-str (slurp "resources/roughhelper.js")))
      (let [^Function circle-js (.get @scope "circle")
            res-str (try
                      (.call circle-js @context @scope @scope (object-array [cx cy d (json/generate-string opts)]))
                      (catch Throwable t
                        (.printStackTrace t)
                        (throw t)))
            parsed (xh/parse res-str)]
        (when *dev-mode*
          (devserver/push-svg! res-str))
        (nth parsed 2)))))

(defn- rectangle-inner
  [x y w h opts]
  {:pre [(s/valid? :roughjs/opts opts)]}
  (run-js-thread
    (bound-fn []
      (when *dev-mode*
        (eval-str (slurp "resources/roughhelper.js")))
      (let [^Function circle-js (.get @scope "rectangle")
            res-str (try
                      (.call circle-js @context @scope @scope (object-array [x y w h (json/generate-string opts)]))
                      (catch Throwable t
                        (.printStackTrace t)
                        (throw t)))
            parsed (xh/parse res-str)]
        (when *dev-mode*
          (devserver/push-svg! res-str))
        (nth parsed 2)))))

(defn- path-inner
  [d opts]
  {:pre [(s/valid? :roughjs/opts opts)]}
  (run-js-thread
    (bound-fn []
      (when *dev-mode*
        (eval-str (slurp "resources/roughhelper.js")))
      (let [^Function circle-js (.get @scope "path")
            res-str (try
                      (.call circle-js @context @scope @scope (object-array [d (json/generate-string opts)]))
                      (catch Throwable t
                        (.printStackTrace t)
                        (throw t)))
            parsed (xh/parse res-str)
            paths (->> (nth parsed 2)
                       (drop 2)
                       (mapv (fn [[_elem attrs]]
                               [:path (merge attrs (clean-opts opts))]))
                       (into [:g]))]
        (when *dev-mode*
          (devserver/push-svg! res-str))
        paths))))

(defn- line-inner
  [x1 y1 x2 y2 opts]
  {:pre [(s/valid? :roughjs/opts opts)]}
  (run-js-thread
    (bound-fn []
      (when *dev-mode*
        (eval-str (slurp "resources/roughhelper.js")))
      (let [^Function circle-js (.get @scope "line")
            res-str (try
                      (.call circle-js @context @scope @scope (object-array [x1 y1 x2 y2 (json/generate-string opts)]))
                      (catch Throwable t
                        (.printStackTrace t)
                        (throw t)))
            parsed (xh/parse res-str)
            opts-clean (dissoc opts :rough :x1 :x2 :y1 :y2)
            path-attrs (-> parsed
                           (get-in [2 2 1])
                           (merge opts-clean))]
        (when *dev-mode*
          (devserver/push-svg! res-str))
        [:path path-attrs]))))

(defn get-bounds
  [path-string]
  {:pre [(string? path-string)]}
  (run-js-thread
    (bound-fn []
      (when *dev-mode*
        (eval-str (slurp "resources/roughhelper.js")))
      (let [^Function js-fn (.get @scope "getBounds")
            res (try
                  (into [] (.call js-fn @context @scope @scope (object-array [path-string])))
                  (catch Throwable t
                    (.printStackTrace t)
                    (throw t)))
            _ (pr-str res)]
        {:x1 (nth res 0) ; same format as returned by opentype.js
         :y1 (nth res 1)
         :x2 (nth res 2)
         :y2 (nth res 3)}))))

(defn get-bounds-rect
  [path-string]
  {:pre [(string? path-string)]}
  (let [{:keys [x1 y1 x2 y2]} (get-bounds path-string)]
    {:x x1
     :y y1
     :width (- x2 x1)
     :height (- y2 y1)
     :stroke "red"
     :fill "none"}))

(comment
  (get-bounds "M0 0L10 10 20 0Z"))

; Begin public API

(defn circle [m]
  (circle-inner (get m :cx)
                (get m :cy)
                (get m :r)
                m))

(defn path [{:keys [d rough] :as opts}]
  (if rough
    (path-inner d (merge opts rough))
    [:path opts]))

(defn line [{:keys [x1 y1 x2 y2 rough] :as opts}]
  (if rough
    (line-inner (or x1 0.0)
                (or y1 0.0)
                (or x2 0.0)
                (or y2 0.0)
                (merge opts rough))
    [:line opts]))

(defn rect [{:keys [x y rough height width]
             :as   opts}]
  (if rough
    (rectangle-inner x y width height (merge (select-keys opts [:fill :stroke :stroke-width :style]) rough))
    [:rect opts]))

#_(do
    (require 'demo.refresh)
    (demo.refresh/ignore-one!)
    (binding [*dev-mode* true]
      (prn
        (str/includes?
          (pr-str (line {:rough          {:fillStyle "zigzag"}
                         :stroke         "green"
                         :stroke-opacity 0.25, :x2 432, :y1 0.5, :y2 0.5}))
          "opacity"))))


#_(do
    (binding [*dev-mode* true]
      ;(rectangle 10 15 180 180 {:fill "none"})
      (println (path "M37,17v15H14V17z M50,0H0v50h50z" {:fill "blue"}))
      ;(path "M80 80 A 45 45, 0, 0, 0, 125 125 L 125 80 Z" {:fill "green"})
      ;(def cc (circle 80 120 50 {:fill "red"}))
      ;(prn (str/includes? (pr-str cc) "red"))
      #_(prn (str/includes? (pr-str (circle-inner 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))

#_(do
    (binding [*dev-mode* true]
      ;(rectangle 10 15 180 180 {:fill "none"})
      (circle-inner 50 50 50 {:fill "yellow"})
      ;(def cc (circle 80 120 50 {:fill "red"}))
      ;(prn (str/includes? (pr-str cc) "red"))
      #_(prn (str/includes? (pr-str (circle-inner 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))

#_(do
    (binding [*dev-mode* true]
      ;(def cc (circle 80 120 50 {:fill "red"}))
      ;(prn (str/includes? (pr-str cc) "red"))
      (prn (str/includes? (pr-str (circle-inner 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))
;(prn cc)))
