(ns clj-simple-chart.bp.diagrams.coal-per-capita-sel
  (:require [clj-simple-chart.bp.bpdata2 :as bpdata]
            [clj-simple-chart.core :refer :all]
            [clj-simple-chart.bp.wb-raw :as wb-raw]
            [clj-simple-chart.opentype :as opentype]
            [clj-simple-chart.translate :refer :all]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.axis.core :as axis]
            [clj-simple-chart.rect :as rect]
            [clj-simple-chart.chart :as chart]
            [clj-simple-chart.bp.units :as units]
            [clj-simple-chart.colors :as colors]
            [clojure.test :as test]))

(defn produce-data [dat]
  (->> dat
       (filter :coal_consumption_mtoe)
       (filter :population)
       (mapv #(assoc %
                :total
                (/ (* 1000.0 units/million (:coal_consumption_mtoe %))
                   (:population %))))
       (mapv #(dissoc % :gas_consumption_bm3
                      :oil_consumption_kbd
                      :oil_consumption_mtoe
                      :total_mtoe
                      :renewables_consumption_mtoe
                      :co2_emissions_mt
                      ;:coal_consumption_mtoe
                      :coal_production_mtoe
                      :coal_production_ton
                      ;:country
                      :country_code
                      :electricity_generation_twh
                      :gas_consumption_mtoe
                      :gas_production_bm3
                      :gas_production_mtoe
                      :gas_proved_reserves_trillion_cubic_metres
                      :gdp
                      :geo_biomass_other_mtoe
                      :geo_biomass_other_twh
                      :hydro_consumption_mtoe
                      :hydro_consumption_twh
                      :imports-of-goods-and-services-percentage
                      :nuclear_consumption_mtoe
                      :nuclear_consumption_twh
                      :oil_production_kbd
                      :oil_production_mtoe
                      :oil_reserves_gb
                      ;:population
                      :regular_country
                      :renewables_consumption_twh
                      :solar_consumption_mtoe
                      :solar_consumption_twh
                      ;:total
                      :wind_consumption_mtoe
                      :wind_consumption_twh
                      ;:year
                      ))
       (mapv #(update % :total double))
       (mapv #(assoc % :coal_consumption_per_capita_oe (:total %)))
       ))

(def data (->> (produce-data bpdata/most-recent-data)
               (sort-by :total)
               (reverse)))

(csv/write-csv-format "data/bp/coal-consumption-per-capita-oe.csv"
                      {:data    data
                       :format  {:coal_consumption_per_capita_oe "%.1f"
                                 :coal_consumption_mtoe "%.1f"}
                       :columns [:year
                                 :country
                                 :coal_consumption_per_capita_oe
                                 :population
                                 :coal_consumption_mtoe]})