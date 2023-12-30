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

(defn rect
  [x y w h opts])

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
        (println res-str)
        (spit "janei.svg" res-str)
        (when *dev-mode*
          (devserver/push-svg! res-str))
        (nth parsed 2)))))

(do
  (binding [*dev-mode* true]
    ;(def cc (circle 80 120 50 {:fill "red"}))
    ;(prn (str/includes? (pr-str cc) "red"))
    (prn (str/includes? (pr-str (circle 80 120 150 {:stroke "black" :fill "blue"})) "blue"))))
    ;(prn cc)))
