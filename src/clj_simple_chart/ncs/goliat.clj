(ns clj-simple-chart.ncs.goliat
  (:require [clj-simple-chart.ncs.reserve :as reserve]
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
                           (filter #(> (:fldRecoverableOil %) 0))
                           (map to-mboe)))

(def goliat (first (filter #(= "GOLIAT" (:fldName %)) oil-reserve-data)))

(defn decapitalize-s [[f & rest]]
  (str (.toUpperCase (str f))
       (.toLowerCase (string/join "" rest))))

(defn decapitalize-str [s]
  (let [parts (string/split s #" ")]
    (string/join " " (mapv decapitalize-s parts))))

(defn decapitalize-fieldname [row]
  (update row :fldName
          (fn [fldName]
            (string/replace
              (decapitalize-str fldName)
              "Johan " "J. "))))

(def top-ten (->> oil-reserve-data
                  (sort-by :fldRecoverableOil)
                  (take-last 10)))

(def the-rest (->> oil-reserve-data
                  (sort-by :fldRecoverableOil)
                  (take (- (count oil-reserve-data) 10))))

(def top-ten-plus-goliat (->> oil-reserve-data
                              (sort-by :fldRecoverableOil)
                              (take-last 10)
                              (cons goliat)
                              (sort-by :fldRecoverableOil)
                              (reverse)
                              (mapv decapitalize-fieldname)
                              (vec)))
