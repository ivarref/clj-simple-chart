(ns clj-simple-chart.data.utils
  (:require [clojure.string :as str]
            [clj-simple-chart.data.chunks :as chunks]
            [clojure.test :as test]))

(defn- column-value->column-inner [column row]
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

(defn column-value->column [column rows]
  {:pre [(not (map? rows))]}
  (map (partial column-value->column-inner column) rows))

; TODO: Detect error case: When numbers will get overwritten (probably not the intention).
(defn contract-by-column [column rows]
  (->> rows
       (group-by column)
       (vals)
       (map #(reduce merge {} %))
       (sort-by column)))

(defn add-sum-column [rows]
  (map #(assoc % :sum (reduce + 0 (filter number? (vals %)))) rows))

(defn relative-to-all-time-high [rows]
  (let [max-map (into {} (map (fn [[k v]]
                                (cond (number? v)
                                      [k (apply max (map k rows))]
                                      :else [k v]))
                              (first rows)))]
    (map #(reduce (fn [o [k v]]
                    (cond (number? v) (assoc o k (double (* 100 (/ v (get max-map k)))))
                          :else (assoc o k v)))
                  {} %)
         rows)))

(test/is (= (relative-to-all-time-high [{:v 100} {:v 200}])
            [{:v 50.0} {:v 100.0}]))

(defn flat->12-mma [rows]
  (map chunks/chunk->moving-average (chunks/rolling-chunks rows 12)))

(defn flat->12-mms [rows]
  (map chunks/chunk->moving-sum (chunks/rolling-chunks rows 12)))