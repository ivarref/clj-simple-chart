(ns clj-simple-chart.ssb.nettokontantstraum
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-simple-chart.ssb.nettokontantstraumprognose :as forecast]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ssb.kpi :as kpi]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/11013")
(def qq [{:code "ContentsCode" :selection {:filter "all" :values ["*"]}}
         {:code "Tid" :selection {:filter "all" :values ["*"]}}])
(def q {:query qq :response {:format "csv"}})
(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
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

(defn entry-to-2016-nok [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  :else (assoc o k (kpi/to-2016-nok (:dato x) v))))
          {}
          x))

(defn entry-to-2017-nok [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  :else (assoc o k (kpi/to-2017-nok (:dato x) v))))
          {}
          x))

(def inflation-adjusted (mapv entry-to-2017-nok parsed))

(def actual-columns (flatten [:dato (mapv keyword (distinct (filter string? (map prop columns))))]))

(csv/write-csv "./data/11013/11013.csv" {:data    parsed
                                         :columns actual-columns})

(defn produce-4-qms [x]
  (reduce (fn [o [k v]]
            (cond (= k :prev-rows) o
                  (= k :dato) (-> o
                                  (assoc :dato v)
                                  (assoc :year (read-string (subs v 0 4))))
                  :else (assoc o k (reduce + 0 (mapv k (:prev-rows x))))))
          {}
          x))

(def four-quarters-moving-sum (->> parsed
                                   (map-indexed (fn [idx x]
                                                  (assoc x :prev-rows (take-last 4 (take (inc idx) parsed)))))
                                   (filter #(= 4 (count (:prev-rows %))))
                                   (mapv produce-4-qms)
                                   (mapv #(dissoc % :prev-rows))))


(def four-quarters-moving-sum-adjusted (->> inflation-adjusted
                                            (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 4 (take (inc idx) inflation-adjusted)))))
                                            (filter #(= 4 (count (:prev-rows %))))
                                            (mapv produce-4-qms)
                                            (mapv #(dissoc % :prev-rows))))

(csv/write-csv "./data/11013/11013-4qms.csv" {:data    four-quarters-moving-sum
                                              :columns actual-columns})

(def four-quarters-moving-sum-eoy
  (->> four-quarters-moving-sum
       (filter #(string/ends-with? (:dato %) "K4"))
       (mapv #(assoc % :year (subs (:dato %) 0 4)))
       (filter #(>= (read-string (:year %)) 2000))
       (mapv #(assoc % :netto-kontantstraum (/ (get % (keyword "Statens netto kontantstrøm fra petroleumsvirksomhet")) 1000)))
       (csv/keep-columns [:year :netto-kontantstraum])
       (mapv #(assoc % :prognose (get forecast/year-to-forecast (:year %) 0.0)))
       (mapv #(assoc % :diff (- (:prognose %) (:netto-kontantstraum %))))))

(csv/write-csv-format "./data/11013/11013-4qms-eoy.csv" {:data    four-quarters-moving-sum-eoy
                                                         :columns [:year
                                                                   :netto-kontantstraum
                                                                   :prognose
                                                                   :diff]
                                                         :format  {:netto-kontantstraum "%.0f"
                                                                   :prognose            "%.0f"
                                                                   :diff                "%.0f"}})

(defn produce-mrd [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  (= k :year) (assoc o k v)
                  :else (assoc o k (format "%.1f" (double (/ v 1000))))))
          {}
          x))

(defn produce-mrd-numeric [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  (= k :year) (assoc o k v)
                  :else (assoc o k (double (/ v 1000)))))
          {}
          x))

(def four-quarters-moving-sum-adjusted-mrd (mapv produce-mrd-numeric four-quarters-moving-sum-adjusted))

(csv/write-csv "./data/11013/11013-mrd-4qms.csv"
               {:data    (mapv produce-mrd four-quarters-moving-sum)
                :columns actual-columns})

(csv/write-csv "./data/11013/11013-mrd-4qms-2017-NOK.csv"
               {:data    (mapv produce-mrd four-quarters-moving-sum-adjusted)
                :columns actual-columns})
