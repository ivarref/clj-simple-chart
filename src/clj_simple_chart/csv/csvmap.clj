(ns clj-simple-chart.csv.csvmap
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn- debomify
  [^String line]
  (let [bom "\uFEFF"]
    (if (.startsWith line bom)
      (.substring line 1)
      line)))

(def sample-csv "hello,world\n123,999\n333,777")

(defn- produce-row
  [columns row]
  (zipmap columns row))

(defn csv-map [^String input]
  (let [csv-raw (csv/read-csv (debomify input))
        columns (mapv keyword (first csv-raw))
        data (filter #(= (count columns) (count %)) (rest csv-raw))]
    {:columns columns
     :data    (mapv #(produce-row columns %) data)}))

(defn csv-map-assert-columns [^String input expected-columns]
  (let [csv-raw (csv/read-csv (debomify input))
        columns (mapv keyword (first csv-raw))
        data (filter #(= (count columns) (count %)) (rest csv-raw))
        missing-columns (->> expected-columns
                             (mapv keyword)
                             (mapv #(if (some #{%} columns) nil %))
                             (remove nil?))]
    (when (pos? (count missing-columns))
      (println "missing columns: " (str missing-columns))
      (throw (Exception. (str "missing columns: " (str missing-columns)))))
    {:columns columns
     :data    (mapv #(produce-row columns %) data)}))

(defn write-csv
  [^String filename {columns :columns data :data}]
  {:pre [(coll? columns) (coll? data)]}
  (with-open [out-file (io/writer filename)]
    (let [column-names (mapv #(if (string? %) % (subs (str %) 1)) columns)]
      (csv/write-csv out-file [column-names])
      (csv/write-csv
        out-file
        (mapv
          (fn [row]
            (mapv (fn [key] (get row key)) columns))
          data)))))