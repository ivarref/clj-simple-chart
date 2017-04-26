(ns clj-simple-chart.rhinoexp
  (:import (org.mozilla.javascript Context)))

(def cx (let [cx (Context/enter)]
          (.setLanguageVersion cx Context/VERSION_ES6)
          cx))

(def scope (.initStandardObjects cx))

(def r (.evaluateString cx scope
                          "
  function uint32(x) {
    var buffer = new ArrayBuffer(8);
    var dataview = new DataView(buffer);
    dataview.setUint32(0, x);
    return dataview.getUint32(0, false);
  }
  " "<cmd>" 1 nil))

(println (.evaluateString cx scope "uint32(0x7f) == uint32(0x7f)" "<cmd>" 1 nil))
(println (.evaluateString cx scope "uint32(0x80) == uint32(0x80)" "<cmd>" 1 nil))
(println (.evaluateString cx scope "uint32(0x5F0F3CF5) === uint32(0x5F0F3CF5)" "<cmd>" 1 nil))
