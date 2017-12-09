(ns clj-simple-chart.ncs.discovery
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
            [clojure.set :refer [rename-keys]]
            [clj-simple-chart.ncs.resource :as resource]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
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

(def status-map {"Approved for production"            :pdo-approved
                 "Producing"                          :producing
                 "Production in clarification phase"  :clarification
                 "Production likely, but unclarified" :likely
                 "Production not evaluated"           :not-evaluated
                 "Shut down"                          :shut-down})

(defn recoverable [{fld-name :fldName dsc-name :dscName} kind]
  (if (and (some? fld-name) (some #{fld-name} reserve/field-names-all))
    (reserve/get-reserve fld-name kind)
    (resource/get-resource dsc-name kind)))

(def parsed (->> data
                 (remove #(= "Included in other discovery" (:dscCurrentActivityStatus %)))
                 (map #(rename-keys % {:dscCurrentActivityStatus :status
                                       :dscDiscoveryYear         :year}))
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


(def start-year (->> parsed
                     (map :year)
                     (apply min)))

(def stop-year production/stop-year)

(def producing-field-names (->> parsed
                                (filter #(= :producing (:status %)))
                                (map :name)
                                (remove #(= "TAMBAR ØST" %))
                                (distinct)
                                (sort)
                                (vec)))

(def shut-down-field-names (->> parsed
                                (filter #(= :shut-down (:status %)))
                                (map :name)
                                (distinct)
                                (sort)
                                (vec)))

(def missing-field-production
  (->> parsed
       (filter #(some #{(:status %)} [:producing :shut-down]))
       (map :name)
       (distinct)
       (remove #(some #{%} production/field-names))
       (sort)
       (vec)))

(test/is (= ["TAMBAR ØST"] missing-field-production))

(defn cumulative-original-recoverable-by-status
  [status year kind]
  {:pre [(some #{kind} [:fldRecoverableLiquids :fldRecoverableGas :liquids :gas])
         (some #{status} (vals status-map))]}
  (cond (= :liquids kind)
        (recur status year :fldRecoverableLiquids)

        (= :gas kind)
        (recur status year :fldRecoverableGas)

        :else
        (->> parsed
             (filter #(= status (:status %)))
             (filter #(>= year (:year %)))
             (map kind)
             (reduce + 0)
             (double))))

(test/is (< (cumulative-original-recoverable-by-status :producing 2000 :fldRecoverableLiquids)
            (cumulative-original-recoverable-by-status :producing 2010 :fldRecoverableLiquids)))

(defn produce-row-year [year kind]
  (assoc {}
    :year year
    :shut-down-produced (production/cumulative-production shut-down-field-names year kind)
    :producing-produced (production/cumulative-production producing-field-names year kind)
    :remaining-reserves (- (+ (cumulative-original-recoverable-by-status :producing year kind)
                              (cumulative-original-recoverable-by-status :shut-down year kind))
                           (production/cumulative-production production/field-names year kind))
    :pdo-approved (cumulative-original-recoverable-by-status :pdo-approved year kind)
    :clarification (cumulative-original-recoverable-by-status :clarification year kind)
    :likely (cumulative-original-recoverable-by-status :likely year kind)
    :not-evaluated (cumulative-original-recoverable-by-status :not-evaluated year kind)))

(def flat-data
  (for [yr (range start-year (inc stop-year))]
    (produce-row-year yr :liquids)))

(def number-columns [:shut-down-produced :producing-produced
                     :remaining-reserves :pdo-approved :clarification
                     :likely :not-evaluated])

(csv/write-csv-format "data/ncs/produced-reserves-liquids-gb.csv"
                      {:columns [:year :shut-down-produced :producing-produced
                                 :remaining-reserves :pdo-approved :clarification
                                 :likely :not-evaluated]
                       :data (->> flat-data
                                  (map #(reduce (fn [o [k v]]
                                                  (if (some #{k} number-columns)
                                                    (assoc o k (double (/ (* 6.29 v) 1000)))
                                                    (assoc o k v)))
                                                {}
                                                %))
                                  (sort-by :year)
                                  (reverse))
                       :format (zipmap number-columns (repeat "%.1f"))})
