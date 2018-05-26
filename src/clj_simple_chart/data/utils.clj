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

(defn rename-keys-remove-whitespace
  [rows]
  (for [row rows]
    (reduce merge {} (for [[k v] row]
                       {(keyword (str/replace (name k) " " "-")) v}))))

(defn same-rows->single
  [rows]
  (apply merge-with (fn [a b]
                      (cond (and (string? a)
                                 (not= a b)) (throw (ex-info "About to loose data" {:a a :b b}))
                            (and (string? a)
                                 (= a b)) a
                            (number? a) (+ a b)))

                    rows))

; TODO: Detect error case: When numbers will get overwritten (probably not the intention).
(defn contract-by-column [column rows]
  (->> rows
       (group-by column)
       (vals)
       (map same-rows->single)
       (sort-by column)))

(def sample-data
  [{:dato "asdf" :b 100}
   {:dato "asdf" :b 200}])

(test/is (= [{:dato "asdf" :b 300}] (vec (contract-by-column :dato sample-data))))

(defn add-sum-column [rows]
  (map #(assoc % :sum (reduce + 0 (filter number? (vals %)))) rows))

(defn add-relative-share [rows]
  (for [row rows]
    (let [sum (reduce + 0 (filter number? (vals row)))]
      (reduce (fn [o [k v]]
                (if (number? v)
                  (assoc o k (Math/round (double (* 100 (/ v sum)))))
                  (assoc o k v)))
              {}
              row))))

(defn div-by [number rows]
  (for [row rows]
    (reduce (fn [o [k v]]
              (if (number? v)
                (assoc o k (Math/round (double (/ v number))))
                (assoc o k v)))
            {}
            row)))

(defn relative-to-all-time-high [rows]
  (let [max-map (into {} (map (fn [[k v]]
                                (cond (number? v)
                                      [k (apply max (map k rows))]
                                      :else [k v]))
                              (first rows)))]
    (map #(reduce (fn [o [k v]]
                    (cond (number? v) (assoc o k (Math/round (double (* 100 (/ v (get max-map k))))))
                          :else (assoc o k v)))
                  {} %)
         rows)))

(test/is (= (relative-to-all-time-high [{:v 100} {:v 200}])
            [{:v 50} {:v 100}]))

(defn flat->12-mma [rows]
  (map chunks/chunk->moving-average (chunks/rolling-chunks rows 12)))

(defn flat->12-mms [rows]
  (map chunks/chunk->moving-sum (chunks/rolling-chunks rows 12)))

(defn drop-columns [columns data]
  (mapv (fn [x]
          (reduce (fn [o [k v]]
                    (if (some #{k} columns)
                      o
                      (assoc o k v))) {} x)) data))

(defn keep-columns [columns data]
  (mapv (fn [x]
          (reduce (fn [o [k v]]
                    (if (not (some #{k} columns))
                      o
                      (assoc o k v))) {} x)) data))

(defn yoy-change [rows]
  (map-indexed (fn [idx x]
                 (reduce (fn [o [k v]]
                           (cond (number? v) (assoc o k (- v (get (nth rows idx) k)))
                                 :else (assoc o k v)))
                         {} x))
               (rest rows)))

(test/is (= (yoy-change [{:year "2000" :v 10}
                         {:year "2001" :v 15}])
            [{:year "2001" :v 5}]))

(defn keep-nil [row]
  (reduce (fn [o [k v]]
            (or o (nil? k) (nil? v)))
          false
          row))

(test/is (true? (keep-nil {:v 123 :a nil :b 999})))
(test/is (false? (keep-nil {:v 123 :a 333 :b 999})))

(defn keep-nils [rows]
  (filter keep-nil rows))

(defn remove-nils [rows]
  (remove keep-nil rows))

(defn numbers->avg [numbers]
  (/ (reduce + 0 numbers)
     (count numbers)))