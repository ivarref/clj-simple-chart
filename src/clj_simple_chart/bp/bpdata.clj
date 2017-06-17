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
   :population :gdp :coal :gas :hydro :nuclear :oil :other_renewables
   :solar :wind :coal_production :oil_production
   :gas_production])

(def mtoe-properties [:coal
                      :oil
                      :gas
                      :nuclear
                      :hydro
                      :other_renewables])

(def one-million 1000000)

(defn add-per-capita [{year       :year
                       country    :country
                       gdp        :gdp
                       population :population
                       coal       :coal
                       oil        :oil
                       gas        :gas
                       nuclear    :nuclear
                       hydro      :hydro
                       renewables :other_renewables
                       total      :total
                       :as        original}]
  (let [per-capita-props
        {:coal             (/ (* one-million coal) population)
         :oil              (/ (* one-million oil) population)
         :gas              (/ (* one-million gas) population)
         :nuclear          (/ (* one-million nuclear) population)
         :hydro            (/ (* one-million hydro) population)
         :other_renewables (/ (* one-million renewables) population)
         :total            (/ (* one-million total) population)
         :gdp              (when (number? gdp)
                             (/ gdp population))}]
    (assoc original :per-capita per-capita-props)))

(def data (->> (csv/tsv-map (:body response))
               (csv/assert-columns expected-columns)
               (:data)
               (csv/read-string-columns [:gdp
                                         :population
                                         :coal
                                         :oil
                                         :gas
                                         :nuclear
                                         :hydro
                                         :other_renewables])
               (mapv #(update % :gdp (fn [gdp] (when (number? gdp) gdp))))
               (mapv #(assoc % :total (reduce + 0 (mapv (fn [x] (get % x)) mtoe-properties))))
               (mapv add-per-capita)
               (csv/drop-columns [:country_code
                                  :coal_production
                                  :oil_production
                                  :gas_production
                                  :solar
                                  :wind])))

(def countries (->> data
                    (mapv :country)
                    (distinct)
                    (sort)
                    (vec)))
