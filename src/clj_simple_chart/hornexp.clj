(ns clj-simple-chart.hornexp
  (:import (javax.script ScriptEngineManager)
           (java.io File)
           (org.apache.commons.io FileUtils)))

(defonce engine-manager (ScriptEngineManager.))
(defonce engine (.getEngineByName engine-manager "nashorn"))

;(.eval engine "load('./resources/jvm-npm.js');")
;(.eval engine "var opentype = require('./resources/opentype.js');")

(.eval engine "var FileClazz = Java.type('java.io.File');")
(.eval engine "var FileUtils = Java.type('org.apache.commons.io.FileUtils');")
(.eval engine "var fontFile = new FileClazz('./resources/fonts/Roboto-Black.ttf');")
(.eval engine "var buffer = FileUtils.readFileToByteArray(fontFile);")
(.eval engine "print('buffer>>'); print(buffer[7] & 255);")
;var ab = new ArrayBuffer(buffer.length);
;var view = new Uint8Array(ab);
;for (var i = 0; i < buffer.length; ++i) {
;         view[i] = buffer[i];
;}
(.eval engine "var ab = new ArrayBuffer(buffer.length);")
(.eval engine "var view = new Uint8Array(ab);")
(.eval engine "for (var i=0; i < buffer.length; ++i) { view[i] = (buffer[i]&255); }")
(.eval engine "var font = opentype.parse(ab);")

(def fil (File. "./resources/fonts/Roboto-Black.ttf"))
(def bytez (FileUtils/readFileToByteArray fil))




#_(.eval engine "var ar = new ArrayBuffer(fontBytes.length);")
#_(.eval engine "for (var i=0; i<10; i++) { print(fontBytes[i]); }")
;(.eval engine "var font = opentype.parse(ar);")

; fileClazz = java.io.File
; public static byte[] readFileToByteArray(File file)
; org.apache.commons.io.FileUtils
