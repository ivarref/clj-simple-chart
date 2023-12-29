(ns clj-simple-chart.roughjs
  (:require [base64-clj.core :as base64]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [clj-simple-chart.translate :refer [translate]]
            [clojure.string :as str]
            [clojure.edn :as edn])
  (:import (org.mozilla.javascript Context JavaScriptException NativeObject RhinoException)
           (org.apache.commons.io FileUtils)
           (java.io File FileReader BufferedInputStream FileInputStream InputStreamReader)
           (java.nio.charset StandardCharsets)))

(defonce input (async/chan))
(defonce exit (async/chan))
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
(defonce cx (atom nil))
(defonce scope (atom nil))
(defonce parsefont (atom nil))
(defonce fonts (atom {}))
;;; ;;; ;;; END OF STATE

(defn- set-lang-version [cx]
  (.setLanguageVersion cx Context/VERSION_ES6)
  cx)

(defn- load-jvm-npm [cx scope]
  (with-open [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader cx scope reader "<cmd>" 1 nil)))

(defn- eval-str [x]
  (.evaluateString @cx @scope x "<cmd>" 1 nil))

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
  (swap! cx (fn [old-cx] (set-lang-version (Context/enter))))
  (swap! scope (fn [old-scope] (.initStandardObjects @cx)))
  (load-jvm-npm @cx @scope)
  (eval-str (slurp "resources/roughlatestes2015.js")) ; pulled 2023-12-29
  (eval-str (slurp "resources/xmldom.js"))
  (eval-str (slurp "resources/roughhelper.js"))
  (eval-str "janei()"))

; how to transpile rough.js: https://stackoverflow.com/questions/34747693/how-do-i-get-babel-6-to-compile-to-es5-javascript
; npm install babel-cli babel-preset-es2015
; npx babel  ./rough.js --out-file ./roughes2015.js --presets babel-preset-es2015

(comment
  (let [v (run-js-thread
            (fn []
              (eval-str "rough")))]
    (prn (property-ids v))))

(defn- bootstrap-rhino-if-needed []
  (when-not @cx (run-js-thread bootstrap-rhino)))

(def _bootstrap (bootstrap-rhino-if-needed))
