(ns clj-simple-chart.ssb.valuta2
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.set :as set]
            [clojure.string :as str]))

(def valuta-url
  (str "https://data.norges-bank.no/api/data/EXR/M..NOK.SP"
       "?startPeriod=1900-01-01&endPeriod=2119-09-21"
       "&format=csv&locale=no"))

(defonce resp (client/get valuta-url))

(def data (->> (csv/csv-map-assert-columns (:body resp) [] :separator \;)
               :data
               (filter #(= "USD" (:BASE_CUR %)))
               (mapv #(set/rename-keys % {:OBS_VALUE :usd
                                          :TIME_PERIOD :dato}))
               (mapv #(select-keys % [:usd :dato]))
               (mapv #(update % :usd (fn [x] (Double/valueOf (str/replace x "," ".")))))
               (sort-by :dato)))

(def one-usd-numeric-12-mma
  (->> data
       (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 12 (take (inc idx) data)))))
       (filter #(= 12 (count (:prev-rows %))))
       (mapv #(assoc % :usd (/ (reduce + 0 (mapv :usd (:prev-rows %))) 12)))
       (mapv #(dissoc % :prev-rows))))

(def one-usd-date-to-nok (reduce (fn [o v] (assoc o (:dato v) (:usd v))) {} one-usd-numeric-12-mma))
