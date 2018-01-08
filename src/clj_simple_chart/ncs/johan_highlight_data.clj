(ns clj-simple-chart.ncs.johan-highlight-data
  (:require [clj-simple-chart.ncs.discovery :as discovery]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
            [clojure.test :as test]))

; decided for production
; pdo approved
; producing and shut-down
(def reserve-type [:pdo-approved :decided-for-production :producing :shut-down])
(def reserve-field-names (->> discovery/parsed
                              (filter #(some #{(:status %)} reserve-type))
                              (map :name)
                              (distinct)
                              (sort)
                              (vec)))

(def top-11-players-names (conj (->> discovery/parsed
                                     (filter #(some #{(:name %)} reserve-field-names))
                                     (sort-by :fldRecoverableLiquids)
                                     (take-last 10)
                                     (sort-by :year)
                                     (mapv :name))
                                "JOHAN CASTBERG"))

(def other-fields (->> discovery/parsed
                       (remove #(some #{(:name %)} top-11-players-names))))

(def top-fields (->> discovery/parsed
                     (filter #(some #{(:name %)} top-11-players-names))))

(defn remaining-at-time [flds year kind]
  (let [original-recoverable (->> discovery/parsed
                                  (filter #(some #{(:name %)} flds))
                                  (filter #(>= year (:year %)))
                                  (map (get {:liquids   :fldRecoverableLiquids
                                             :gas       :fldRecoverableGas
                                             :petroleum :fldRecoverableOE} kind))
                                  (reduce + 0))
        produced (production/cumulative-production (filter #(some #{%} production/field-names) flds)
                                                   year
                                                   kind)]
    (- original-recoverable produced)))

(test/is (= top-11-players-names
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
