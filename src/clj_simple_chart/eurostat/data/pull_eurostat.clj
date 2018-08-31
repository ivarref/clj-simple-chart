(ns clj-simple-chart.eurostat.data.pull-eurostat
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.string :as str])
  (:import (org.apache.commons.compress.compressors.gzip GzipCompressorInputStream)
           (java.io ByteArrayOutputStream ByteArrayInputStream)
           (org.apache.commons.io IOUtils)
           (java.nio.charset StandardCharsets)))

(defn- proper-keyword [kv]
  (str/replace kv "\\time" ""))

(defn- untangle-row [columns row]
  (let [first-column (first columns)]
    (reduce (fn [o [idx v]]
              (assoc o (keyword (proper-keyword v))
                       (nth (str/split (get row first-column) #",") idx)))
            (dissoc row first-column)
            (map-indexed (fn [idx x] [idx x]) (str/split (name first-column) #",")))))

(defn pull [table-name]
  (let [url (str "https://ec.europa.eu/eurostat/estat-navtree-portlet-prod"
                 "/AppLinkServices?lang=en&appId=bulkdownload"
                 "&appUrl=http%3A%2F%2Fec.europa.eu%2Feurostat%2Festat-navtree-portlet-prod%2FBulkDownloadListing%3Ffile%3Ddata%2F"
                 table-name
                 ".tsv.gz")
        resp (client/get url {:as :byte-array})]
    (let [input (GzipCompressorInputStream. (ByteArrayInputStream. (:body resp)))
          output (ByteArrayOutputStream.)]
      (IOUtils/copy input output)
      (let [bytearray (.toByteArray output)
            tsv-str (String. bytearray StandardCharsets/UTF_8)]
        (spit (str "data/eurostat/" table-name ".tsv") tsv-str)
        (let [result (csvmap/tsv-map tsv-str)]
          (mapv (partial untangle-row (:columns result)) (:data result)))))))
