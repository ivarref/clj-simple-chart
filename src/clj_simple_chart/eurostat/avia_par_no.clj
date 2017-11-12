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


