(ns clj-simple-chart.rhinoexp
  (:require [base64-clj.core :as base64])
  (:import (org.mozilla.javascript Context)
           (org.apache.commons.io FileUtils)
           (java.io File FileReader BufferedInputStream FileInputStream InputStreamReader)
           (java.nio.charset StandardCharsets)))

(def cx (let [cx (Context/enter)]
          (.setLanguageVersion cx Context/VERSION_ES6)
          cx))

(def scope (.initStandardObjects cx))

(defn load-jvm-npm []
  (let [reader (InputStreamReader. (BufferedInputStream. (FileInputStream. "./resources/jvm-npm.js")) StandardCharsets/UTF_8)]
    (.evaluateReader cx scope reader "<cmd>" 1 nil)
    (.close reader)))

(defn js-to-clj [x]
  (zipmap (map keyword (keys x)) (vals x)))

(defn eval-str [x]
  (.evaluateString cx scope x "<cmd>" 1 nil))

(def font-bytes (FileUtils/readFileToByteArray (File. "./resources/fonts/Roboto-Black.ttf")))
(load-jvm-npm)
(eval-str "var opentype = require('./resources/opentype.js');")
(eval-str "var b64 = require('./resources/base64-arraybuffer.js');")
(eval-str (str "var base64 = '" (String. (base64/encode-bytes font-bytes)) "';"))
(eval-str "var ab = b64.decode(base64);")
(eval-str "var font = opentype.parse(ab);")
(eval-str "var path = font.getPath('hello world');")
(def bb (js-to-clj (eval-str "path.getBoundingBox();")))
(def path (eval-str "path.toPathData()"))
(println bb)
