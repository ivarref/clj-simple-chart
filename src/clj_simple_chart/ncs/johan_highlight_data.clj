(ns clj-simple-chart.ncs.johan-highlight-data
  (:require [clj-simple-chart.ncs.discovery :as discovery]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
            [clojure.test :as test]))

; decided for production
; pdo approved
; producing and shut-down
(def reserve-type [:pdo-approved :decided-for-production :producing :shut-down])

(def reserves (->> discovery/parsed
                   (filter #(some #{(:status %)} reserve-type))))

(def reserve-field-names (->> reserves
                              (map :name)
                              (distinct)
                              (sort)
                              (vec)))

(def top-11-players-names (conj (->> reserves
                                     (sort-by :fldRecoverableLiquids)
                                     (take-last 10)
                                     (sort-by :year)
                                     (mapv :name))
                                "JOHAN CASTBERG"))

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

(def other-fields (->> reserves
                       (remove #(some #{(:name %)} top-11-players-names))))

(def other-field-names (mapv :name other-fields))

(def top-fields (->> reserves
                     (filter #(some #{(:name %)} top-11-players-names))))

(defn remaining-at-time [flds year kind]
  (let [original-recoverable (->> reserves
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

