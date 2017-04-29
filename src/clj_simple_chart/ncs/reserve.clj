(ns clj-simple-chart.ncs.reserve
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csvmap]
            [clojure.pprint :as pprint]
            [clojure.test :as test]))

(def field-reserves-url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_reserves&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=80.213.237.130&CultureCode=en")
(defonce field-reserves-raw (-> field-reserves-url client/get :body csvmap/csv-map))

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

(def numeric-fields [:fldRecoverableOil
                     :fldRecoverableGas
                     :fldRecoverableNGL
                     :fldRecoverableCondensate
                     :fldRecoverableOE
                     :fldRemainingOil
                     :fldRemainingGas
                     :fldRemainingNGL
                     :fldRemainingCondensate
                     :fldRemainingOE])

(defn parse-row [row]
  (reduce (fn [r field] (update r field
                                (fn [x]
                                  (if (number? (read-string x))
                                    (read-string x)
                                    (throw (Exception. (str "Expected number, got " x)))))))
          row
          numeric-fields))

(def data-parsed (map parse-row data))

(def field-names (map :fldName data))

(defn get-reserve [field-name kind]
  {:pre [(some #{field-name} field-names)
         (some #{kind} [:fldRecoverableOE :fldRecoverableOil :fldRecoverableGas])]}
  (-> (filter #(= (:fldName %) field-name) data)
      first
      (get kind)
      read-string))