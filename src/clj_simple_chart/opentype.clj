(ns clj-simple-chart.opentype
  (:require [base64-clj.core :as base64]
            [clojure.core.async :as async]
            [clojure.string :as string])
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

(defonce js-loop-thread (js-loop))                          ;;; bootstrap JS loop

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

(def roboto-regular (get font-name-to-font "Roboto Regular"))

(defn get-path-data [fontname text x y size]
  {:pre [(some #{fontname} (keys font-name-to-font))]}
  (run-js-thread (fn []
                   (let [font (get font-name-to-font fontname)
                         get-path (NativeObject/getProperty font "getPath")
                         path (.call get-path @cx @scope font (object-array [text x y size]))
                         to-path-data-fn (NativeObject/getProperty path "toPathData")
                         path-data (.call to-path-data-fn @cx @scope path (object-array []))]
                     path-data))))

(defn get-bounding-box [fontname text x y size]
  {:pre [(some #{fontname} (keys font-name-to-font))]}
  (run-js-thread (fn []
                   (let [font (get font-name-to-font fontname)
                         get-path (NativeObject/getProperty font "getPath")
                         path (.call get-path @cx @scope font (object-array [text x y size]))
                         bb-fn (NativeObject/getProperty path "getBoundingBox")
                         bounding-box (.call bb-fn @cx @scope path (object-array []))]
                     (zipmap (map keyword (keys bounding-box)) (vals bounding-box))))))

(defn em-to-number [font-size em-str]
  (cond
    (.startsWith em-str "-") (- (em-to-number font-size (subs em-str 1)))
    (.startsWith em-str ".") (recur font-size (str "0" em-str))
    :else (* font-size (read-string (string/replace em-str "em" "")))))

(defn text-inner [{font-name    :font-name
                   font-size    :font-size
                   x            :x
                   y            :y
                   border-tight :border-tight}
                  text]
  (let [bb (get-bounding-box font-name text x y font-size)
        metadata {:font-size (double font-size)
                  :height    (Math/abs (- (:y1 bb) (:y2 bb)))}
        metadata (merge metadata bb)
        font-path [:path {:d (get-path-data font-name text x y font-size)}]
        txt (with-meta {} metadata)
        border (when border-tight
                 [:g [:line {:x1     (:x1 (meta txt))
                             :x2     (:x1 (meta txt))
                             :y1     (:y1 (meta txt))
                             :y2     (:y2 (meta txt))
                             :stroke border-tight
                             }]
                  [:line {:x1     (:x2 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y1 (meta txt))
                          :y2     (:y2 (meta txt))
                          :stroke border-tight
                          }]
                  [:line {:x1     (:x1 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y1 (meta txt))
                          :y2     (:y1 (meta txt))
                          :stroke border-tight
                          }]
                  [:line {:x1     (:x1 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y2 (meta txt))
                          :y2     (:y2 (meta txt))
                          :stroke border-tight}]])
        path [:g font-path border]]
    (with-meta path metadata)))

(defn text
  ([{font               :font
     font-size          :font-size
     x                  :x
     y                  :y
     dx                 :dx
     dy                 :dy
     text-anchor        :text-anchor
     border-tight       :border-tight
     alignment-baseline :alignment-baseline
     :as                config
     :or                {x                  0.0
                         y                  0.0
                         dx                 0.0
                         dy                 0.0
                         text-anchor        "start"
                         font-size          14
                         alignment-baseline "auto"
                         font               "Roboto Regular"
                         border-tight       false}
     } text]
   {:pre [(some #{font} (keys font-name-to-font))
          (number? font-size)
          (number? x)
          (number? y)]}
   (cond (and (string? dx) (.endsWith dx "em"))
         (recur (update config :dx (partial em-to-number font-size)) text)
         (and (string? dy) (.endsWith dy "em"))
         (recur (update config :dy (partial em-to-number font-size)) text)
         (not (string? text))
         (recur config (str text))
         (= text-anchor "middle")
         (let [bb (get-bounding-box font text 0 0 font-size)
               w (- (:x2 bb) (:x1 bb))]
           ;;; TODO: I'm not 100% sure about this, but looks quite good
           (recur (-> config
                      (assoc :dx (double (- dx (/ w 2) (:x1 bb))))
                      (dissoc :text-anchor)) text))
         (= text-anchor "end")
         (let [bb (get-bounding-box font text 0 0 font-size)]
           (recur (-> config
                      (assoc :dx (- dx (:x2 bb)))
                      (dissoc :text-anchor)) text))
         (= alignment-baseline "hanging")
         (let [bb (get-bounding-box font text 0 0 font-size)]
           (recur (-> config
                      (assoc :dy (double (+ dy (Math/abs (:y1 bb)))))
                      (dissoc :alignment-baseline)) text))
         :else (text-inner {:x            (+ x dx)
                            :y            (+ y dy)
                            :font-name    font
                            :font-size    font-size
                            :border-tight border-tight}
                           text)))
  ([{txt :text
     :as config}]
   (text (dissoc config :text) txt)))

(defn translate [x y]
  (str "translate(" (double x) "," (double y) ")"))

(defn- stack-downwards-text [y-offset {path :path idx :idx}]
  [:g {:transform (translate (- (:x1 (meta path))) (reduce + (take idx y-offset)))} path])

(defn stack-downwards-texts [{margin-top    :margin-top
                              margin-left   :margin-left
                              margin-bottom :margin-bottom
                              :or           {margin-left   0
                                             margin-top    0
                                             margin-bottom 0}}
                             txts]
  (let [with-baseline (map #(assoc % :alignment-baseline "hanging") txts)
        with-idx (map-indexed (fn [idx x] (assoc x :idx idx)) with-baseline)
        with-path (map (fn [x] (assoc x :path (text x))) with-idx)
        paths (map :path with-path)
        metas (map meta paths)
        y-offset (map #(let [h-with-margin (+ 4 (:height %))]
                         (Math/min h-with-margin (+ 0 (:font-size %)))) metas)
        height (reduce + y-offset)]
    (with-meta
      [:g {:transform (translate margin-left margin-top)}
       [:g (map (partial stack-downwards-text y-offset) with-path)]]
      {:height (+ margin-top height margin-bottom)})))
