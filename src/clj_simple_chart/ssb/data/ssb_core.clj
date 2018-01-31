(ns clj-simple-chart.ssb.data.ssb-core
  (:require [clj-http.client :as client]
            [clojure.set :as set]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]))

(def get-memo (memoize client/get))

(defn table-str [table]
  (cond (not (string? table)) (recur (str table))
        (= (count table) 5) table
        :else (recur (str "0" table))))

(defn get-meta [table]
  (let [url (str "http://data.ssb.no/api/v0/no/table/" (table-str table))]
    (:body (get-memo url {:as :json}))))

(defn codes [table]
  (map :code (:variables (get-meta table))))

(defn variable [table code]
  {:pre [(some #{code} (codes table))]}
  (first (filter #(= code (:code %)) (:variables (get-meta table)))))

(defn valueText->value [table code valueText]
  {:pre [(some #{code} (codes table))]}
  (let [v (variable table code)
        m (zipmap (:valueTexts v) (:values v))]
    (assert (some #{valueText} (keys m)) (str "Must be one of: \n" (str/join "\n" (vec (sort (keys m)))) "\nwas: \"" valueText "\""))
    (get m valueText)))
