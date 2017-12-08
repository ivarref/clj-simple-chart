(ns clj-simple-chart.ncs.discovery
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
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

(def parsed (->> data
                 (remove #(= "Included in other discovery" (:dscCurrentActivityStatus %)))
                 (map #(select-keys % [:dscName :fldName :dscDiscoveryYear :dscCurrentActivityStatus]))
                 (csv/read-number-or-throw-columns [:dscDiscoveryYear])
                 (map #(update % :fldName (fn [x] (if (empty? x) nil x))))
                 (map #(assoc % :fldName (or (:fldName %) (:dscName %))))
                 (map #(dissoc % :dscName))
                 (group-by :fldName)
                 (vals)
                 (map #(sort-by (fn [x] (:dscDiscoveryYear x)) %))
                 (map #(first %))
                 (flatten)
                 (sort-by :fldName)
                 (vec)))
