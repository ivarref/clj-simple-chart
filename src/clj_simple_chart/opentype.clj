(ns clj-simple-chart.opentype
  (:require [base64-clj.core :as base64]
            [clojure.core.async :as async]
            [clojure.string :as string]
            [clj-simple-chart.translate :refer [translate]]
            [clojure.string :as str])
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

(defn- text-inner [{font-name    :font-name
                    font-size    :font-size
                    x            :x
                    y            :y
                    fill         :fill
                    stroke       :stroke
                    rect         :rect
                    circle       :circle
                    path         :path
                    min-height   :min-height
                    border-tight :border-tight}
                   text]
  (let [bb (get-bounding-box font-name text x y font-size)
        metadata {:font-size (double font-size)
                  :height    (or (if (or rect circle) min-height nil) (Math/abs (- (:y1 bb) (:y2 bb))))
                  :text      text
                  :width     (Math/abs (- (:x1 bb) (:x2 bb)))}
        metadata (merge metadata bb)
        font-path [:path {:d      (get-path-data font-name text x y font-size)
                          :fill   fill
                          :stroke stroke}]
        txt (with-meta {} metadata)
        border (when border-tight
                 [:g [:line {:x1     (:x1 (meta txt))
                             :x2     (:x1 (meta txt))
                             :y1     (:y1 (meta txt))
                             :y2     (:y2 (meta txt))
                             :stroke border-tight}]

                  [:line {:x1     (:x2 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y1 (meta txt))
                          :y2     (:y2 (meta txt))
                          :stroke border-tight}]

                  [:line {:x1     (:x1 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y1 (meta txt))
                          :y2     (:y1 (meta txt))
                          :stroke border-tight}]

                  [:line {:x1     (:x1 (meta txt))
                          :x2     (:x2 (meta txt))
                          :y1     (:y2 (meta txt))
                          :y2     (:y2 (meta txt))
                          :stroke border-tight}]])
        rect-size (or (:size rect) (Math/ceil (* 0.8 font-size)))
        circle-r (or (:r circle) (Math/ceil (* 0.35 font-size)))
        rectangle (when rect
                    [:rect {:x            (+ #_0.5 (:x1 bb))
                            :y            0 #_-0.5
                            :height       rect-size
                            :width        rect-size
                            :fill         (or (:fill rect) "red")
                            :stroke       (or (:stroke rect) "black")
                            :stroke-width (or (:stroke-width rect) "1px")
                            :fill-opacity (or (:fill-opacity rect) 1.0)}])
        circle-elem (when circle
                      [:g
                       [:path {:d            (str "M 0," (dec (Math/floor (* 0.5 (or min-height 0.0))))
                                                  " H " font-size)
                               :stroke       (or (:stroke path) "black")
                               :stroke-width (or (:stroke-width path) 3.5)}]
                       [:circle {:r            circle-r
                                 :cx           (Math/floor (* 0.5 (or font-size 0.0)))
                                 :cy           (dec (Math/floor (* 0.5 (or min-height 0.0))))
                                 :stroke-width (or (:stroke-width circle) 1)
                                 :stroke       (or (:stroke circle) "black")
                                 :fill         (or (:fill circle) "red")}]])
        text-margin (or (:margin rect) (Math/ceil (* 0.2 font-size)))
        text-offset (+ rect-size text-margin)
        text-offset (if (or rect circle) text-offset 0)
        path [:g rectangle circle-elem [:g {:transform (translate text-offset 0)} font-path border]]]
    (with-meta path (update metadata :width #(+ % text-offset)))))

(defn text
  ([{font               :font
     font-size          :font-size
     x                  :x
     y                  :y
     dx                 :dx
     dy                 :dy
     text-anchor        :text-anchor
     border-tight       :border-tight
     min-height         :min-height
     alignment-baseline :alignment-baseline
     fill               :fill
     stroke             :stroke
     spacing            :spacing
     rect               :rect
     circle             :circle
     path               :path
     :as                config
     :or                {x                  0.0
                         y                  0.0
                         dx                 0.0
                         dy                 0.0
                         rect               nil
                         circle             nil
                         fill               "#000"
                         text-anchor        :none
                         font-size          14
                         stroke             "none"
                         alignment-baseline "auto"
                         font               "Roboto Regular"
                         spacing            ::none
                         border-tight       false
                         min-height         nil}}
    text]
   {:pre [(some #{font} (keys font-name-to-font))
          (number? font-size)
          (number? x)
          (number? y)]}
   (cond (and (string? dx) (.endsWith dx "em"))
         (recur (update config :dx (partial em-to-number font-size)) text)
         (and (string? dy) (.endsWith dy "em"))
         (recur (update config :dy (partial em-to-number font-size)) text)
         (not= ::none spacing) (with-meta [:g] {:font-size spacing
                                                :x1        0.0
                                                :x2        0.0
                                                :height    spacing :width 0})
         (not (string? text))
         (recur config (str text))

         ;; start text anchor
         (= text-anchor "start")
         (let [bb (get-bounding-box font text 0 0 font-size)
               w (- (:x2 bb) (:x1 bb))]
           (recur (-> config
                      (assoc :dx (double (- dx (:x1 bb))))
                      (dissoc :text-anchor)) text))

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
                            :fill         fill
                            :rect         rect
                            :circle       circle
                            :path         path
                            :min-height   min-height
                            :stroke       stroke
                            :border-tight border-tight}
                           text)))
  ([{txt :text
     :as config}]
   (text (dissoc config :text) txt)))

(defn- stack-downwards-text [y-offset {path :path idx :idx}]
  [:g {:transform (translate (- (:x1 (meta path))) (reduce + (take idx y-offset)))} path])

(defn text-stack-downwards [{margin-top    :margin-top
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
        y-offset (map-indexed (fn [idx yoff] (+ yoff (get (nth txts idx) :margin-bottom 0))) y-offset)
        height (reduce + y-offset)]
    (with-meta
      [:g {:transform (translate margin-left margin-top)}
       [:g (map (partial stack-downwards-text y-offset) with-path)]]
      {:height (+ margin-top height margin-bottom)})))

(defn- stack-text [alignment max-width y-offset {path :path reverse-start :reverse-start idx :idx}]
  (cond
    (= alignment :left) [:g {:transform (translate (- (:x1 (meta path))) (reduce + (take idx y-offset)))} path]
    (= alignment :right) [:g {:transform (translate
                                           (if (nil? reverse-start)
                                             (-  (:x2 (meta path)))
                                             (Math/floor (- (or (:x2 (meta reverse-start)) 0)
                                                            max-width
                                                            (:x2 (meta path)))))
                                           (reduce + (take idx y-offset)))} path]
    :else (throw (Exception. (str "Unknown alignment >" alignment "<")))))

(defn- text-stack
  [alignment txts]
  (let [texts-with-spacing (vec (flatten (mapv
                                           (fn [v]
                                             (cond
                                               (number? (:margin-top v)) [{:spacing (:margin-top v)} (dissoc v :margin-top)]
                                               (number? (:margin-bottom v)) [(dissoc v :margin-bottom) {:spacing (:margin-bottom v)}]
                                               :else [v])) txts)))
        with-baseline (map #(assoc % :alignment-baseline "hanging") texts-with-spacing)
        max-height-for-rects (->> with-baseline
                                  (filter #(or (:rect %) (:circle %)))
                                  (mapv text)
                                  (mapv meta)
                                  (mapv :height)
                                  (reduce max 0)
                                  (Math/ceil))
        with-baseline (mapv #(assoc % :min-height max-height-for-rects) with-baseline)
        with-idx (map-indexed (fn [idx x] (assoc x :idx idx)) with-baseline)
        with-path (->> with-idx
                       (map #(assoc % :path (text %)))
                       (map #(assoc % :no-baseline (text (dissoc % :alignment-baseline)))))
        with-right (->> with-path
                        (map (fn [x] (update x :right #(or % {:text ""}))))
                        (map (fn [x] (update x :right #(merge (dissoc x :right) %))))
                        (mapv #(assoc % :right-path (text (:right %)))))
        right-text (->> with-path
                        (filter :right)
                        (map #(merge (dissoc % :rect) (:right %)))
                        (map #(dissoc % :right))
                        (map #(dissoc % :alignment-baseline))
                        (map #(assoc % :reverse-start (text (update % :text (fn [x] (str (first (reverse x))))))))
                        (map #(assoc % :path (text (assoc % :dy (let [{y1 :y1 y2 :y2} (-> % :no-baseline (meta))]
                                                                  (Math/abs y1)))))))
        paths (map :path with-path)
        metas (map meta paths)
        max-width-left (apply max (map :width metas))
        total-width (->> with-right
                         (map #(+ (-> % :path (meta) :width)
                                  (let [right-w (-> % :right-path (meta) :width)]
                                    (if (pos? right-w)
                                      (+ right-w (* 0.4 (:font-size %)))
                                      0))))
                         (reduce max 0))
        max-width-right-char (reduce max 0 (remove nil? (map (comp :width meta :reverse-start) right-text)))
        y-offset (map #(let [h-with-margin (+ 4 (:height %))]
                         (Math/min h-with-margin (+ 0 (:font-size %)))) metas)
        y-offset (map-indexed (fn [idx yoff] (+ yoff (get (nth texts-with-spacing idx) :margin-bottom 0))) y-offset)
        height (Math/ceil (- (reduce + 0 y-offset) 0))]
    (with-meta
      [:g (map (partial stack-text alignment max-width-left y-offset) with-path)
       [:g {:transform (translate (Math/ceil total-width) 0)}
        (map (partial stack-text :right (Math/floor max-width-right-char) y-offset) right-text)]]
      {:height height :width total-width})))

(defn- add-translation [width height [alignment group]]
  (cond (= [:top :left] alignment) group
        (= [:top :right] alignment) [:g {:transform (translate width 0)} group]
        (= [:bottom :right] alignment) [:g {:transform (translate width (- height (:height (meta group))))} group]
        (= [:bottom :left] alignment) [:g {:transform (translate 0 (- height (:height (meta group))))} group]))

(defn- stack-inner [{width         :width
                     fill          :fill
                     fill-opacity  :fill-opacity
                     margin-left   :margin-left
                     margin-right  :margin-right
                     margin-bottom :margin-bottom
                     margin-top    :margin-top
                     stroke        :stroke
                     stroke-width  :stroke-width
                     :as           config
                     :or           {margin-left  0 margin-right 0 margin-top 0 margin-bottom 0
                                    fill         nil
                                    stroke       "black"
                                    stroke-width "1px"
                                    fill-opacity 1}} txts]
  (let [with-alignment (mapv #(assoc % :align (or (:align %) :left)) txts)
        with-alignment (mapv #(assoc % :valign (or (:valign %) :top)) with-alignment)
        grouped (group-by (fn [x] [(:valign x) (:align x)]) with-alignment)
        groups (map (fn [[k v]] [k (text-stack (second k) v)]) grouped)
        group-map (zipmap (mapv first groups) (mapv second groups))
        max-height (apply max (mapv (comp :height meta) (vals group-map)))
        max-width (apply max (mapv (comp :width meta) (vals group-map)))
        with-translation (map (partial add-translation width max-height) groups)
        width-w-margs (+ margin-left margin-right (or width max-width))
        height-w-margs (+ margin-top margin-bottom max-height)]
    (with-meta [:g {:transform (translate 0.5 0.5)}
                (when fill [:rect {:width        (Math/ceil width-w-margs)
                                   :height       (Math/ceil height-w-margs)
                                   :stroke       stroke
                                   :stroke-width stroke-width
                                   :fill-opacity fill-opacity
                                   :fill         fill}])
                [:g {:transform (translate margin-left margin-top)} with-translation]]
               {:height (Math/ceil height-w-margs)
                :width  (Math/ceil width-w-margs)})))

(defn stack [{width         :width
              fill          :fill
              fill-opacity  :fill-opacity
              margin        :margin
              margin-left   :margin-left
              margin-right  :margin-right
              margin-bottom :margin-bottom
              margin-top    :margin-top
              :as           config
              :or           {margin       nil margin-left nil margin-right nil margin-top nil margin-bottom nil
                             fill         nil
                             fill-opacity 1}} txts]
  (cond margin
        (recur (assoc config :margin nil
                             :margin-left (or margin-left margin)
                             :margin-right (or margin-right margin)
                             :margin-bottom (or margin-bottom margin)
                             :margin-top (or margin-top margin)) txts)
        :else (stack-inner config txts)))