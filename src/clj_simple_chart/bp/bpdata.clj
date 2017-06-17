(ns clj-simple-chart.bp.bpdata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def data-url "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/data.tsv")

(defonce response (client/get data-url))

(test/is (= 200 (:status response)))

(def expected-columns
  [:year
   :country
   :country_code
   :population
   :gdp
   :coal
   :gas
   :hydro
   :nuclear
   :oil
   :other_renewables
   :solar
   :wind
   :co2
   :coal_production
   :oil_production
   :gas_production])

(def mtoe-properties [:coal
                      :oil
                      :gas
                      :nuclear
                      :hydro
                      :other_renewables])

(def one-million 1000000)

(defn add-per-capita [{year           :year
                       country        :country
                       gdp            :gdp
                       population     :population
                       coal           :coal
                       oil            :oil
                       gas            :gas
                       nuclear        :nuclear
                       hydro          :hydro
                       renewables     :other_renewables
                       gas-production :gas_production
                       oil-production :oil_production
                       co2            :co2
                       total          :total
                       :as            original}]
  (let [per-capita-props
        {:coal             (/ (* one-million coal) population)
         :oil              (/ (* one-million oil) population)
         :gas              (/ (* one-million gas) population)
         :nuclear          (/ (* one-million nuclear) population)
         :hydro            (/ (* one-million hydro) population)
         :other_renewables (/ (* one-million renewables) population)
         :total            (/ (* one-million total) population)
         :gdp              (when (number? gdp)
                             (/ gdp population))
         :co2              (when (number? co2)
                             (/ (* one-million co2) population))
         :gas_production   (when (number? gas-production)
                             (/ (* one-million gas-production) population))
         :oil_production   (when (number? oil-production)
                             (/ (* one-million oil-production) population))}]
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
                                         :other_renewables
                                         :co2
                                         :gas_production
                                         :oil_production])
               (csv/number-or-nil-columns [:gdp :co2 :gas_production :oil_production])
               (mapv #(assoc % :total (reduce + 0 (mapv (fn [x] (get % x)) mtoe-properties))))
               (mapv add-per-capita)
               (csv/drop-columns [
                                  :coal_production
                                  :solar
                                  :wind])))

(def countries (->> data
                    (mapv :country)
                    (distinct)
                    (sort)
                    (vec)))
