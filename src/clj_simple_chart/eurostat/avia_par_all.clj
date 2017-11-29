(ns clj-simple-chart.eurostat.avia-par-all
  (:require [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [clj-simple-chart.eurostat.icao-airport-code :as airport-codes]
            [clj-simple-chart.dateutils :as dateutils])
  (:import (java.nio.charset StandardCharsets)
           (org.apache.commons.io IOUtils)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data)                                ;; First row is the header
            (map keyword)                                   ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn get-url [url]
  (client/get url {:as :byte-array}))

(def get-memo (memoize get-url))

(def first-column-str "unit,tra_meas,airp_pr\\time")
(def first-column (keyword first-column-str))

(defn process-row [row]
  (if (get row first-column)
    (reduce (fn [o [idx v]]
              (assoc o (keyword v)
                       (nth (str/split (get row first-column) #",") idx)))
            (dissoc row first-column)
            (map-indexed (fn [idx x] [idx x]) (str/split first-column-str #",")))
    nil))

(defn remove-whitespace [row]
  (reduce (fn [o [k v]]
            (assoc o (keyword (str/trim (name k))) (str/trim v)))
          {}
          row))

(defn from-code [x]
  (str/join "_" (take 2 (str/split (:airp_pr x) #"_"))))

(defn to-code [x]
  (str/join "_" (take-last 2 (str/split (:airp_pr x) #"_"))))

(defn to-cc [x]
  (str/lower-case (nth (str/split (:airp_pr x) #"_") 2)))

(defn add-readable-from-to [row]
  (assoc row
    :from (get airport-codes/codes (from-code row) (from-code row))
    :to (get airport-codes/codes (to-code row) (to-code row))))

(def euro-cc ["be" "bg" "cz" "dk" "de" "ee"
              "ie" "el" "es" "fr" "hr" "it"
              "cy" "lv" "lt" "lu" "hu" "mt"
              "nl" "at" "pl" "pt" "ro" "si"
              "sk" "fi" "se" "uk" "is" "no"
              "ch" "me" "mk" "tr"])

(defn tsv-map [cc]
  (println "doing cc >" cc "<")
  (let [url (str "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/AppLinkServices?"
                 "lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2"
                 "Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2F"
                 "avia_par_" cc ".tsv.gz")
        response (get-memo url)
        input (GzipCompressorInputStream. (ByteArrayInputStream. (:body response)))
        output (ByteArrayOutputStream.)]
    (IOUtils/copy input output)
    (let [bytearray (.toByteArray output)
          tsv-str (String. bytearray StandardCharsets/UTF_8)]
      (->> (csv-data->maps (csv/read-csv tsv-str :separator \tab))
           (map process-row)
           (map remove-whitespace)
           (map #(assoc % :airp_pr (get % (keyword "airp_pr\\time"))))
           (map #(dissoc % (keyword "airp_pr\\time")))
           (filter #(and (= "PAS" (:unit %)) (= "PAS_CRD" (:tra_meas %))))
           (map add-readable-from-to)
           (map #(assoc % :to (get {"Newark" "New York"
                                    "Moscow" "Moskva"} (:to %) (:to %))))
           (map #(assoc % :to_cc (to-cc %)))
           (remove #(some #{(:to_cc %)} euro-cc))
           #_(filter #(= "New York" (:to %)))))))

(defonce all (mapcat tsv-map euro-cc))

(def regular-columns [:unit :tra_meas :airp_pr :from :to :codes])

(defn number-or-nil-for-num-column [k v]
  (if (some #{k} regular-columns)
    v
    (try (if (number? (read-string v))
           (read-string v)
           nil)
         (catch Exception e nil))))

(defn condense-row-yearly [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    (= 4 (count (name k))))
              (assoc o k (number-or-nil-for-num-column k v))
              o))
          {}
          row))

(defn condense-row-monthly [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    #_(= 4 (count (name k)))
                    (str/includes? (name k) "M"))
              (assoc o k (number-or-nil-for-num-column k v))
              o))
          {}
          row))

(defn add-item [a b]
  (reduce (fn [o [k v]]
            (cond (number? v) (update o k #(+ (or % 0) v))
                  (nil? v) (update o k #(or % nil))
                  :else (assoc o k v))) a b))

(defn condense-group [items]
  (if (= 1 (count items))
    (assoc (first items)
      :codes [(:airp_pr (first items))])
    (assoc (reduce add-item (first items) (rest items))
      :codes (vec (sort (distinct (map :airp_pr items)))))))

(def data (->> all
               (map condense-row-yearly)
               (vec)))

(def data-grouped (->> data
                       (group-by (juxt :to))
                       (vals)
                       (mapv condense-group)
                       (flatten)
                       (vec)))

(defn explode-row [row]
  (reduce (fn [o [k v]]
            (if (some #{k} regular-columns)
              o
              (conj o
                    (merge (into {} (mapv (fn [k] [k (get row k)]) regular-columns))
                           {:date     (str/replace (name k) "M" "-")
                            :date-int (read-string (str/replace (name k) "M" ""))
                            :value    v}))))
          []
          row))

(def top-ten-dests (->> data-grouped
                        (sort-by #(:2016 %))
                        (reverse)
                        (take 10)
                        (map :to)))

(defn mma [dat]
  (->> dat
       (map #(assoc % :prev-rows (take-last 12 (take (inc (:idx %)) dat))))
       (map #(assoc % :value (/ (apply + (map :value (:prev-rows %)))
                                (* 1000 (dateutils/prev-12-months-num-days (:date %))))))
       (filter #(= 12 (count (:prev-rows %))))))

(defn do-mma [data]
  (->> data
       (sort-by :date)
       (map-indexed (fn [idx x] (assoc x :idx idx)))
       (mma)))

(def data-monthly (->> all
                       (map condense-row-monthly)
                       (group-by (juxt :to))
                       (vals)
                       (mapv condense-group)
                       (flatten)
                       (map explode-row)
                       (flatten)
                       (filter :value)
                       (filter #(number? (:value %)))
                       (filter #(some #{(:to %)} top-ten-dests))
                       (group-by :to)
                       (vals)
                       (map do-mma)
                       (flatten)
                       (sort-by :date)
                       (filter #(>= (:date-int %) 200312))
                       (filter #(< (:date-int %) 201701))
                       (vec)))

(def max-date (->> data-monthly
                   (map :date-int)
                   (apply max)))

(csvmap/write-csv "data/eurostat/avia-par-ex-eu-pas-carried.csv"
                  {:columns (vec (distinct (concat regular-columns (reverse (sort (keys (first data-grouped)))))))
                   :data    (reverse (sort-by #(:2016 %) data-grouped))})
