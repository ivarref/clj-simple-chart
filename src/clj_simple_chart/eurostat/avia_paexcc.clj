(ns clj-simple-chart.eurostat.avia-paexcc
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

(def path "/eurostat/estat-navtree-portlet-prod/AppLinkServices?lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2Favia_paexcc.tsv.gz")
(def url (str "http://ec.europa.eu" path))

(defonce response (client/get url {:as :byte-array}))
(test/is (= 200 (:status response)))

(defonce body (:body response))

(defonce tsv (let [input (GzipCompressorInputStream. (ByteArrayInputStream. (:body response)))
                   output (ByteArrayOutputStream.)]
               (IOUtils/copy input output)
               (let [bytearray (.toByteArray output)
                     tsv-str (String. bytearray StandardCharsets/UTF_8)]
                 (spit "data/eurostat/avia-paexcc.tsv" tsv-str)
                 (csvmap/tsv-map tsv-str))))

(def first-column-str "unit,tra_meas,partner,geo\\time")
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

(defonce data (->> (:data tsv)
                   (map process-row)
                   (map remove-whitespace)
                   (map #(assoc % :geo (get % (keyword "geo\\time"))))
                   (map #(dissoc % (keyword "geo\\time")))
                   (vec)))

(test/is (= (vec (sort (distinct (map :geo data))))
            ["AT" "BE" "BG" "CY" "CZ" "DE" "DK" "EE" "EL" "ES" "EU27" "EU28" "FI" "FR" "HR" "HU" "IE" "IT" "LT" "LU" "LV" "MT" "NL" "PL" "PT" "RO" "SE" "SI" "SK" "UK"]
            ))