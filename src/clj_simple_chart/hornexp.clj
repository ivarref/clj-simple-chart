(ns clj-simple-chart.hornexp
  (:require [base64-clj.core :as base64])
  (:import (javax.script ScriptEngineManager)
           (java.io File)
           (org.apache.commons.io FileUtils)))

(defonce engine-manager (ScriptEngineManager.))
(defonce engine (.getEngineByName engine-manager "nashorn"))

(.eval engine "load('./resources/jvm-npm.js');")
(.eval engine "var opentype = require('./resources/opentype.js');")
(.eval engine "var b64 = require('./resources/base64-arraybuffer.js');")

(def font-bytes (FileUtils/readFileToByteArray (File. "./resources/fonts/Roboto-Black.ttf")))
(.eval engine (str "var base64 = '" (String. (base64/encode-bytes font-bytes)) "';"))
(.eval engine "var ab = b64.decode(base64);")
;(.eval engine "var font = opentype.parse(ab);") ;;; doesn't work

(.eval engine "
function uint32(x) {
  var buffer = new ArrayBuffer(8);
  var dataview = new DataView(buffer);
  dataview.setUint32(0, x);
  return dataview.getUint32(0, false);
}
print(uint32(0x5F0F3CF5) === uint32(0x5F0F3CF5));
")