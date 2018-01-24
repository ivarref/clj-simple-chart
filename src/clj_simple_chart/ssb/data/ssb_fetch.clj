(ns clj-simple-chart.ssb.data.ssb-fetch
  (:require [clj-http.client :as client]
            [clojure.set :as set]
            [clj-simple-chart.csv.csvmap :as csv]))

(def get-memo (memoize client/get))

(defn get-meta [table]
  (let [url (str "http://data.ssb.no/api/v0/no/table/" table)]
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
    (assert (some #{valueText} (keys m)) (str "Must be one of: " (vec (sort (keys m))) ", was: \"" valueText "\""))
    (get m valueText)))

(defn filter-values [table code valueTexts]
  (assert (some #{code} (codes table)) (str "Code must be one of: " (vec (codes table)) ", was: \"" code "\""))
  (cond (= "*" (first valueTexts))
        {:code code :selection {:filter "all" :values ["*"]}}
        :else {:code code :selection {:filter "item" :values (mapv (partial valueText->value table code) valueTexts)}}))

(defn map->query-vector [table m]
  (let [mm (reduce (fn [o [k v]]
                     (if (string? v)
                       (assoc o k [v])
                       (assoc o k v)))
                   {}
                   m)]
    (map (fn [[k v]] (filter-values table k v)) mm)))

(defn pull [table query-vector]
  (doseq [code (codes table)]
    (assert (some #{code} (map :code query-vector)) (str "Code " code " must be present in query vector!")))
  (let [query {:query query-vector :response {:format "csv"}}
        resp (client/post (str "http://data.ssb.no/api/v0/no/table/" table) {:form-params  query
                                                                             :content-type :json
                                                                             :as           :byte-array})]
    (assert (= "text/csv; charset=Windows-1252" (get-in resp [:headers "Content-Type"])))
    (let [body-str (String. (:body resp) "Windows-1252")]
      (csv/csv-map body-str))))

(defn pull-map [table m]
  (pull table (map->query-vector table m)))

(def pull-map-cached (memoize pull-map))

(defn row-with-time->generic-row [table]
  (let [v (:valueTexts (variable table "ContentsCode"))
        all-tid (:valueTexts (variable table "Tid"))]
    (->> (for [prefix v tid all-tid]
           [(keyword (str prefix " " tid))
            (keyword prefix)])
         (into {}))))

(defn row-with-time->tid [table]
  (let [v (:valueTexts (variable table "ContentsCode"))
        all-tid (:valueTexts (variable table "Tid"))]
    (->> (for [prefix v tid all-tid]
           [(keyword (str prefix " " tid))
            tid])
         (into {}))))

(defn difference [a b]
  (set/difference (into #{} a) (into #{} b)))

(defn parse-pulled [table pulled]
  (let [explode-cols-map (row-with-time->generic-row table)
        tid-map (row-with-time->tid table)
        explode-cols-cols (keys explode-cols-map)
        regular-cols (difference (:columns pulled) explode-cols-cols)
        explode-row (fn [row]
                      (reduce (fn [o [k v]]
                                (cond (some #{k} regular-cols) o
                                      (some #{k} explode-cols-cols)
                                      (let [new-item (-> (select-keys row regular-cols)
                                                         (assoc (get explode-cols-map k) v)
                                                         (assoc :Tid (get tid-map k)))]
                                        (conj o new-item))
                                      :else (throw (ex-info (str "Unexpected column") {:column k}))))
                              [] row))]
    (->> (:data pulled)
         (mapcat explode-row)
         (vec))))

(defn pull-parse [table query-map]
  (parse-pulled table (pull-map-cached table query-map)))

(def pull-parse-cached (memoize pull-parse))

(def qq {"ContentsCode"  "Salg"
         "Region"        "Hele landet"
         "PetroleumProd" ["Autodiesel" "Bilbensin"]
         "Kjopegrupper"  "Alle kjøpegrupper"
         "Tid"           "*"})

;(def data (pull-map-cached 11174 qq))

(def parsed (pull-parse 11174 qq))

;(csv/write-csv "data/ssb/11174.csv" {:columns (:columns data) :data (:data data)})