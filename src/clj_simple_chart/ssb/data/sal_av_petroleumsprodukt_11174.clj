(ns clj-simple-chart.ssb.data.sal-av-petroleumsprodukt-11174
  (:require [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as str]))

; 11174: Salg av petroleumsprodukter, etter kjøpegruppe og produkttype (mill. liter). Foreløpige tall (F) 2010M01 - 2017M12
;
; https://www.ssb.no/statbank/table/11174?rxid=49a52ff4-5d3c-4264-aa49-95134312070d

(def raw-data (ssb/fetch 11174 {"ContentsCode"  "Salg"
                                "Region"        "Hele landet"
                                "PetroleumProd" ["Autodiesel" "Bilbensin"]
                                "Kjopegrupper"  "Alle kjøpegrupper"
                                "Tid"           "*"}))

(test/is (= [:kjøpegruppe :petroleumsprodukt :region :salg] (:columns raw-data)))

(defn column-value->column [column row]
  {:pre [(map? row)
         (keyword? column)]}
  (when-not (= 1 (count (filter number? (vals row))))
    (throw (ex-info "Expected a single value with type number" {:row    row
                                                                :column column})))
  (reduce (fn [o [k v]]
            (cond (= k column) o
                  (number? v) (assoc o (keyword (str/lower-case (get row column))) v)
                  :else (assoc o k v)))
          {}
          row))

(defn contract-by-column [column rows]
  (->> rows
       (group-by column)
       (vals)
       (map #(reduce merge {} %))
       (sort-by column)))

(defn- chunks-inner
  [rows accum n]
  {:pre [(vector? accum)]}
  (cond (empty? rows) (if (= n (count accum)) [accum] [])
        (= n (count accum)) (lazy-seq (cons accum
                                            (chunks-inner (rest rows)
                                                          (conj (vec (drop 1 accum)) (first rows))
                                                          n)))
        :else (recur (rest rows)
                     (conj accum (first rows))
                     n)))

(defn chunks [rows n]
  {:pre [(not (map? rows))
         (coll? rows)
         (pos-int? n)]}
  (chunks-inner rows [] n))

(test/is (= '([:a] [:b] [:c] [:d] [:e]) (chunks [:a :b :c :d :e] 1)))
(test/is (= '([:a :b] [:b :c] [:c :d] [:d :e]) (chunks [:a :b :c :d :e] 2)))
(test/is (= '([:a :b :c] [:b :c :d] [:c :d :e]) (chunks [:a :b :c :d :e] 3)))

(def data (->> (:data raw-data)
               (map #(dissoc % :kjøpegruppe))
               (map #(dissoc % :region))
               (csv/number-or-throw-columns [:salg])
               (map (partial column-value->column :petroleumsprodukt))
               (contract-by-column :dato)))

(test/is (= "2010-01" (:dato (first data))))