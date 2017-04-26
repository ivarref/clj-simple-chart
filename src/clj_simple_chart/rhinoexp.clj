(ns clj-simple-chart.rhinoexp
  (:require [base64-clj.core :as base64])
  (:import (org.mozilla.javascript Context NativeObject)
           (org.apache.commons.io FileUtils)
           (java.io File FileReader BufferedInputStream FileInputStream InputStreamReader)
           (java.nio.charset StandardCharsets)))

(defn load-jvm-npm [cx scope]
  (let [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader cx scope reader "<cmd>" 1 nil)
    (.close reader)))

(defn eval-str [cx scope x]
  (.evaluateString cx scope x "<cmd>" 1 nil))

(defn js-to-clj [x]
  (zipmap (map keyword (keys x)) (vals x)))

(defn set-lang-version [cx]
  (.setLanguageVersion cx Context/VERSION_ES6)
  cx)

(defn get-path
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
          this-scope (.get scope "font")
          path (.call getPath cx scope this-scope (object-array [text 0 0 size]))
          bb-fn (NativeObject/getProperty path "getBoundingBox")
          to-path-data-fn (NativeObject/getProperty path "toPathData")
          bb (.call bb-fn cx scope path (object-array []))
          path (.call to-path-data-fn cx scope path (object-array []))
          result {:bounding-box (js-to-clj bb) :path path}]
      (do result)
      (Context/exit)
      result))))

(def r (get-path "hello world" 50))