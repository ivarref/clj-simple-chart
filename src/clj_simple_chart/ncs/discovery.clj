(ns clj-simple-chart.ncs.discovery
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
            [clojure.set :refer [rename-keys]]
            [clj-simple-chart.ncs.resource :as resource]
            [clj-simple-chart.ncs.reserve :as reserve]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
            [clj-simple-chart.ncs.raw-production :as raw-production]
            [clojure.test :as test]))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/discovery&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=81.191.112.135&CultureCode=en")
(defonce raw (-> url
                 (client/get)
                 (:body)
                 (csv/csv-map)))

(def columns (:columns raw))
(def data (:data raw))

(test/is (= [:dscName :cmpLongName :dscCurrentActivityStatus :dscHcType
             :wlbName :nmaName :fldName :dscDateFromInclInField
             :dscDiscoveryYear :dscResInclInDiscoveryName :dscOwnerKind
             :dscOwnerName :dscNpdidDiscovery :fldNpdidField :wlbNpdidWellbore
             :dscFactPageUrl :dscFactMapUrl :dscDateUpdated :dscDateUpdatedMax
             :DatesyncNPD]
            columns))

(def valid-activity-types (vec (sort ["Approved for production"
                                      "Decided for production"
                                      "Included in other discovery"
                                      "Producing"
                                      "Production in clarification phase"
                                      "Production is unlikely"
                                      "Production likely, but unclarified"
                                      "Production not evaluated"
                                      "Shut down"])))

