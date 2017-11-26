(ns clj-simple-chart.eurostat.avia-par-all
  (:require [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [clj-simple-chart.eurostat.icao-airport-code :as airport-codes])
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

(defn add-readable-from-to [row]
  (assoc row
    :from (get airport-codes/codes (from-code row) (from-code row))
    :to (get airport-codes/codes (to-code row) (to-code row))))

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
           (filter #(= "New York" (:to %)))))))

(defonce all (mapcat tsv-map ["be" "bg" "cz" "dk" "de" "ee"
                              "ie" "el" "es" "fr" "hr" "it"
                              "cy" "lv" "lt" "lu" "hu" "mt"
                              "nl" "at" "pl" "pt" "ro" "si"
                              "sk" "fi" "se" "uk" "is" "no"
                              "ch" "me" "mk" "tr"]))

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

(def data (->> all
               (map condense-row-yearly)
               (vec)))

(csvmap/write-csv "data/eurostat/avia-par-NY-pas-carried.csv"
                  {:columns (vec (distinct (concat regular-columns (reverse (sort (keys (first data)))))))
                   :data    (reverse (sort-by #(:2016 %) data))})
