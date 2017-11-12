(ns clj-simple-chart.eurostat.avia-paexac
  (:require [clj-http.client :as client]
            [clojure.test :as test]
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

(defn columns []
  (with-open [reader (io/reader (io/file "data/eurostat/avia-paexac.tsv"))]
    (let [c (csv/read-csv reader :separator \tab)
          columns (first c)]
      (mapv keyword columns))))

(def first-column-str "unit,tra_meas,partner,rep_airp\\time")
(def first-column (keyword first-column-str))

(test/is (= (name (first (columns))) first-column-str))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword) ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

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

(defn rows []
  (->> (csv-data->maps (csv/read-csv (io/reader (io/file "data/eurostat/avia-paexac.tsv")) :separator \tab))
       (map process-row)
       (map remove-whitespace)))

(defn distinct-values [row]
  (->> (rows) (map row) (distinct) (sort) (vec)))

;(test/is (= (distinct-values :unit) ["FLIGHT" "PAS"]))
;(test/is (= (distinct-values :tra_meas) ["CAF_PAS" "CAF_PAS_ARR" "CAF_PAS_DEP" "PAS_BRD" "PAS_BRD_ARR" "PAS_BRD_DEP" "PAS_CRD" "PAS_CRD_ARR" "PAS_CRD_DEP"]))
;(test/is (= (distinct-values :partner) ["" ... "US" ... "]))

;(csv/read-csv (io/reader  :separator \tab)

;(defonce response (let [r (time (client/get url {:as :byte-array}))]
;                    (println "done downloading!")
;                    r))
;
;(defonce tsv (let [input (BufferedInputStream. (GzipCompressorInputStream. (ByteArrayInputStream. (:body response))))
;                   output (ByteArrayOutputStream.)]
;               (println "start copy input ...")
;               (IOUtils/copy input output)
;               (let [bytearray (.toByteArray output)
;                     tsv-str (String. bytearray StandardCharsets/UTF_8)]
;                 (println "done making string ...")
;                 (spit "data/eurostat/avia-paexac.tsv" tsv-str)
;                 (println "done spitting ...")
;                 (csvmap/tsv-map tsv-str))))
