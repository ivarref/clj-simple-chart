(ns clj-simple-chart.ssb.data.ssb-pull
  (:require [clj-http.client :as client]
            [clj-simple-chart.ssb.data.ssb-core :as ssb]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]))

(defn- filter-values [table code valueTexts]
  (assert (some #{code} (ssb/codes table)) (str "Code must be one of:\n" (str/join "\n" (sort (ssb/codes table))) "\nwas: \"" code "\""))
  (cond (= "*" (first valueTexts))
        {:code code :selection {:filter "all" :values ["*"]}}
        :else {:code code :selection {:filter "item" :values (mapv (partial ssb/valueText->value table code) valueTexts)}}))

(defn- map->query-vector [table m]
  {:pre [(map? m)]}
  (let [mm (reduce (fn [o [k v]]
                     (cond (string? v) (assoc o k [v])
                           (keyword? v) (assoc o k [(name v)])
                           :else (assoc o k v)))
                   {}
                   m)]
    (mapv (fn [[k v]] (filter-values table k v)) mm)))

(defn- pull-inner [table query-vector]
  {:pre [(vector? query-vector)]}
  (doseq [code (ssb/codes table)]
    (assert (some #{code} (map :code query-vector)) (str "Code " code " must be present in query vector!")))
  (let [query {:query query-vector :response {:format "csv"}}
        resp (client/post (str "http://data.ssb.no/api/v0/no/table/" (ssb/table-str table))
                          {:form-params  query
                           :content-type :json
                           :as           :byte-array})]
    (assert (= "text/csv; charset=Windows-1252" (get-in resp [:headers "Content-Type"])))
    (let [body-str (String. (:body resp) "Windows-1252")]
      (csv/csv-map body-str))))

(defn pull [table m]
  {:pre [(map? m)]}
  (pull-inner table (map->query-vector table m)))

(def ^:private qq {"ContentsCode"  "Salg"
                   "Region"        "Hele landet"
                   "PetroleumProd" ["Autodiesel" "Bilbensin"]
                   "Kjopegrupper"  "Alle kjøpegrupper"
                   "Tid"           "*"})

(def ^:private pulled (pull 11174 qq))
