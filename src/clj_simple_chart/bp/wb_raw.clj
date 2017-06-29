(ns clj-simple-chart.bp.wb-raw
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.data.json :as json]))

(def urls {"SP.POP.TOTL" :population
           })

(def cached-get (memoize client/get))

(def cc2 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-2.json"))))
(def cc3 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-3.json"))))

(def cc3-to-cc2
  (->> [cc2 cc3]
       (flatten)
       (group-by (fn [x] (get x "name")))
       (vals)
       (mapv #(reduce merge {} %))
       (reduce (fn [o v] (assoc o (get v "alpha-3") (get v "alpha-2"))) {})))

(def country-name (keyword "Country Name"))
(def country-code (keyword "Country Code"))

(defn parse-country-record [prop c]
  (let [country (get c country-name)
        country-code (get c country-code)]
    (reduce (fn [o [k v]]
              (if (or (= k country)
                      (= k (keyword "Country"))
                      (= k country-code)
                      (zero? (count (name k))))
                o
                (let [year-value (read-string (name k))
                      vv (if (= "" v) nil (read-string v))]
                  (assert (integer? year-value) (str "Expected column/year to be integer, was >" year-value "<"))
                  (conj o {:country      country
                           :country_code (keyword (get cc3-to-cc2 country-code))
                           :year         year-value
                           prop          vv})))) [] c)))

(defn parse-url [indicator prop]
  (let [resp (cached-get (str "http://api.worldbank.org/countries/all/indicators/" indicator "?format=csv"))
        expected-columns [country-code country-name]
        csv-map (csv/csv-map (:body resp))]
    (spit (str "./data/wb/" indicator ".csv") (:body resp))
    (test/is (= 200 (:status resp)))
    (println (:columns csv-map))
    (->> csv-map
         (csv/assert-columns expected-columns)
         (:data)
         (mapv (partial parse-country-record prop))
         (flatten))))

(def all-data (->> urls
                   (mapv #(parse-url (first %) (second %)))
                   (flatten)))

;(defonce resp (client/get source-url))
;(test/is (= 200 (:status resp)))
;(def data (csv/csv-map (:body resp)))
