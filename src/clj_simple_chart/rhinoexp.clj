(ns clj-simple-chart.rhinoexp
  (:require [base64-clj.core :as base64]
            [clojure.core.async :as async])
  (:import (org.mozilla.javascript Context NativeObject)
           (org.apache.commons.io FileUtils)
           (java.io File FileReader BufferedInputStream FileInputStream InputStreamReader)
           (java.nio.charset StandardCharsets)))

(defonce input (async/chan))
(defonce output (async/chan))

(defn js-handle [fn]
  (println "executing stuff in " (Thread/currentThread))
  (fn))

(defn js-loop []
  (async/thread (while true
                  (let [fn (async/<!! input)]
                    (async/>!! output (js-handle fn))))))

(defonce js-loop-thread (js-loop))

(defn run-js-thread [f]
  (async/>!! input f)
  (async/<!! output))

;;; ;;; ;;; STATE
(defonce cx (atom nil))
(defonce scope (atom nil))
(defonce parsefont (atom nil))
(defonce fonts (atom {}))
;;; ;;; ;;; END OF STATE

(defn set-lang-version [cx]
  (.setLanguageVersion cx Context/VERSION_ES6)
  cx)

(defn load-jvm-npm [cx scope]
  (let [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader cx scope reader "<cmd>" 1 nil)
    (.close reader)))

(defn eval-str [x]
  (.evaluateString @cx @scope x "<cmd>" 1 nil))

(defn bootstrap-rhino []
  (swap! cx (fn [old-cx] (set-lang-version (Context/enter))))
  (swap! scope (fn [old-scope] (.initStandardObjects @cx)))
  (load-jvm-npm @cx @scope)
  (eval-str "var opentype = require('./resources/opentype.js')")
  (eval-str "var b64 = require('./resources/base64-arraybuffer.js')")
  (eval-str "function parseFont(payload) { return opentype.parse(b64.decode(payload)); }")
  (swap! parsefont (fn [old-parsefont] (.get @scope "parseFont"))))

(defn bootstrap-rhino-if-needed []
  (when-not @cx (run-js-thread bootstrap-rhino)))

(defn load-font [filename]
  (when-not (get @fonts filename)
    (let [font-bytes (FileUtils/readFileToByteArray (File. filename))
          font-b64 (String. (base64/encode-bytes font-bytes))]
      (run-js-thread (fn []
                       (swap! fonts (fn [old-fonts]
                                      (println "loading font" filename)
                                      (assoc old-fonts filename (.call @parsefont @cx @scope @scope (object-array [font-b64])))))))
      :done)))

  (defn load-fonts []
    (bootstrap-rhino-if-needed)
    (doall (map load-font ["./resources/fonts/Roboto-Black.ttf"
                           "./resources/fonts/Roboto-Regular.ttf"])))


  ;
  ;(defn js-to-clj [x]
  ;  (zipmap (map keyword (keys x)) (vals x)))
  ;
  ;

  #_(defn get-path
      [text size]
      (let [font-bytes (FileUtils/readFileToByteArray (File. "./resources/fonts/Roboto-Black.ttf"))
            cx (set-lang-version (Context/enter))
            scope (.initStandardObjects cx)]
        (load-jvm-npm cx scope)
        (eval-str cx scope "var opentype = require('./resources/opentype.js');")
        (eval-str cx scope "var b64 = require('./resources/base64-arraybuffer.js');")
        (eval-str cx scope (str "var base64 = '" (String. (base64/encode-bytes font-bytes)) "';"))
        (eval-str cx scope "var ab = b64.decode(base64);")
        (eval-str cx scope "var font = opentype.parse(ab);")
        (time (let [font (.get scope "font")
                    getPath (NativeObject/getProperty font "getPath")
                    path (.call getPath cx scope font (object-array [text 0 0 size]))
                    bb-fn (NativeObject/getProperty path "getBoundingBox")
                    to-path-data-fn (NativeObject/getProperty path "toPathData")
                    bb (.call bb-fn cx scope path (object-array []))
                    path (.call to-path-data-fn cx scope path (object-array []))
                    result {:bounding-box (js-to-clj bb) :path path}]
                (do result)
                (Context/exit)
                result))))