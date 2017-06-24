(ns clj-simple-chart.bp.bpdata2
  (:require [clojure.test :as test]
            [clj-simple-chart.bp.wbdata :as wbdata]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]
            [clojure.set :as set]))

(def cached-get (memoize client/get))

(def url-prefix "https://raw.githubusercontent.com/ivarref/EnergyExportDatabrowser/master/StaticData/2017/")

(def urls
  {
   "BP_2017_co2_emissions_mt.csv"                          :co2_emissions_mt

   "BP_2017_coal_consumption_mtoe.csv"                     :coal_consumption_mtoe
   "BP_2017_coal_production_mtoe.csv"                      :coal_production_mtoe
   "BP_2017_coal_production_ton.csv"                       :coal_production_ton

   "BP_2017_gas_consumption_m3.csv"                        :gas_consumption_m3
   "BP_2017_gas_consumption_mtoe.csv"                      :gas_consumption_mtoe
   "BP_2017_gas_production_m3.csv"                         :gas_production_m3
   "BP_2017_gas_production_mtoe.csv"                       :gas_production_mtoe
   "BP_2017_gas_proved_reserves_trillion_cubic_metres.csv" :gas_proved_reserves_trillion_cubic_metres

   "BP_2017_geo_biomass_other_mtoe.csv"                    :geo_biomass_other_mtoe
   "BP_2017_geo_biomass_other_twh.csv"                     :geo_biomass_other_twh

   "BP_2017_hydro_consumption_mtoe.csv"                    :hydro_consumption_mtoe
   "BP_2017_hydro_consumption_twh.csv"                     :hydro_consumption_twh

   "BP_2017_nuclear_consumption_mtoe.csv"                  :nuclear_consumption_mtoe
   "BP_2017_nuclear_consumption_twh.csv"                   :nuclear_consumption_twh

   "BP_2017_oil_consumption_bbl.csv"                       :oil_consumption_kbd
   "BP_2017_oil_consumption_mtoe.csv"                      :oil_consumption_mtoe
   "BP_2017_oil_production_bbl.csv"                        :oil_production_kbd
   "BP_2017_oil_production_mtoe.csv"                       :oil_production_mtoe
   "BP_2017_oil_proved_reserves_gb.csv"                    :oil_reserves_gb

   "BP_2017_renewables_consumption_mtoe.csv"               :renewables_consumption_mtoe
   "BP_2017_renewables_consumption_twh.csv"                :renewables_consumption_twh

   "BP_2017_solar_consumption_mtoe.csv"                    :solar_consumption_mtoe
   "BP_2017_solar_consumption_twh.csv"                     :solar_consumption_twh

   "BP_2017_wind_consumption_mtoe.csv"                     :wind_consumption_mtoe
   "BP_2017_wind_consumption_twh.csv"                      :wind_consumption_twh
   })

(defn parse-url [url prop]
  (let [resp (cached-get (str url-prefix url))
        expected-columns [:YEAR]
        csv-map (csv/csv-map (:body resp))]
    (test/is (= 200 (:status resp)))
    (->> csv-map
         (csv/assert-columns expected-columns)
         (:data)
         (csv/read-string-columns (:columns csv-map))
         (csv/number-or-nil-columns (:columns csv-map))
         (mapv #(set/rename-keys % {:country :country_code}))
         (mapv #(reduce
                  (fn [o [k v]]
                    (if (not= :YEAR k)
                      (conj o {:year (:YEAR %) :country_code k prop v})
                      o)) [] %)))))


(defn get-wb-data [{cc :country_code year :year}]
  (-> (get wbdata/all-data [cc year]
           (get wbdata/all-data [cc (dec year)] {}))
      (assoc :year year)))

(def all-data (->> urls
                   (mapv #(parse-url (first %) (second %)))
                   (flatten)
                   (group-by (fn [x] [(:country_code x) (:year x)]))
                   (vals)
                   (mapv #(reduce merge {} %))
                   (mapv #(assoc % :country (get wbdata/cc2-to-name (:country_code %))))
                   (mapv #(merge (get-wb-data %) %))
                   (mapv #(assoc % :regular_country (not (.startsWith (name (:country_code %)) "BP_"))))))

(def missing-country (filter #(nil? (:country %)) all-data))
(test/is (zero? (count missing-country)))

(def max-year (apply max (mapv :year all-data)))

(def most-recent-data (filter #(= max-year (:year %)) all-data))
(def most-recent-data-countries (filter #(:regular_country %) most-recent-data))

(defn find-recent-country [country]
  (first (filter #(= country (:country_code %)) most-recent-data)))
