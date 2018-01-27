(ns clj-simple-chart.data.utils
  (:require [clojure.string :as str]
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

(defn- rolling-chunks-inner
  [rows acc n]
  {:pre [(vector? acc)]}
  (cond (empty? rows) (if (= n (count acc)) [acc] [])
        (= n (count acc)) (lazy-seq (cons acc
                                            (rolling-chunks-inner (rest rows)
                                                                  (conj (vec (drop 1 acc)) (first rows))
                                                                  n)))
        :else (recur (rest rows)
                     (conj acc (first rows))
                     n)))

(defn rolling-chunks [rows n]
  {:pre [(not (map? rows))
         (coll? rows)
         (pos-int? n)]}
  (rolling-chunks-inner rows [] n))

(test/is (= '([:a] [:b] [:c] [:d] [:e]) (rolling-chunks [:a :b :c :d :e] 1)))
(test/is (= '([:a :b] [:b :c] [:c :d] [:d :e]) (rolling-chunks [:a :b :c :d :e] 2)))
(test/is (= '([:a :b :c] [:b :c :d] [:c :d :e]) (rolling-chunks [:a :b :c :d :e] 3)))
