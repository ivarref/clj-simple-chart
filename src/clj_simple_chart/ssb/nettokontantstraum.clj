(ns clj-simple-chart.ssb.nettokontantstraum
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/11013")
(def qq [{:code "ContentsCode" :selection {:filter "all" :values ["*"]}}
         {:code "Tid" :selection {:filter "all" :values ["*"]}}])
(def q {:query qq :response {:format "csv"}})
(def response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= "text/csv; charset=Windows-1252" (get (:headers response) "Content-Type")))

(def resp (String. (:body response) "Windows-1252"))
(def csv-parsed (csv/csv-map resp))
(def columns (:columns csv-parsed))
(def data (:data csv-parsed))

(def sample-column "Skatter på utvinning av petroleum 1991K1")

(defn year-and-quarter [x]
  (cond (keyword? x) (recur (name x))
        (re-matches #"^.+ \d{4}K\d$" x) (last (string/split x #" "))
        :else nil))

(defn prop [x]
  (cond (keyword? x) (recur (name x))
        (re-matches #"^.+ \d{4}K\d$" x) (string/join " " (drop-last (string/split x #" ")))
        :else nil))

(defn process-entry [[k v]]
  {:dato  (year-and-quarter k)
   :prop  (keyword (prop k))
   :value v})

(defn process-grouped [values]
  (reduce (fn [o v]
            (assoc o (:prop v)
                     (read-string (:value v))))
          {:dato (:dato (first values))}
          values))

(def parsed (->> data
                 (mapcat (partial mapv process-entry))
                 (remove #(nil? (:dato %)))
                 (group-by :dato)
                 (vals)
                 (mapv process-grouped)
                 (sort-by :dato)))

(def actual-columns (flatten [:dato (mapv keyword (distinct (filter string? (map prop columns))))]))

(csv/write-csv "./data/11013/11013.csv" {:data    parsed
                                         :columns actual-columns})

(defn produce-4-qms [x]
  (reduce (fn [o [k v]]
            (cond (= k :prev-rows) o
                  (= k :dato) (assoc o k v)
                  :else (assoc o k (reduce + 0 (mapv k (:prev-rows x))))))
          {}
          x))

(def four-quarters-moving-sum (->> parsed
                                   (map-indexed (fn [idx x]
                                                  (assoc x :prev-rows (take-last 4 (take (inc idx) parsed)))))
                                   (filter #(= 4 (count (:prev-rows %))))
                                   (mapv produce-4-qms)
                                   (mapv #(dissoc % :prev-rows))
                                   ))

(csv/write-csv "./data/11013/11013-4qms.csv" {:data    four-quarters-moving-sum
                                              :columns actual-columns})

(defn produce-mrd [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  :else (assoc o k (format "%.1f" (double (/ v 1000))))))
          {}
          x))

(csv/write-csv "./data/11013/11013-mrd-4qms.csv" {:data    (mapv produce-mrd four-quarters-moving-sum)
                                                  :columns actual-columns})
