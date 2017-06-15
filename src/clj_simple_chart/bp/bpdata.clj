(ns clj-simple-chart.bp.bpdata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def data-url "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/data.tsv")

(defonce response (client/get data-url))

(def expected-columns
  [:year :country :country_code
   :population :gdp	:coal	:gas :hydro :nuclear :oil :other_renewables
   :solar :wind :coal_production :oil_production
   :gas_production])

(def data (->> (csv/tsv-map (:body response))
               (csv/assert-columns expected-columns)
               (:data)
               (csv/read-string-columns [:population :coal :oil :gas :nuclear :hydro :other_renewables])
               (csv/drop-columns [:country_code :gdp :coal_production
                                  :oil_production :gas_production
                                  :solar :wind])))

(def countries (->> data
                    (mapv :country)
                    (distinct)
                    (sort)
                    (vec)))
