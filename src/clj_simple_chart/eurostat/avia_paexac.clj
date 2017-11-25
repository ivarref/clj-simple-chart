; International extra-EU air passenger transport by main airports
; in each reporting country and partner world regions and countries (avia_paexac)
(ns clj-simple-chart.eurostat.avia-paexac
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.eurostat.avia-par-no :as utils]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clj-simple-chart.dateutils :as dateutils]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import (org.apache.commons.compress.archivers ArchiveStreamFactory)
           (java.io ByteArrayInputStream ByteArrayOutputStream BufferedInputStream FileInputStream)
           (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)
           (org.apache.commons.io IOUtils)
           (java.nio.charset StandardCharsets)))

(def url "http://ec.europa.eu/eurostat/estat-navtree-portlet-prod/AppLinkServices?lang=en&appId=bulkdownload&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2Favia_paexac.tsv.gz")

(defonce response (time (client/get url {:as :byte-array})))
(test/is (= 200 (:status response)))

(defonce body (:body response))

(defonce tsv-string (time (let [input (GzipCompressorInputStream. (ByteArrayInputStream. (:body response)))
                                output (ByteArrayOutputStream.)]
                               (IOUtils/copy input output)
                               (let [bytearray (.toByteArray output)
                                     tsv-str (String. bytearray StandardCharsets/UTF_8)]
                                 tsv-str))))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(def first-column-str "unit,tra_meas,partner,rep_airp\\time")
(def first-column (keyword first-column-str))

(defn process-row [row]
  (reduce (fn [o [idx v]]
            (assoc o (keyword v)
                     (nth (str/split (get row first-column) #",") idx)))
          (dissoc row first-column)
          (map-indexed (fn [idx x] [idx x]) (str/split first-column-str #","))))

(def regular-columns [:unit :tra_meas :partner :rep_airp :from :to :codes])

(defn rows []
  (->> (csv-data->maps (csv/read-csv tsv-string :separator \tab))
       (map process-row)
       (map utils/remove-whitespace)
       (filter #(and (= "PAS" (:unit %)) (= "PAS_CRD" (:tra_meas %))))))

(defonce data (vec (rows)))