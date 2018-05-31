(ns clj-simple-chart.data.chunks
  (:require [clojure.test :as test]))

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

(defn chunk->moving-sum [chunk]
  (into {} (map (fn [[k v]]
                  (cond (number? v) [k (reduce + 0.0 (map #(or (k %) 0) chunk))]
                        (string? v) [k v]
                        (keyword? v) [k v]
                        :else (throw (ex-info "Unhandled value type" {:value v :chunk chunk}))))
                (last chunk))))

(defn chunk->moving-average [chunk]
  (into {} (map (fn [[k v]]
                  (cond (number? v) [k (double (/ (reduce + 0.0 (map k chunk)) (count chunk)))]
                        (string? v) [k v]
                        (keyword? v) [k v]
                        :else (throw (ex-info "Unhandled value type" {:value v :chunk chunk}))))
                (last chunk))))

(test/is (= (chunk->moving-sum [{:dato "first" :v 100}
                                {:dato "second" :v 33}
                                {:dato "last" :v 200}])
            {:dato "last" :v 333.0}))

(test/is (= (chunk->moving-average [{:dato "first" :v 25}
                                    {:dato "second" :v 40}
                                    {:dato "third" :v 60}
                                    {:dato "last" :v 75}])
            {:dato "last" :v 50.0}))