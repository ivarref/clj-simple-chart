(ns clj-simple-chart.eurostat.avia-par-no
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clj-simple-chart.dateutils :as dateutils]
            [clj-simple-chart.eurostat.icao-airport-code :as airport-codes]
            [clojure.string :as str])
  (:import (org.apache.commons.compress.archivers ArchiveStreamFactory)
           (java.io ByteArrayInputStream ByteArrayOutputStream)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)
           (org.apache.commons.io IOUtils)
           (java.nio.charset StandardCharsets)))

(def url "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/AppLinkServices?lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2Favia_par_no.tsv.gz")

(defonce response (client/get url {:as :byte-array}))
(test/is (= 200 (:status response)))

(defonce body (:body response))

(defonce tsv (let [input (GzipCompressorInputStream. (ByteArrayInputStream. (:body response)))
                   output (ByteArrayOutputStream.)]
               (IOUtils/copy input output)
               (let [bytearray (.toByteArray output)
                     tsv-str (String. bytearray StandardCharsets/UTF_8)]
                 (spit "data/eurostat/avia-par-no.tsv" tsv-str)
                 (csvmap/tsv-map tsv-str))))

(def first-column-str "unit,tra_meas,airp_pr\\time")

(def first-column (keyword first-column-str))

(test/is (= first-column-str (name (first (:columns tsv)))))

(defn process-row [row]
  (reduce (fn [o [idx v]]
            (assoc o (keyword v)
                     (nth (str/split (get row first-column) #",") idx)))
          (dissoc row first-column)
          (map-indexed (fn [idx x] [idx x]) (str/split first-column-str #","))))

(defn remove-whitespace [row]
  (reduce (fn [o [k v]]
            (assoc o (keyword (str/trim (name k))) (str/trim v)))
          {}
          row))

(def regular-columns [:unit :tra_meas :airp_pr :from :to])

(defn number-or-nil-for-num-column [k v]
  (if (some #{k} regular-columns)
    v
    (try (if (number? (read-string v))
           (read-string v)
           nil)
         (catch Exception e nil))))

(defn from-code [x]
  (str/join "_" (take 2 (str/split (:airp_pr x) #"_"))))

(defn to-code [x]
  (str/join "_" (take-last 2 (str/split (:airp_pr x) #"_"))))

(defn condense-row-yearly [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    (= 4 (count (name k))))
              (assoc o k (number-or-nil-for-num-column k v))
              o))
          {}
          row))

(defn add-readable-from-to [row]
  (assoc row
    :from (get airport-codes/codes (from-code row) (from-code row))
    :to (get airport-codes/codes (to-code row) (to-code row))))

(defn condense-row-monthly [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    #_(= 4 (count (name k)))
                    (str/includes? (name k) "M"))
              (assoc o k (number-or-nil-for-num-column k v))
              o))
          {}
          row))

(defn explode-row [row]
  (reduce (fn [o [k v]]
            (if (some #{k} regular-columns)
              o
              (conj o
                    (merge (into {} (mapv (fn [k] [k (get row k)]) regular-columns))
                           {:date  (str/replace (name k) "M" "-")
                            :value v}))))
          []
          row))

(def data (->> (:data tsv)
               (map process-row)
               (map remove-whitespace)
               (map #(assoc % :airp_pr (get % (keyword "airp_pr\\time"))))
               (map #(dissoc % (keyword "airp_pr\\time")))
               (map condense-row-yearly)
               (filter #(and (= "PAS" (:unit %)) (= "PAS_CRD" (:tra_meas %))))
               (map add-readable-from-to)
               (vec)))

(def data-monthly (->> (:data tsv)
                       (map process-row)
                       (map remove-whitespace)
                       (map #(assoc % :airp_pr (get % (keyword "airp_pr\\time"))))
                       (map #(dissoc % (keyword "airp_pr\\time")))
                       (map condense-row-monthly)
                       (filter #(and (= "PAS" (:unit %)) (= "PAS_CRD" (:tra_meas %))))
                       (map add-readable-from-to)
                       (map explode-row)
                       (flatten)
                       (filter :value)
                       (sort-by :date)
                       (vec)))

(csvmap/write-csv "data/eurostat/avia-par-no-pas-carried.csv"
                  {:columns (reverse (sort (keys (first data))))
                   :data    (reverse (sort-by #(:2016 %) data))})

#_(csvmap/write-csv "data/eurostat/avia-par-no-pas-carried-monthly.csv"
                    {:columns (reverse (sort (keys (first data-monthly))))
                     :data    data-monthly})

#_(csvmap/write-csv "data/eurostat/avia-par-no-pas-carried-monthly-oslo-trondheim.csv"
                    {:columns (reverse (sort (keys (first data-monthly))))
                     :data    oslo-trondheim})
