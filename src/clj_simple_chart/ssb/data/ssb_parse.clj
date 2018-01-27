(ns clj-simple-chart.ssb.data.ssb-parse
  (:require [clojure.set :as set]
            [clj-simple-chart.ssb.data.ssb-core :as ssb]
            [clojure.string :as str]))

(defn- row-with-time->generic-row [table]
  (let [v (:valueTexts (ssb/variable table "ContentsCode"))
        all-tid (:valueTexts (ssb/variable table "Tid"))]
    (->> (for [prefix v tid all-tid]
           [(keyword (str prefix " " tid))
            (keyword (str/lower-case prefix))])
         (into {}))))

(defn- tid->dato [tid]
  (cond (str/includes? tid "M") (str/replace tid "M" "-")
        :else tid))

(defn- row-with-time->tid [table]
  (let [v (:valueTexts (ssb/variable table "ContentsCode"))
        all-tid (:valueTexts (ssb/variable table "Tid"))]
    (->> (for [prefix v tid all-tid]
           [(keyword (str prefix " " tid))
            (tid->dato tid)])
         (into {}))))

(defn- difference [a b]
  (set/difference (into #{} a) (into #{} b)))

(defn pulled->parsed [table pulled]
  (let [explode-cols-map (row-with-time->generic-row table)
        tid-map (row-with-time->tid table)
        explode-cols-cols (keys explode-cols-map)
        regular-cols (difference (:columns pulled) explode-cols-cols)
        explode-row (fn [row]
                      (reduce (fn [o [k v]]
                                (cond (some #{k} regular-cols) o
                                      (some #{k} explode-cols-cols)
                                      (let [new-item (-> (select-keys row regular-cols)
                                                         (assoc (get explode-cols-map k) (read-string v))
                                                         (assoc :dato (get tid-map k)))]
                                        (conj o new-item))
                                      :else (throw (ex-info (str "Unexpected column") {:column k}))))
                              [] row))]
    {:data    (->> (:data pulled)
                   (mapcat explode-row)
                   (vec))
     :columns (vec (sort (concat regular-cols (vec (distinct (vals (row-with-time->generic-row table)))))))}))
