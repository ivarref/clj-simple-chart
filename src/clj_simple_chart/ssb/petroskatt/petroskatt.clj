;;; To be honest I find this somewhat difficult to fetch this data ...
;;; But that's how it goes ... for now.
;;; I'm open to improvements! ==> refsdal.ivar@gmail.com

;;; Human friendly source for table 07022:
;;; https://www.ssb.no/statistikkbanken/SelectVarVal/Define.asp?MainTable=InnbetSkatt2&KortNavnWeb=skatteregn&PLanguage=0&checked=true

(ns clj-simple-chart.ssb.petroskatt.petroskatt
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]
            [clj-simple-chart.ssb.data.ssb-api :as ssb]
            [clojure.set :as set]
            [clojure.string :as str]))

(def skattart ["Ordinær skatt på utvinning av petroleum"
               "Særskatt på utvinning av petroleum"
               "Skatteinngang i alt"])

(def q {"Region"       "*"
        "Skatteart"    skattart
        "ContentsCode" "Skatt"
        "Tid"          "*"})

(test/is (= [:ContentsCode :Region :Skatteart :dato] (vec (sort (keys (first (ssb/fetch 7022 q)))))))

(def all-data (->> (ssb/fetch 7022 q)
                   (map #(set/rename-keys % {:ContentsCode :value}))))

(def flat-data (filter #(re-matches #"^\d{2} .*?$" (:Region %)) all-data))

;;; Start summed data
(def grouped (vals (group-by :dato flat-data)))

(defn sum-over-region [x]
  (let [g (group-by :Skatteart x)
        ks (keys g)]
    (reduce (fn [o k]
              (assoc o (keyword k)
                       (reduce + 0 (filter number?
                                           (map :value (filter #(= k (:Skatteart %)) x)))))) {} ks)))

(defn contract-row [x]
  (merge {:dato (:dato (first x))}
         (sum-over-region x)))

(def output-rows-numeric (->> (mapv contract-row grouped)
                              (sort-by :dato)))

(defn rows-round-str [rows]
  (mapv (fn [row]
          (reduce (fn [o k] (update o k #(format "%.1f" %))) row
                  (mapv keyword skattart))) rows))

(csv/write-csv "data/7022/7022-summed.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                            :data    (rows-round-str output-rows-numeric)})

;;; Start de-aggregated summed data
(defn deagg-row [all-data idx x]
  (cond (= idx 0) nil
        (.endsWith (:dato x) "-01") x
        :else (merge x
                     (reduce (fn [o p]
                               (assoc o p (- (get x p)
                                             (get (nth all-data (dec idx)) p))))
                             {} (mapv keyword skattart)))))

(def deagg-rows (->> output-rows-numeric
                     (map-indexed (fn [idx x] (deagg-row output-rows-numeric idx x)))
                     (remove nil?)))

(csv/write-csv "data/7022/7022-deagg-summed.csv" {:columns (vec (flatten [:dato (mapv keyword (take 2 skattart))]))
                                                  :data    (rows-round-str deagg-rows)})

;;; Start 12-mma de-agg summed data
(defn twelve-mma-row [all-data idx row]
  (assoc row :prev-rows (take-last 12 (take (inc idx) all-data))))

(defn twelve-mma-contract-row [attrs row]
  (reduce (fn [o k] (assoc o k (reduce + 0 (map k (:prev-rows row))))) row attrs))

(def twelve-mma (->> deagg-rows
                     (map-indexed (partial twelve-mma-row deagg-rows))
                     (filter #(= 12 (count (:prev-rows %))))
                     (mapv (partial twelve-mma-contract-row (mapv keyword skattart)))
                     (map #(dissoc % :prev-rows))))

(csv/write-csv "data/7022/7022-deagg-summed-12-mms.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                         :data    (rows-round-str twelve-mma)})

(def twelve-mma-mrd-monthly (->> deagg-rows
                                 (map-indexed (partial twelve-mma-row deagg-rows))
                                 (filter #(= 12 (count (:prev-rows %))))
                                 (mapv (partial twelve-mma-contract-row (mapv keyword skattart)))
                                 (mapv (fn [row] (reduce (fn [o k]
                                                           (assoc o k (/ (get o k) (* 12 1000)))) row (map keyword skattart))))
                                 (map #(dissoc % :prev-rows))))

(csv/write-csv "data/7022/7022-deagg-summed-mrd-12-mma.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                             :data    (rows-round-str twelve-mma-mrd-monthly)})

(def twelve-mms-mrd (->> deagg-rows
                         (map-indexed (partial twelve-mma-row deagg-rows))
                         (filter #(= 12 (count (:prev-rows %))))
                         (mapv (partial twelve-mma-contract-row (mapv keyword skattart)))
                         (mapv (fn [row] (reduce (fn [o k]
                                                   (assoc o k (/ (get o k) 1000))) row (map keyword skattart))))
                         (map #(dissoc % :prev-rows))))

(def twelve-mms-sum-mrd (->> twelve-mms-mrd
                             (mapv (fn [row]
                                     (assoc row (keyword "Sum innbetalt petroleumsskatt")
                                                (format "%.1f" (reduce (fn [o k] (+ o (get row (keyword k)))) 0 (take 2 skattart))))))))

(csv/write-csv "data/7022/7022-deagg-summed-mrd-12-mms-sum.csv" {:columns (vec (flatten [:dato (keyword "Sum innbetalt petroleumsskatt")]))
                                                                 :data    twelve-mms-sum-mrd})


(csv/write-csv "data/7022/7022-deagg-summed-mrd-12-mms.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                             :data    (rows-round-str twelve-mms-mrd)})

(def twelve-mma-mrd-yearly-ytd (filter #(or (= (:dato %) (:dato (last twelve-mms-mrd))) (.endsWith (:dato %) "-12")) twelve-mms-mrd))

(csv/write-csv "data/7022/7022-deagg-summed-mrd-yearly-ytd.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                                 :data    (rows-round-str twelve-mma-mrd-yearly-ytd)})


;;; Start grouped by region
(def grouped-by-region (vals (group-by (fn [x] (str (:Region x) (:dato x))) flat-data)))

(defn contract-row-region [x]
  (merge {:dato   (:dato (first x))
          :Region (:Region (first x))}
         (zipmap (map keyword (map :Skatteart x)) (map :value x))))

(def output-rows-by-region (->> (mapv contract-row-region grouped-by-region)
                                (sort-by :Region)
                                (sort-by :dato)))

(csv/write-csv "data/7022/7022-by-region.csv" {:columns (vec (flatten [:dato :Region (mapv keyword skattart)]))
                                               :data    output-rows-by-region})