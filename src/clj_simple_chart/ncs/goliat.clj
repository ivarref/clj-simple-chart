(ns clj-simple-chart.ncs.goliat
  (:require [clj-simple-chart.ncs.reserve :as reserve]
            [camel-snake-kebab.core :as snake]
            [clojure.string :as string]))

(def keep-fields [:fldName :fldRecoverableOil])

(defn keep-fields-fn [row]
  (reduce (fn [o k]
            (if (some #{k} keep-fields)
              o
              (dissoc o k))) row (keys row)))

(defn to-mboe [row]
  (update row :fldRecoverableOil #(Math/round (* 6.29 %))))

(def oil-reserve-data (->> reserve/data-parsed
                           (map keep-fields-fn)
                           (map to-mboe)))

(def goliat (first (filter #(= "GOLIAT" (:fldName %)) oil-reserve-data)))

(defn decapitalize-fieldname [row]
  (update row :fldName
          (fn [fldName]
            (string/replace
              (snake/->Camel_Snake_Case fldName)
              "_" " "))))

(def top-ten-plus-goliat (->> oil-reserve-data
                              (sort-by :fldRecoverableOil)
                              (take-last 10)
                              (cons goliat)
                              (sort-by :fldRecoverableOil)
                              (reverse)
                              (mapv decapitalize-fieldname)
                              (vec)))