(test/is (every?
           (fn [item] (some #{item} valid-activity-types))
           (->> data
                (map :dscCurrentActivityStatus)
                (vec))))

(def status-map {"Approved for production"            :pdo-approved
                 "Decided for production"             :decided-for-production
                 "Producing"                          :producing
                 "Production in clarification phase"  :clarification
                 "Production likely, but unclarified" :likely
                 "Production not evaluated"           :not-evaluated
                 "Shut down"                          :shut-down})

(defn recoverable [{fld-name :fldName dsc-name :dscName} kind]
  (if (and (some? fld-name) (some #{fld-name} reserve/field-names-all))
    (reserve/get-reserve fld-name kind)
    (resource/get-resource dsc-name kind)))

(def rename-field {"7220/8-1 JOHAN CASTBERG" "JOHAN CASTBERG"})

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
                 (map #(update % :name (fn [x] (get rename-field x x))))
                 (group-by :name)
                 (vals)
                 (map #(sort-by (fn [x] (:year x)) %))
                 (map #(first %))
                 (flatten)
                 (remove #(= "TAMBAR ØST" (:name %)))
                 (map #(assoc % :fldRecoverableLiquids (recoverable % :fldRecoverableLiquids)))
                 (map #(assoc % :fldRecoverableOE (recoverable % :fldRecoverableOE)))
                 (map #(assoc % :fldRecoverableGas (recoverable % :fldRecoverableGas)))
                 (map #(dissoc % :fldName :dscName))
                 (filter #(or (some? (:fldRecoverableLiquids %))
                              (some? (:fldRecoverableGas %))))
                 (csv/number-or-throw-columns [:fldRecoverableLiquids :fldRecoverableGas :fldRecoverableOE])
                 (sort-by :name)
                 (vec)))

(def start-year (->> parsed
                     (map :year)
                     (apply min)))

(def stop-year raw-production/max-complete-year)
(def max-complete-year raw-production/max-complete-year)

(def producing-field-names (->> parsed
                                (filter #(= :producing (:status %)))
                                (map :name)
                                (filter #(some #{%} production/field-names))
                                (distinct)
                                (sort)
                                (vec)))

(def shut-down-field-names (->> parsed
                                (filter #(= :shut-down (:status %)))
                                (map :name)
                                (distinct)
                                (sort)
                                (vec)))

; decided for production
; pdo approved
; producing and shut-down
(def reserve-type [:pdo-approved :decided-for-production :producing :shut-down])
(def reserve-field-names (->> parsed
                              (filter #(some #{(:status %)} reserve-type))
                              (map :name)
                              (distinct)
                              (sort)
                              (vec)))

(def top-11-players (conj (->> parsed
                            (filter #(some #{(:name %)} reserve-field-names))
                            (sort-by :fldRecoverableLiquids)
                            (take-last 10)
                            (sort-by :year)
                            (mapv :name))
                          "JOHAN CASTBERG"))

(test/is (= top-11-players
            ["EKOFISK"
             "STATFJORD"
             "VALHALL"
             "GULLFAKS"
             "SNORRE"
             "TROLL"
             "OSEBERG"
             "ÅSGARD"
             "HEIDRUN"
             "JOHAN SVERDRUP"
             "JOHAN CASTBERG"]))

(def missing-field-production
  (->> parsed
       (filter #(some #{(:status %)} [:producing :shut-down]))
       (map :name)
       (distinct)
       (remove #(some #{%} production/field-names))
       (sort)
       (vec)))

(test/is (= ["MARIA"] missing-field-production))

(defn cumulative-original-recoverable-by-status
  [status year kind]
  {:pre [(some #{kind} [:fldRecoverableLiquids :fldRecoverableGas :fldRecoverableOE :liquids :gas :petroleum])
         (some #{status} (vals status-map))]}
  (cond (= :liquids kind)
        (recur status year :fldRecoverableLiquids)

        (= :gas kind)
        (recur status year :fldRecoverableGas)

        (= :petroleum kind)
        (recur status year :fldRecoverableOE)

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
    :decided-for-production (cumulative-original-recoverable-by-status :decided-for-production year kind)
    :clarification (cumulative-original-recoverable-by-status :clarification year kind)
    :likely (cumulative-original-recoverable-by-status :likely year kind)
    :not-evaluated (cumulative-original-recoverable-by-status :not-evaluated year kind)))

(def number-columns [:shut-down-produced :producing-produced
                     :remaining-reserves :pdo-approved :clarification
                     :decided-for-production
                     :likely :not-evaluated])

(def flat-data
  (for [yr (range start-year (inc stop-year))]
    (produce-row-year yr :liquids)))

(def flat-data-petroleum
  (for [yr (range start-year (inc stop-year))]
    (produce-row-year yr :petroleum)))

(def flat-data-gas
  (for [yr (range start-year (inc stop-year))]
    (produce-row-year yr :gas)))

; http://www.npd.no/no/Tema/Ressursregnskap-og-analyser/Temaartikler/Ressursregnskap/2016/
; Totalt olje 7308,3
; Totalt NGL 337,3
; Totalt Kondensat 264,4
;
(def oil-resources-but-not-undiscovered-gb (* 6.29e-3 (- (+ 7308.3 337.3 264.4)
                                                         (+ 1285.0 120.0
                                                            145
                                                            367 14.3 3.4)))) ; 37.5

(def data-factpages-oil (-> (last flat-data)
                            (dissoc :year)
                            (vals)
                            ((fn [x] (* 6.29e-3 (reduce + 0 x)))))) ; 37.49

(def data-factpages-gas (-> (last flat-data-gas)
                            (dissoc :year)
                            (vals)
                            ((fn [x] (* 1 (reduce + 0 x)))))) ; 4279
; (- 6070.2 1465 60 299.5) => 4245.7
; Minus Ikkje oppdaga ressursar,	Utvinning ikkje evaluert (RK 7A) og Betinga ressursar i funn

(def data-factpages-petro (-> (last flat-data-petroleum)
                              (dissoc :year)
                              (vals)
                              ((fn [x] (* 1 (reduce + 0 x)))))) ; 10517
; OD value:(- 14283 (+ 2870 205 697.5))
; => 10510.5))

(defn explode-row [row]
  (reduce (fn [o k]
            (conj o {:year  (:year row)
                     :c     k
                     :value (get row k)}))
          [] number-columns))

(def exploded-data-liquids-gboe
  (->> flat-data
       (mapcat explode-row)
       (map #(update % :value (fn [v] (double (* 6.29e-3 v)))))
       (sort-by :year)
       (vec)))

(def exploded-data-gas-gboe
  (->> flat-data-gas
       (mapcat explode-row)
       (map #(update % :value (fn [v] (double (* 6.29e-3 v)))))
       (sort-by :year)
       (vec)))

(def exploded-data-petroleum-gboe
  (->> flat-data-petroleum
       (mapcat explode-row)
       (map #(update % :value (fn [v] (double (* 6.29e-3 v)))))
       (sort-by :year)
       (vec)))