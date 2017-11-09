(ns clj-simple-chart.eurostat.avia-paoc
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

(defn months-only [row]
  (reduce (fn [o [k v]]
            (if (or (some #{k} regular-columns)
                    (str/includes? (name k) "M"))
              (assoc o k v)
              o))
          {}
          row))

(defn to-mill [row]
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

(def monthly-data (->> (:data tsv)
                       (mapv process-row)
                       (mapv remove-whitespace)
                       (mapv #(assoc % :schedule-time (get % (keyword "schedule\\time"))))
                       (mapv #(dissoc % (keyword "schedule\\time")))
                       (mapv months-only)
                       (filter #(and (= "PAS" (:unit %))
                                     (= "PAS_CRD" (:tra_meas %))
                                     (= "TOTAL" (:tra_cov %))
                                     (= "TOT" (:schedule-time %))))))

(def norway (->> data
                 (filter #(= "NO" (:geo %)))
                 (filter #(and (= "PAS" (:unit %))
                               (= "PAS_CRD" (:tra_meas %))
                               (= "TOTAL" (:tra_cov %))
                               (= "TOT" (:schedule-time %))))))

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

(def norway-monthly (->> monthly-data
                         (filter #(= "NO" (:geo %)))
                         (mapcat explode-row)
                         (remove #(= ":" (:value %)))
                         (mapv #(update % :value read-string))
                         (sort-by :date)
                         (map-indexed (fn [idx x] (assoc x :idx idx)))
                         (vec)))

(def norway-monthly-12mms (->> norway-monthly
                               (mapv #(assoc % :12mms
                                               (apply + (mapv :value (take-last 12 (take (inc (:idx %)) norway-monthly))))))
                               (mapv #(assoc % :12mms-mill (/ (:12mms %) 1000000)))
                               (remove #(< (:idx %) (dec 12)))
                               (map-indexed (fn [idx x] (assoc x :idx idx)))))

(def norway-monthly-12mms-yoy (->> norway-monthly-12mms
                                   (mapv #(assoc % :prev (first (take-last 12 (take (inc (:idx %)) norway-monthly-12mms)))))
                                   (remove #(< (:idx %) (dec 12)))
                                   (mapv #(assoc % :yoy (double (* 100 (/ (:12mms %) (:12mms (:prev %)))))))
                                   (mapv #(update % :yoy (fn [yoy] (- yoy 100))))))

(def eu28-monthly (->> monthly-data
                       (filter #(= "EU28" (:geo %)))
                       (mapcat explode-row)
                       (remove #(= ":" (:value %)))
                       (mapv #(update % :value read-string))
                       (sort-by :date)
                       (map-indexed (fn [idx x] (assoc x :idx idx)))
                       (vec)))

(def eu28-monthly-12mms (->> eu28-monthly
                             (mapv #(assoc % :12mms
                                             (apply + (mapv :value (take-last 12 (take (inc (:idx %)) eu28-monthly))))))
                             (mapv #(assoc % :12mms-mill (/ (:12mms %) 1000000)))
                             (remove #(< (:idx %) (dec 12)))))

(test/is (= (count (dateutils/date-range
                     (:date (first norway-monthly))
                     (:date (last norway-monthly))))
            (count norway-monthly)))

(def geo-distinct (sort (distinct (mapv :geo data))))

(csvmap/write-csv "data/eurostat/avia-paoc-yearly.csv"
                  {:columns (reverse (sort (keys (first data))))
                   :data    data})

(csvmap/write-csv "data/eurostat/NO-avia-paoc-yearly.csv"
                  {:columns (reverse (sort (keys (first data))))
                   :data    (filter #(= "NO" (:geo %)) data)})

(csvmap/write-csv "data/eurostat/avia-paoc-yearly-pas-carried.csv"
                  {:columns (reverse (sort (keys (first data))))
                   :data    (filter #(and (= "PAS" (:unit %))
                                          (= "PAS_CRD" (:tra_meas %))
                                          (= "TOTAL" (:tra_cov %))
                                          (= "TOT" (:schedule-time %))) data)})

(csvmap/write-csv "data/eurostat/avia-paoc-monthly-pas-carried.csv"
                  {:columns (reverse (sort (keys (first monthly-data))))
                   :data    monthly-data})

(csvmap/write-csv-format "data/eurostat/avia-paoc-norway-monthly-pas-carried.csv"
                         {:columns [:date :value :12mms-mill]
                          :data    norway-monthly-12mms
                          :format  {:12mms-mill "%.1f"}})

(csvmap/write-csv-format "data/eurostat/avia-paoc-norway-monthly-yoy-eoy-pas-carried.csv"
                         {:columns [:date :value :12mms-mill :yoy]
                          :data    (filter #(or (str/ends-with? (:date %) "-12")
                                                (= % (last norway-monthly-12mms-yoy))) norway-monthly-12mms-yoy)
                          :format  {:12mms-mill "%.1f"
                                    :yoy        "%.1f"}})

(csvmap/write-csv-format "data/eurostat/avia-paoc-norway-monthly-yoy-pas-carried.csv"
                         {:columns [:date :value :12mms-mill :yoy]
                          :data    norway-monthly-12mms-yoy
                          :format  {:12mms-mill "%.1f"
                                    :yoy        "%.1f"}})

(csvmap/write-csv-format "data/eurostat/avia-paoc-eu28-monthly-pas-carried.csv"
                         {:columns [:date :value :12mms-mill]
                          :data    eu28-monthly-12mms
                          :format  {:12mms-mill "%.1f"}})