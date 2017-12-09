(ns clj-simple-chart.ncs.discovery
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
            [clojure.set :refer [rename-keys]]
            [clj-simple-chart.ncs.resource :as resource]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clojure.test :as test]))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/discovery&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=81.191.112.135&CultureCode=en")
(defonce raw (-> url
                 (client/get)
                 (:body)
                 (csv/csv-map)))

(def columns (:columns raw))
(def data (:data raw))

(test/is (= [:dscName
             :cmpLongName
             :dscCurrentActivityStatus
             :dscHcType
             :wlbName
             :nmaName
             :fldName
             :dscDateFromInclInField
             :dscDiscoveryYear
             :dscResInclInDiscoveryName
             :dscOwnerKind
             :dscOwnerName
             :dscNpdidDiscovery
             :fldNpdidField
             :wlbNpdidWellbore
             :dscFactPageUrl
             :dscFactMapUrl
             :dscDateUpdated
             :dscDateUpdatedMax
             :DatesyncNPD]
            columns))

(test/is (= ["Approved for production"
             "Included in other discovery"
             "Producing"
             "Production in clarification phase"
             "Production is unlikely"
             "Production likely, but unclarified"
             "Production not evaluated"
             "Shut down"]
            (->> data
                 (map :dscCurrentActivityStatus)
                 (sort)
                 (distinct)
                 (vec))))

(def status-map {"Approved for production" :pdo-approved
                 "Producing" :producing
                 "Production in clarification phase" :clarification
                 "Production likely, but unclarified" :likely
                 "Production not evaluated" :not-evaluated
                 "Shut down" :shut-down})

(defn recoverable [{fld-name :fldName dsc-name :dscName} kind]
  (if (and (some? fld-name) (some #{fld-name} reserve/field-names-all))
      (reserve/get-reserve fld-name kind)
      (resource/get-resource dsc-name kind)))

(def parsed (->> data
                 (remove #(= "Included in other discovery" (:dscCurrentActivityStatus %)))
                 (map #(rename-keys % {:dscCurrentActivityStatus :status
                                       :dscDiscoveryYear :year}))
                 (map #(update % :status status-map))
                 (map #(select-keys % [:dscName :fldName :year :status]))
                 (csv/read-number-or-throw-columns [:year])
                 (map #(update % :fldName (fn [x] (if (empty? x) nil x))))
                 (map #(update % :dscName (fn [x] (if (empty? x) nil x))))
                 (map #(assoc % :name (or (:fldName %) (:dscName %))))
                 (group-by :name)
                 (vals)
                 (map #(sort-by (fn [x] (:year x)) %))
                 (map #(first %))
                 (flatten)
                 (map #(assoc % :fldRecoverableLiquids (recoverable % :fldRecoverableLiquids)))
                 (map #(assoc % :fldRecoverableGas (recoverable % :fldRecoverableGas)))
                 (map #(dissoc % :fldName :dscName))
                 (filter #(or (some? (:fldRecoverableLiquids %))
                              (some? (:fldRecoverableGas %))))
                 (csv/number-or-throw-columns [:fldRecoverableLiquids :fldRecoverableGas])
                 (sort-by :name)
                 (vec)))

(def producing-field-names (->> parsed
                                (filter #(= :producing (:status %)))
                                (map :name)
                                (distinct)
                                (sort)
                                (vec)))

(def shut-down-field-names (->> parsed
                                (filter #(= :shut-down (:status %)))
                                (map :name)
                                (distinct)
                                (sort)
                                (vec)))
