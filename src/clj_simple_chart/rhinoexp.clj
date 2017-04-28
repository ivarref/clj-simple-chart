(ns clj-simple-chart.rhinoexp
  (:require [base64-clj.core :as base64]
            [clojure.core.async :as async])
  (:import (org.mozilla.javascript Context NativeObject)
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
                        (println "error in js-loop ..." (.getMessage e))
                        (async/>!! exception e)))))))

(defn- run-js-thread [f]
  (async/>!! input f)
  (let [[result ch] (async/alts!! [output exception])]
    (if (= ch exception)
      (throw result)
      result)))

(defonce js-loop-thread (js-loop)) ;;; bootstrap JS loop

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
  (let [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader cx scope reader "<cmd>" 1 nil)
    (.close reader)))

(defn- eval-str [x]
  (.evaluateString @cx @scope x "<cmd>" 1 nil))

(defn- bootstrap-rhino []
  (swap! cx (fn [old-cx] (set-lang-version (Context/enter))))
  (swap! scope (fn [old-scope] (.initStandardObjects @cx)))
  (load-jvm-npm @cx @scope)
  (eval-str "var opentype = require('./resources/opentype.js')")
  (eval-str "var b64 = require('./resources/base64-arraybuffer.js')")
  (eval-str "function parseFont(payload) { return opentype.parse(b64.decode(payload)); }")
  (swap! parsefont (fn [old-parsefont] (.get @scope "parseFont"))))

(defn- bootstrap-rhino-if-needed []
  (when-not @cx (run-js-thread bootstrap-rhino)))

(defn- load-font [filename]
  (if-not (get @fonts (.getName filename))
    (let [font-bytes (FileUtils/readFileToByteArray filename)
          font-b64 (String. (base64/encode-bytes font-bytes))]
      (run-js-thread (fn []
                       (swap! fonts (fn [old-fonts]
                                      #_(println "loading font" (.getName filename))
                                      (assoc old-fonts (.getName filename) (.call @parsefont @cx @scope @scope (object-array [font-b64])))))))
      :done)
    :already-loaded))

(defn- load-fonts []
  (bootstrap-rhino-if-needed)
  (doall (map load-font (->> (file-seq (File. "./resources/fonts"))
                             (filter #(.endsWith (.getName %) ".ttf"))))))

(defn- property-ids [obj]
  (vec (run-js-thread #(NativeObject/getPropertyIds obj))))

(defn- get-property [obj prop]
  (run-js-thread #(NativeObject/getProperty obj prop)))

(defn- font-fullname [font]
  (let [names (get-property font "names")
        fullnames (vals (get names "fullName"))]
    (if (= 1 (count fullnames))
      (first fullnames)
      (throw (Exception. (str "No support for multiple fontnames:" fullnames))))))

(defn- make-font-names-map []
  (load-fonts)
  (reduce (fn [o font]
            (update o (font-fullname font)
                    (fn [old-value]
                      (if old-value
                        (throw (Exception. (str "Duplicate font " old-value)))
                        font)))) {} (vals @fonts)))

(defonce font-name-to-font (make-font-names-map))

(defn get-path-data [fontname text x y size]
  {:pre [(some #{fontname} (keys font-name-to-font))]}
  (run-js-thread (fn []
                   (let [font (get font-name-to-font fontname)
                         get-path (NativeObject/getProperty font "getPath")
                         path (.call get-path @cx @scope font (object-array [text x y size]))
                         to-path-data-fn (NativeObject/getProperty path "toPathData")
                         path-data (.call to-path-data-fn @cx @scope path (object-array []))]
                     path-data))))

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