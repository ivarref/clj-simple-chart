(ns clj-simple-chart.bp.wbdata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.set :as set]))

(def cached-get (memoize client/get))

(def cc2 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-2.json"))))
(def cc3 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-3.json"))))

(def urls {"https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/population.csv" :population
           "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/gdp.csv" :gdp})

(defn parse-url [url prop]
  (let [resp (cached-get url)
        expected-columns [(keyword "Country Code") (keyword "Country Name") :Year :Value]
        csv-map (csv/csv-map (:body resp))]
    (test/is (= 200 (:status resp)))
    (->> csv-map
         (csv/assert-columns expected-columns)
         (:data)
         (csv/read-string-columns (:columns csv-map))
         (csv/number-or-nil-columns [:Year :Value])
         (mapv #(set/rename-keys % {:Year :year
                                    (keyword "Country Name") :country
                                    :Value prop})))))

(def all-data (->> urls
                   (mapv #(parse-url (first %) (second %)))
                   (flatten)
                   (group-by (fn [x] [(:year x) (:country x)]))
                   (vals)
                   (mapv #(reduce merge {} %))))

(def cc3-to-cc2
  (->> [cc2 cc3]
       (flatten)
       (group-by (fn [x] (get x "name")))
       (vals)
       (mapv #(reduce merge {} %))
       (reduce (fn [o v] (assoc o (get v "alpha-3") (get v "alpha-2"))) {})))

