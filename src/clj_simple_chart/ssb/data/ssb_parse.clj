(ns clj-simple-chart.ssb.data.ssb-parse
  (:require [clojure.set :as set]
            [clj-simple-chart.ssb.data.ssb-core :as ssb]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn- row-with-time->generic-row [table]
  (let [v (:valueTexts (ssb/variable table "ContentsCode"))
        all-tid (:valueTexts (ssb/variable table "Tid"))]
    (into {} (for [prefix v tid all-tid]
               [(keyword (str prefix " " tid))
                :ContentsCode]))))

(defn- row-with-time->category [table]
  (let [v (:valueTexts (ssb/variable table "ContentsCode"))
        all-tid (:valueTexts (ssb/variable table "Tid"))]
    (into {} (for [prefix v tid all-tid]
               [(keyword (str prefix " " tid))
                prefix]))))

(defn- tid->dato [tid]
  (cond (str/includes? tid "M") (str/replace tid "M" "-")
        :else tid))

(defn- row-with-time->tid [table]
  (let [v (:valueTexts (ssb/variable table "ContentsCode"))
        all-tid (:valueTexts (ssb/variable table "Tid"))]
    (into {} (for [prefix v tid all-tid]
               [(keyword (str prefix " " tid))
                (tid->dato tid)]))))

(defn- difference [a b]
  (set/difference (into #{} a) (into #{} b)))

(defn text->code-map [table]
  (let [vars (:variables (ssb/get-meta table))]
    (zipmap (map (comp keyword :text) vars)
            (map (comp keyword :code) vars))))

(defn rename-columns-map [query-map]
  (reduce (fn [o [k v]]
            (cond (vector? k)
                  (assoc o (first k) (last k))
                  :else o))
          {}
          query-map))

(defn pulled->parsed [table query-map pulled]
  (let [explode-cols-map (row-with-time->generic-row table)
        tid-map (row-with-time->tid table)
        category-map (row-with-time->category table)
        rename-map (rename-columns-map query-map)
        explode-cols-cols (keys explode-cols-map)
        regular-cols (difference (:columns pulled) explode-cols-cols)
        explode-row (fn [row]
                      (reduce (fn [o [k v]]
                                (cond (some #{k} regular-cols) o
                                      (some #{k} explode-cols-cols)
                                      (let [new-item (-> (select-keys row regular-cols)
                                                         (assoc (get explode-cols-map k) (edn/read-string v))
                                                         (assoc :Tid (get tid-map k))
                                                         (assoc :ContentsCodeCategory (get category-map k)))]
                                        (conj o new-item))
                                      :else (throw (ex-info (str "Unexpected column") {:column k}))))
                              [] row))
        symbol-dot-to-nil (fn [row]
                            (reduce (fn [o [k v]]
                                      (if (or (= v (symbol ".")) (= v (symbol "..")))
                                        (assoc o k nil)
                                        (assoc o k v)))
                                    {}
                                    row))]
    (->> (:data pulled)
         (mapcat explode-row)
         (map symbol-dot-to-nil)
         (map #(set/rename-keys % (text->code-map table)))
         (sort-by :Tid)
         (map #(set/rename-keys % rename-map))
         (vec))))
