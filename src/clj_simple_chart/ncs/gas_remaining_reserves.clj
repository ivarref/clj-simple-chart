(ns clj-simple-chart.ncs.gas-remaining-reserves
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

(def top-10-players-names (conj (->> reserves
                                     (sort-by :gas)
                                     (take-last 10)
                                     (sort-by :year)
                                     (mapv :name))))

(test/is (= top-10-players-names
            ["EKOFISK" "FRIGG" "SLEIPNER VEST" "GULLFAKS SØR" "OSEBERG" "TROLL" "ÅSGARD" "SNØHVIT" "KVITEBJØRN" "ORMEN LANGE"]))

(def other-fields (->> reserves
                       (remove #(some #{(:name %)} top-10-players-names))
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
                     (filter #(some #{(:name %)} top-10-players-names))))

(def cats (vec (flatten [top-10-players-names
                         "OTHERS_PRE_1990"
                         "OTHERS_POSTE_1990"
                         "OTHERS_POSTE_2000"])))

(def override-default {"OTHERS_PRE_1990"   pre-1990
                       "OTHERS_POSTE_1990" post-1990
                       "OTHERS_POSTE_2000" post-2000})

(def cat-name-and-flds (mapv (fn [f] [f (get override-default f [f])])
                             cats))

(defn remaining-at-time [flds year kind]
  (let [original-recoverable (->> reserves
                                  (filter #(some #{(:name %)} flds))
                                  (filter #(>= year (:year %)))
                                  (map kind)
                                  (reduce + 0))
        produced (production/cumulative-production (filter #(some #{%} production/field-names) flds)
                                                   year
                                                   kind)]
    (- original-recoverable produced)))

(def flat-data-gas
  (for [yr (range discovery/start-year (inc discovery/stop-year))]
    (reduce (fn [o [v flds]] (assoc o v (remaining-at-time flds yr :gas)))
            {"YEAR" yr} cat-name-and-flds)))

(defn explode-row [row]
  (reduce (fn [o k]
            (conj o {:year  (get row "YEAR")
                     :c     k
                     :value (get row k)}))
          [] (mapv first cat-name-and-flds)))

(def exploded-data-gas-gboe
  (->> flat-data-gas
       (mapcat explode-row)
       (map #(update % :value (fn [v] (double (* 6.29e-3 v)))))
       (sort-by :year)
       (vec)))

(csv/write-csv-format "data/ncs/discovery/remaining-reserves-gas.csv"
                      {:columns (vec (cons "YEAR" (mapv first cat-name-and-flds)))
                       :data    flat-data-gas
                       :format  (reduce (fn [o [k v]] (assoc o k "%.1f")) {} cat-name-and-flds)})