(ns clj-simple-chart.ncs.resource
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
            [clojure.set :refer [rename-keys]]
            [clojure.test :as test]
            [clojure.string :as str]))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/discovery_reserves&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=81.191.112.135&CultureCode=en")

(defonce raw (-> url
                 (client/get)
                 (:body)
                 (csv/csv-map)))

(def columns (:columns raw))
(def data (:data raw))

(test/is (= [:dscName
             :dscReservesRC
             :dscRecoverableOil
             :dscRecoverableGas
             :dscRecoverableNGL
             :dscRecoverableCondensate
             :dscRecoverableOe
             :dscDateOffResEstDisplay
             :dscNpdidDiscovery
             :dscReservesDateUpdated
             :DatesyncNPD]
            columns))

(def parsed (->> data
                 (map #(rename-keys % {:dscName                  :fldName
                                       :dscRecoverableOil        :fldRecoverableOil
                                       :dscRecoverableGas        :fldRecoverableGas
                                       :dscRecoverableNGL        :fldRecoverableNGL
                                       :dscRecoverableCondensate :fldRecoverableCondensate
                                       :dscRecoverableOe         :fldRecoverableOE}))
                 (map #(select-keys % [:fldName :fldRecoverableOil :fldRecoverableGas
                                       :fldRecoverableNGL :fldRecoverableCondensate :fldRecoverableOE]))
                 (csv/read-number-or-throw-columns [:fldRecoverableOil
                                                    :fldRecoverableGas
                                                    :fldRecoverableNGL
                                                    :fldRecoverableCondensate
                                                    :fldRecoverableOE])
                 (map #(update % :fldName str/trim))
                 (group-by :fldName)
                 (vals)
                 (map #(assoc {}
                         :fldName (:fldName (first %))
                         :fldRecoverableGas (reduce + 0 (map :fldRecoverableGas %))
                         :fldRecoverableOE (reduce + 0 (map :fldRecoverableOE %))
                         :fldRecoverableLiquids (+ (reduce + 0 (map :fldRecoverableOil %))
                                                   (reduce + 0 (map :fldRecoverableNGL %))
                                                   (reduce + 0 (map :fldRecoverableCondensate %)))))
                 (flatten)
                 (sort-by :fldName)
                 (vec)))

(def field-names (map :fldName parsed))
(test/is (= (count field-names) (count (distinct field-names))))

(defn get-resource [field-name kind]
  {:pre [(some #{kind} [:fldRecoverableOE :fldRecoverableGas :fldRecoverableLiquids])]})
