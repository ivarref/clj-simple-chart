;;; To be honest I find this spectacularly difficult to fetch this data ...
;;; But that's how it goes ... for now.
;;; I'm open to improvements! ==> refsdal.ivar@gmail.com

;;; Human friendly source for table 07022:
;;; https://www.ssb.no/statistikkbanken/SelectVarVal/Define.asp?MainTable=InnbetSkatt2&KortNavnWeb=skatteregn&PLanguage=0&checked=true

(ns clj-simple-chart.ssb.petroskatt
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/07022")
(defonce metadata (client/get url))
(test/is (= 200 (:status metadata)))
(def metadata-body (json/read-str (:body metadata) :key-fn keyword))
(def variables-readable (mapv keyword (mapv :text (:variables metadata-body))))
(test/is (= [:region :skatteart :statistikkvariabel :måned] variables-readable))

(def variables (:variables metadata-body))
(defn show-variable [{code       :code
                      text       :text
                      codes      :values
                      codesTexts :valueTexts}]
  (let [reverse-lookup (zipmap codesTexts codes)
        sorted-codesTexts (sort codesTexts)]
    (doall (map (fn [txt]
                  (println txt "" (get reverse-lookup txt)))
                sorted-codesTexts)))
  ::empty)

(defn get-code-for-variable [variable readable]
  {:pre [(some #{variable} variables-readable)]}
  (let [v (first (filter #(= (name variable) (:text %)) variables))
        codes (:values v)
        codesTexts (:valueTexts v)
        reverse-lookup (zipmap codesTexts codes)]
    (if (= ::none (get reverse-lookup readable ::none))
      (throw (Exception. (str "Could not find " readable " for variable " variable)))
      (get reverse-lookup readable ::none))))

(def skattart ["Ordinær skatt på utvinning av petroleum"
               "Særskatt på utvinning av petroleum"
               "Skatteinngang i alt"])

(def qq [{:code "Region" :selection {:filter "all" :values ["*"]}}
         {:code "Skatteart" :selection {:filter "item" :values (mapv (partial get-code-for-variable :skatteart) skattart)}}
         {:code "ContentsCode" :selection {:filter "item" :values [(get-code-for-variable :statistikkvariabel "Skatt")]}}
         {:code "Tid" :selection {:filter "all" :values ["*"]}}])

(def q {:query qq :response {:format "csv"}})

(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= 200 (:status response)))
(def resp (String. (:body response) "Windows-1252"))
(def parsed (csv/csv-map resp))
(def data (:data parsed))
(def columns (:columns parsed))
(def variable-columns (remove #(.startsWith (name %) "Skatt ") columns))
(def data-columns (filter #(.startsWith (name %) "Skatt ") columns))
(test/is (= '(:region :skatteart) variable-columns))

(defn process-row [{region    :region
                    skatteart :skatteart
                    :as       row}]
  (reduce (fn [o v]
            (conj o {:region    region
                     :skatteart skatteart
                     :dato      (string/replace (subs (name v) 6) "M" "-")
                     :value     (get row v)})) [] data-columns))
(def all-data (flatten (mapv process-row data)))

(def flat-data (filter #(re-matches #"^\d{2} .*?$" (:region %)) all-data))

;;; Start summed data
(def grouped (vals (group-by :dato flat-data)))

(defn sum-over-region [x]
  (let [g (group-by :skatteart x)
        ks (keys g)]
    (reduce (fn [o k]
              (assoc o (keyword k)
                       (reduce + 0 (filter number?
                                           (map read-string (map :value (filter #(= k (:skatteart %)) x))))))) {} ks)))

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

(def twelve-mma-mrd (->> deagg-rows
                         (map-indexed (partial twelve-mma-row deagg-rows))
                         (filter #(= 12 (count (:prev-rows %))))
                         (mapv (partial twelve-mma-contract-row (mapv keyword skattart)))
                         (mapv (fn [row] (reduce (fn [o k]
                                                   (assoc o k (/ (get o k) 1000))) row (map keyword skattart))))
                         (map #(dissoc % :prev-rows))))

(csv/write-csv "data/7022/7022-deagg-summed-mrd-12-mms.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                             :data    (rows-round-str twelve-mma-mrd)})

(def twelve-mma-mrd-yearly-ytd (filter #(or (= (:dato %) (:dato (last twelve-mma-mrd))) (.endsWith (:dato %) "-12")) twelve-mma-mrd))

(csv/write-csv "data/7022/7022-deagg-summed-mrd-yearly-ytd.csv" {:columns (vec (flatten [:dato (mapv keyword skattart)]))
                                                                 :data    (rows-round-str twelve-mma-mrd-yearly-ytd)})


;;; Start grouped by region
(def grouped-by-region (vals (group-by (fn [x] (str (:region x) (:dato x))) flat-data)))

(defn contract-row-region [x]
  (merge {:dato   (:dato (first x))
          :region (:region (first x))}
         (zipmap (map keyword (map :skatteart x)) (map :value x))))

(def output-rows-by-region (->> (mapv contract-row-region grouped-by-region)
                                (sort-by :region)
                                (sort-by :dato)))

(csv/write-csv "data/7022/7022-by-region.csv" {:columns (vec (flatten [:dato :region (mapv keyword skattart)]))
                                               :data    output-rows-by-region})