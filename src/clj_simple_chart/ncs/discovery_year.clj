(ns clj-simple-chart.ncs.discovery-year
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clj-simple-chart.ncs.raw-production :as production]
            [clj-simple-chart.csv.csvmap :as csvmap]))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/discovery&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=81.191.126.253&CultureCode=en")
; factpages => discovery => table view => overview

(defonce raw-data (-> url
                      (client/get)
                      (:body)
                      (csvmap/csv-map)))

(def data (:data raw-data))

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
            (:columns raw-data)))

(def numeric-columns [:dscDiscoveryYear])

(def field-names (->> (map :fldName data)
                      (distinct)
                      (remove empty?)
                      (sort)
                      (vec)))

(defn de-duplicate [coll]
  {:pre [(coll? coll)]}
  (->> coll
       (sort-by :dscDiscoveryYear)
       (first)))

(def data-parsed (->> data
                      (csvmap/read-string-columns numeric-columns)
                      (csvmap/number-or-throw-columns numeric-columns)
                      (remove #(= "Included in other discovery" (:dscCurrentActivityStatus %)))
                      (group-by :fldName)
                      (vals)
                      (mapv de-duplicate)
                      (flatten)))

(def manual-lookup {"33/9-6 DELTA" 1976})

(defn discovery-year [fldName]
  {:pre [(or (some #{fldName} field-names)
             (some #{fldName} (keys manual-lookup)))
         (not (and (some #{fldName} field-names)
                   (some #{fldName} (keys manual-lookup))))]}
  (or (->> data-parsed
           (filter #(= (:fldName %) fldName))
           (first)
           (:dscDiscoveryYear))
      (get manual-lookup fldName)))

(defn discovery-decade [fldName]
  (let [year (discovery-year fldName)
        decade (- year (mod year 10))]
    decade))

(def discovery-decades (vec (sort (distinct (mapv discovery-decade production/field-names)))))

(defn discovery-decade-bucket [fldName]
  (let [dd (discovery-decade fldName)]
    (str (.indexOf discovery-decades dd) "- " dd)))

(doseq [fld production/field-names]
  (assert (and (number? (discovery-year fld))
               (< 1960 (discovery-year fld))) (str "Expected discovery year for " fld)))

; TODO: Consider adding more tests.
(test/is (= 1969 (discovery-year "EKOFISK")))
(test/is (= 1978 (discovery-year "GULLFAKS")))
(test/is (= 1979 (discovery-year "TROLL")))
(test/is (= 1979 (discovery-year "SNORRE")))
(test/is (= 1981 (discovery-year "VESLEFRIKK")))
(test/is (= 1984 (discovery-year "SNØHVIT")))
(test/is (= 1988 (discovery-year "EMBLA")))

(test/is (= 2000 (discovery-year "GOLIAT")))
(test/is (= 2010 (discovery-year "JOHAN SVERDRUP")))
(test/is (= 2017 (discovery-year "SINDRE")))
