(ns clj-simple-chart.eurostat.avia-par-no
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clj-simple-chart.dateutils :as dateutils]
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

(def regular-columns [:unit :tra_meas :airp_pr])

(defn condense-row [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    (= 4 (count (name k)))
                    #_(str/includes? (name k) "Q"))
              (assoc o k v)
              o))
          {}
          row))

(defn condense-row-monthly [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    #_(= 4 (count (name k)))
                    (str/includes? (name k) "M"))
              (assoc o k v)
              o))
          {}
          row))

(def data (->> (:data tsv)
               (map process-row)
               (map remove-whitespace)
               (map #(assoc % :airp_pr (get % (keyword "airp_pr\\time"))))
               (map #(dissoc % (keyword "airp_pr\\time")))
               (map condense-row)
               (vec)))

(def data-monthly (->> (:data tsv)
                       (map process-row)
                       (map remove-whitespace)
                       (map #(assoc % :airp_pr (get % (keyword "airp_pr\\time"))))
                       (map #(dissoc % (keyword "airp_pr\\time")))
                       (map condense-row-monthly)
                       (filter #(and (= "PAS" (:unit %))
                                     (= "PAS_CRD" (:tra_meas %))))
                       (vec)))

(def pas-carried (->> data
                      (filter #(and (= "PAS" (:unit %))
                                    (= "PAS_CRD" (:tra_meas %))))))

(csvmap/write-csv "data/eurostat/avia-par-no-pas-carried.csv"
                  {:columns (reverse (sort (keys (first pas-carried))))
                   :data    pas-carried})

(csvmap/write-csv "data/eurostat/avia-par-no-pas-carried-monthly.csv"
                  {:columns (reverse (sort (keys (first data-monthly))))
                   :data    data-monthly})
