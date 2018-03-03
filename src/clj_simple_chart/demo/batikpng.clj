(ns clj-simple-chart.demo.batikpng
  (:import [org.apache.batik.transcoder.image PNGTranscoder]
           (org.apache.batik.transcoder TranscoderInput TranscoderOutput)
           (java.io StringReader BufferedOutputStream FileOutputStream)
           (java.awt Color)))

(defn write-png [^String s ^String output-filename]
  (let [t (doto (PNGTranscoder.)
            (.addTranscodingHint PNGTranscoder/KEY_PIXEL_TO_MM (Float. 0.2645833)) ; 96 dpi
            (.addTranscodingHint PNGTranscoder/KEY_BACKGROUND_COLOR Color/white))]
    (with-open [input (StringReader. s)
                output (BufferedOutputStream. (FileOutputStream. output-filename))]
      (.transcode t (TranscoderInput. input) (TranscoderOutput. output)))))

(defn demo []
  (write-png (slurp "img/ssb-svg/nettokontantstraum.svg") "demo.png"))
  ; MD5 (demo.png) = 014a1215aa4c365462bcdcff920c6f4d on OS X
