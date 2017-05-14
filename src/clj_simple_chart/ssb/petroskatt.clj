;;; To be honest I find this spectacularly difficult to pull this data ...
;;; That's how it goes ... for now.
;;; I'm open to improvements! ==> refsdal.ivar@gmail.com

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
               "Særskatt på utvinning av petroleum"])

(def qq [{:code "Region" :selection {:filter "item" :values [(get-code-for-variable :region "Kontinentalsokkelen")]}}
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

(def flat-data (flatten (mapv process-row data)))
(def grouped (vals (group-by (fn [x] (str (:region x) (:dato x))) flat-data)))

(defn contract-row [x]
  (merge {:dato   (:dato (first x))
          :region (:region (first x))}
         (zipmap (map keyword (map :skatteart x)) (map :value x))))

(def output-rows (->> (mapv contract-row grouped)
                      (sort-by :dato)))
(csv/write-csv "7022.csv" {:columns (vec (flatten [:dato :region (mapv keyword skattart)]))
                           :data    output-rows})