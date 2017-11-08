(ns clj-simple-chart.eurostat.avia-paoc
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csvmap])
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

(defonce side-effect (IOUtils/copy input output)) ; such code.
(defonce tsv-bytearray (.toByteArray output))
(spit "data/eurostat/avia-paoc.tsv" (String. tsv-bytearray StandardCharsets/UTF_8))
(defonce tsv (csvmap/tsv-map (String. tsv-bytearray StandardCharsets/UTF_8)))

;(def unpacked (.createArchiveInputStream (ArchiveStreamFactory.) (ByteArrayInputStream. (:body response))))
