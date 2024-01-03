(ns clj-simple-chart.roughjs
  (:require [cheshire.core :as json]
            [clojure.core.async :as async]
            [clojure.string :as str]
            [dk.cst.xml-hiccup :as xh]
            [clj-simple-chart.webserver :as devserver])
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
  (eval-str (slurp "resources/roughhelper.js")))
;(eval-str "janei()"))

; how to transpile rough.js: https://stackoverflow.com/questions/34747693/how-do-i-get-babel-6-to-compile-to-es5-javascript
; npm install babel-cli babel-preset-es2015
; npx babel  ./rough.js --out-file ./roughes2015.js --presets babel-preset-es2015

(defn- bootstrap-rhino-if-needed []
  (when-not @context (run-js-thread bootstrap-rhino)))

(defonce _bootstrap (bootstrap-rhino-if-needed))

(def ^:dynamic *dev-mode* false)

(defn circle [cx cy d opts]
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

(defn circle2 [m]
  (circle (get m :cx)
          (get m :cy)
          (get m :r)
          m))

(defn rectangle [x y w h opts]
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

(defn clean-opts [opts]
  (dissoc opts :d :rough :simplification :fillStyle))

(defn validate-fillStyle! [opts]
  (let [valid ["hachure" "solid" "zigzag" "cross-hatch"
               "dots" "dashed" "zigzag-line"]]
    (when-let [inp (get opts :fillStyle)]
      (assert (contains? (into #{} valid) inp)
              (str ":fillStyle must be one of:\n"
                   (str/join " " valid)
                   "\nfillStyle was: " (pr-str inp))))))

(defn- path-inner [d opts]
  (validate-fillStyle! opts)
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

(comment
  (def data [:svg {:height "900", :width "900", :xmlns "http://www.w3.org/2000/svg"} [:g {} [:path {:d "M0.6019157481000358 0.886256456629726 C137.56270079470403 1.32974029473357, 274.07894181435074 1.7805850074613176, 432.9665103858171 -0.1813074331441411 M0.02332459676044617 0.9614684102999136 C137.8759623432271 0.1471134365211179, 275.3237681158494 0.06715752635703026, 431.78168477466625 0.28382520312386694", :fill "none", :stroke "black", :stroke-width "1"}]]]))

(comment
  (->> data
       (tree-seq seq? identity)
       (mapv prn)))

(defn- line-inner [x1 y1 x2 y2 opts]
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
        #_(prn (dissoc path-attrs :d))
        (when *dev-mode*
          (devserver/push-svg! res-str))
        [:path path-attrs]))))


(defn path [{:keys [d rough] :as opts}]
  (if rough
    (path-inner d (merge opts rough))
    [:path opts]))

(defn line [{:keys [x1 y1 x2 y2 rough] :as opts}]
  (if rough
    (do
      (line-inner (or x1 0.0)
                  (or y1 0.0)
                  (or x2 0.0)
                  (or y2 0.0)
                  (merge opts rough)))
    [:line opts]))

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
      #_(prn (str/includes? (pr-str (circle 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))


(comment
  {:x            (point xscale px)
   :y            (double (point yscale py))
   :height       (double (- (point yscale (+ py height))
                            (point yscale py)))
   :fill         fill
   :stroke       stroke
   :stroke-width stroke-width
   :style        "shape-rendering:crispEdges;"
   :width        (:bandwidth xscale)})

(defn rect [{:keys [x y rough height fill stroke stroke-width style width]
             :as   opts}]
  (if rough
    (rectangle x y width height (merge (select-keys opts [:fill :stroke :stroke-width :style]) rough))
    [:rect opts]))

#_(do
    (binding [*dev-mode* true]
      ;(rectangle 10 15 180 180 {:fill "none"})
      (circle 50 50 50 {:fill "yellow"})
      ;(def cc (circle 80 120 50 {:fill "red"}))
      ;(prn (str/includes? (pr-str cc) "red"))
      #_(prn (str/includes? (pr-str (circle 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))

#_(do
    (binding [*dev-mode* true]
      ;(def cc (circle 80 120 50 {:fill "red"}))
      ;(prn (str/includes? (pr-str cc) "red"))
      (prn (str/includes? (pr-str (circle 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))
;(prn cc)))
