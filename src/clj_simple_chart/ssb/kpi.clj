(ns clj-simple-chart.ssb.kpi
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]
            [clojure.edn :as edn]))

(def url "http://data.ssb.no/api/v0/no/table/08981")
(def qq [{:code "Maaned" :selection {:filter "all" :values ["*"]}}
         {:code "ContentsCode" :selection {:filter "all" :values ["*"]}}
         {:code "Tid" :selection {:filter "all" :values ["*"]}}])
(def q {:query qq :response {:format "csv"}})
(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= "text/csv; charset=Windows-1252" (get (:headers response) "Content-Type")))

(def resp (String. (:body response) "Windows-1252"))
(def csv-parsed (csv/csv-map resp))
(def columns (:columns csv-parsed))
(def data (->> (:data csv-parsed)
               (remove #(= "Årsgjennomsnitt" (:måned %)))))

(defn year [x]
  (cond (keyword? x) (recur (name x))
        (re-matches #"^.+ \d{4}$" x) (last (string/split x #" "))
        :else nil))

(def months (zipmap
              ["Januar" "Februar" "Mars" "April" "Mai" "Juni" "Juli" "August" "September" "Oktober" "November" "Desember"]
              (mapv #(format "%02d" %) (range 1 13))))

(def month-to-quarter
  {"01" "1"
   "02" "1"
   "03" "1"

   "04" "2"
   "05" "2"
   "06" "2"

   "07" "3"
   "08" "3"
   "09" "3"

   "10" "4"
   "11" "4"
   "12" "4"})

(defn process-entry [x]
  (reduce (fn [o [k v]]
            (cond (year k) (conj o {:dato         (str (year k) "-" (get months (:måned x)))
                                    :year         (str (year k))
                                    :year-quarter (str (year k) "K"
                                                       (get month-to-quarter
                                                            (get months (:måned x))))
                                    :kpi          v})
                  :else o))
          []
          x))

(def parsed (->> data
                 (mapv process-entry)
                 (flatten)
                 (sort-by :dato)
                 (remove #(= "." (:kpi %)))
                 (vec)))


(csv/write-csv "./data/08981/08981-kpi.csv" {:data    parsed
                                             :columns [:dato :kpi]})

(def quarterly (->> parsed
                    (group-by :year-quarter)
                    (vals)
                    (mapv (fn [x] {:dato (:year-quarter (first x))
                                   :kpi  (/ (reduce + 0 (mapv (comp edn/read-string :kpi) x))
                                            (count x))}))
                    (sort-by :dato)
                    (vec)))

(def quarterly-4qma (->> quarterly
                         (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 4 (take (inc idx) quarterly)))))
                         (filter #(= 4 (count (:prev-rows %))))
                         (map #(assoc % :kpi (/ (reduce + 0 (mapv :kpi (:prev-rows %))) 4)))
                         (mapv #(dissoc % :prev-rows))))

(def quarter-to-kpi (zipmap (mapv :dato quarterly)
                            (mapv :kpi quarterly)))

(def quarter-4qma-to-kpi (zipmap (mapv :dato quarterly-4qma)
                                 (mapv :kpi quarterly-4qma)))

(def baseline-2018-items (->> parsed
                              (filter #(= "2022" (:year %)))
                              (mapv (comp edn/read-string :kpi))))

(def baseline-2018 (/ (reduce + 0 baseline-2018-items) (count baseline-2018-items)))

(defn to-2018-nok-4qma [dato-with-quarter v]
  (* v (/ baseline-2018 (get quarter-4qma-to-kpi dato-with-quarter))))

(defn to-2018-nok [dato-with-quarter v]
  (* v (/ baseline-2018 (get quarter-to-kpi dato-with-quarter))))

(csv/write-csv "./data/08981/08981-kpi-quarterly.csv"
               {:data    (mapv #(assoc % :kpi (format "%.1f" (:kpi %))) quarterly)
                :columns [:dato :kpi]})
