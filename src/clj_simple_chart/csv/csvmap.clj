(ns clj-simple-chart.csv.csvmap
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn debomify
  [^String line]
  (let [bom "\uFEFF"]
    (if (.startsWith line bom)
      (.substring line 1)
      line)))

(defn- produce-row
  [columns row]
  (zipmap columns row))

(defn csv-map [^String input]
  (let [csv-raw (csv/read-csv (debomify input))
        columns (mapv keyword (first csv-raw))
        data (filter #(= (count columns) (count %)) (rest csv-raw))]
    {:columns columns
     :data    (mapv #(produce-row columns %) data)}))

(defn tsv-map [^String input]
  (let [csv-raw (csv/read-csv (debomify input) :separator \tab)
        columns (mapv keyword (first csv-raw))
        data (filter #(= (count columns) (count %)) (rest csv-raw))]
    {:columns columns
     :data    (mapv #(produce-row columns %) data)}))

(defn assert-columns [expected-columns {columns :columns
                                        data    :data
                                        :as     input}]
  (let [missing-columns (->> expected-columns
                             (mapv keyword)
                             (mapv #(if (some #{%} columns) nil %))
                             (remove nil?))]
    (when (pos? (count missing-columns))
      (println "got columns: " (string/join ", " columns))
      (println "missing columns: " (string/join ", " missing-columns))
      (throw (Exception. (str "missing columns: " (string/join ", " missing-columns)))))
    input))

(defn read-string-columns [columns data]
  (let [make-row (fn [x])]
    (mapv (fn [x]
            (reduce (fn [o [k v]]
                      (if (some #{k} columns)
                        (assoc o k (read-string v))
                        (assoc o k v))) {} x)) data)))

(defn number-or-nil-columns [columns data]
  (let [make-row (fn [x])]
    (mapv (fn [x]
            (reduce (fn [o [k v]]
                      (if (some #{k} columns)
                        (assoc o k (if (number? v) v nil))
                        (assoc o k v))) {} x)) data)))

(defn drop-columns [columns data]
  (mapv (fn [x]
          (reduce (fn [o [k v]]
                    (if (some #{k} columns)
                      o
                      (assoc o k v))) {} x)) data))

(defn keep-columns [columns data]
  (mapv (fn [x]
          (reduce (fn [o [k v]]
                    (if (not (some #{k} columns))
                      o
                      (assoc o k v))) {} x)) data))

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
      (throw (Exception. (str "missing columns: " (string/join ", " missing-columns)))))
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

(defn write-csv-format
  [^String filename {columns :columns
                     data :data
                     fmt :format}]
  {:pre [(coll? columns) (coll? data)]}
  (with-open [out-file (io/writer filename)]
    (let [column-names (mapv #(if (string? %) % (subs (str %) 1)) columns)
          format-fn (fn [col]
                      (if (= ::none (get fmt col ::none))
                        identity
                        (fn [v] (format (get fmt col) v))))]
      (csv/write-csv out-file [column-names])
      (csv/write-csv
        out-file
        (mapv
          (fn [row]
            (mapv (fn [key] ((format-fn key) (get row key))) columns))
          data)))))