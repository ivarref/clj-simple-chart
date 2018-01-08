(ns clj-simple-chart.ncs.johan-highlight-data
  (:require [clj-simple-chart.ncs.discovery :as discovery]
            [clj-simple-chart.ncs.production-cumulative-yearly-fields :as production]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csv]))

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
                       (remove #(some #{(:name %)} top-11-players-names))
                       (sort-by :name)))

(def pre-1990 (->> other-fields
                   (filter #(< (:year %) 1990))
                   (sort-by :name)
                   (mapv :name)))

(def post-1990 (->> other-fields
                    (filter #(>= (:year %) 1990))
                    (filter #(< (:year %) 2000))
                    (sort-by :name)
                    (mapv :name)))

(def post-2000 (->> other-fields
                    (filter #(>= (:year %) 2000))
                    (sort-by :name)
                    (mapv :name)))

;(test/is (some #{"GOLIAT"} post-1990))
(test/is (some #{"VEST EKOFISK"} pre-1990))

(def top-fields (->> reserves
                     (filter #(some #{(:name %)} top-11-players-names))))

(def cats ["EKOFISK"
           "STATFJORD"
           "VALHALL"
           "GULLFAKS"
           "SNORRE"
           "TROLL"
           "OSEBERG"
           "ÅSGARD"
           "HEIDRUN"
           "OTHERS_PRE_1990"
           "OTHERS_POSTE_1990"
           "OTHERS_POSTE_2000"
           "JOHAN SVERDRUP"
           "JOHAN CASTBERG"])

(def override-default {"OTHERS_PRE_1990"   pre-1990
                       "OTHERS_POSTE_1990" post-1990
                       "OTHERS_POSTE_2000" post-2000})

(def cat-name-and-flds (mapv (fn [f] [f (get override-default f [f])])
                             cats))

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

(def flat-data-liquids
  (for [yr (range discovery/start-year (inc discovery/stop-year))]
    (reduce (fn [o [v flds]] (assoc o v (remaining-at-time flds yr :liquids)))
            {"YEAR" yr} cat-name-and-flds)))

(defn explode-row [row]
  (reduce (fn [o k]
            (conj o {:year  (get row "YEAR")
                     :c     k
                     :value (get row k)}))
          [] (mapv first cat-name-and-flds)))

(def exploded-data-liquids-gboe
  (->> flat-data-liquids
       (mapcat explode-row)
       (map #(update % :value (fn [v] (double (* 6.29e-3 v)))))
       (sort-by :year)
       (vec)))

(csv/write-csv-format "data/ncs/discovery/johan-highlight.csv"
                      {:columns (vec (cons "YEAR" (mapv first cat-name-and-flds)))
                       :data    flat-data-liquids
                       :format  (reduce (fn [o [k v]] (assoc o k "%.1f")) {} cat-name-and-flds)})