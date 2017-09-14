(ns clj-simple-chart.ncs.reserve
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.pprint :as pprint]
            [clojure.test :as test]))

(def field-reserves-url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_reserves&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=80.213.237.130&CultureCode=en")
(defonce field-reserves-raw (-> field-reserves-url
                                (client/get)
                                (:body)
                                (csv/csv-map)))

(def columns (:columns field-reserves-raw))
(def data (:data field-reserves-raw))

(test/is (= columns [:fldName
                     :fldRecoverableOil
                     :fldRecoverableGas
                     :fldRecoverableNGL
                     :fldRecoverableCondensate
                     :fldRecoverableOE
                     :fldRemainingOil
                     :fldRemainingGas
                     :fldRemainingNGL
                     :fldRemainingCondensate
                     :fldRemainingOE
                     :fldDateOffResEstDisplay
                     :fldNpdidField
                     :DatesyncNPD]))

(def numeric-columns [:fldRecoverableOil
                      :fldRecoverableGas
                      :fldRecoverableNGL
                      :fldRecoverableCondensate
                      :fldRecoverableOE
                      :fldRemainingOil
                      :fldRemainingGas
                      :fldRemainingNGL
                      :fldRemainingCondensate
                      :fldRemainingOE])

(def data-parsed (->> data
                      (csv/read-string-columns numeric-columns)
                      (csv/number-or-throw-columns numeric-columns)))

(def field-names (map :fldName data))

(defn get-reserve [field-name kind]
  {:pre [(some #{field-name} field-names)
         (some #{kind} [:fldRecoverableOE :fldRecoverableOil :fldRecoverableGas])]}
  (-> (filter #(= (:fldName %) field-name) data-parsed)
      first
      (get kind)))