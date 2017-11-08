(ns clj-simple-chart.eurostat.avia-paoc
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.string :as str])
  (:import (org.apache.commons.compress.archivers ArchiveStreamFactory)
           (java.io ByteArrayInputStream ByteArrayOutputStream)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)
           (org.apache.commons.io IOUtils)
           (java.nio.charset StandardCharsets)))

; "/eurostat/estat-navtree-portlet-prod/AppLinkServices?lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2Favia_paoc.tsv.gz"
(def url "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/AppLinkServices?lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2Favia_paoc.tsv.gz")

(defonce response (client/get url {:as :byte-array}))
(test/is (= 200 (:status response)))

(defonce body (:body response))

(defonce output (ByteArrayOutputStream.))
(defonce input (GzipCompressorInputStream. (ByteArrayInputStream. (:body response))))

(defonce side-effect (IOUtils/copy input output))           ; such code.
(defonce tsv-bytearray (.toByteArray output))
(spit "data/eurostat/avia-paoc.tsv" (String. tsv-bytearray StandardCharsets/UTF_8))
(defonce tsv (csvmap/tsv-map (String. tsv-bytearray StandardCharsets/UTF_8)))

(def first-column-str "unit,tra_meas,geo,tra_cov,schedule\\time")
(def regular-columns [:unit :geo :schedule-time :tra_cov :tra_meas])
(def first-column (keyword first-column-str))
(test/is (= first-column (first (:columns tsv))))

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

(defn condense-row [row]
    (reduce (fn [o [k v]]
              (if (or (some #{k} regular-columns)
                      (= 4 (count (name k))))
                (assoc o k v)
                o))
            {}
            row))

(def data (->> (:data tsv)
               (mapv process-row)
               (mapv remove-whitespace)
               (mapv #(assoc % :schedule-time (get % (keyword "schedule\\time"))))
               (mapv #(dissoc % (keyword "schedule\\time")))
               (mapv condense-row)))

(csvmap/write-csv "data/eurostat/avia-paoc-yearly.csv"
                  {:columns (reverse (sort (keys (first data))))
                   :data data})