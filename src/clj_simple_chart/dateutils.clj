(ns clj-simple-chart.dateutils
  (:require [clojure.string :as string]
            [clojure.edn :as edn])
  (:import (java.time YearMonth)))

(def months ["ignore"
             "januar" "februar" "mars" "april" "mai"
             "juni" "juli" "august" "september" "oktober" "november" "desember"])

(defn months-str [v]
  (let [parts (string/split v #"-0?")]
    (str (nth months (edn/read-string (last parts)))
         " " (first parts))))

(defn year-month [s]
   {:pre [(string? s)]}
   (let [parts (string/split s #"-0?")
         year (edn/read-string (first parts))
         month (edn/read-string (last parts))]
     (YearMonth/of year month)))

(defn date-range
  ([start stop] (date-range [] start stop))
  ([sofar start stop]
   (cond
     (string? start) (recur sofar (year-month start) stop)
     (string? stop) (recur sofar start (year-month stop))
     (.equals start stop)
     (mapv #(format "%04d-%02d" (.getYear %) (.getMonthValue %)) (conj sofar stop))
     :else (date-range (conj sofar start) (.plusMonths start 1) stop))))

(defn prev-12-months [s]
  {:pre  [(string? s)]
   :post [(= 12 (count %))]}
  (let [parts (string/split s #"-0?")
        year (edn/read-string (first parts))
        month (edn/read-string (last parts))]
    (date-range (.minusMonths (YearMonth/of year month) 11)
                (YearMonth/of year month))))

(defn prev-12-months-num-days [s]
  (apply + (mapv #(.lengthOfMonth %) (mapv year-month (prev-12-months s)))))

