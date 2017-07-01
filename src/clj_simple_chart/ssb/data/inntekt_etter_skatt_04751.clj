(ns clj-simple-chart.ssb.data.inntekt-etter-skatt-04751
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/04751")
(defonce metadata (client/get url))
(test/is (= 200 (:status metadata)))
(def metadata-body (json/read-str (:body metadata) :key-fn keyword))
(def variables-readable (mapv keyword (mapv :text (:variables metadata-body))))
(test/is (= [:hushaldningstype :statistikkvariabel :år] variables-readable))

(def variables (:variables metadata-body))

(defn show-variable [variable]
  {:pre [(some #{variable} variables-readable)]}
  (let [full-variable (->> (:variables metadata-body) (filter #(= (:text %) (name variable))) (first))
        codesTexts (:valueTexts full-variable)
        codes (:values full-variable)
        reverse-lookup (zipmap codesTexts codes)
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

(defn code [variable]
  {:pre [(some #{variable} variables-readable)]}
  (->> variables
       (filter #(= (name variable) (:text %)))
       (first)
       (:code)))

(def qq [{:code (code :hushaldningstype) :selection {:filter "item" :values [(get-code-for-variable :hushaldningstype "Alle hushald")]}}
         {:code (code :statistikkvariabel) :selection {:filter "item" :values [(get-code-for-variable :statistikkvariabel "Inntekt etter skatt (kr)")]}}
         {:code (code :år) :selection {:filter "all" :values ["*"]}}])

(def q {:query qq :response {:format "csv"}})

(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= 200 (:status response)))
(test/is (= "text/csv; charset=Windows-1252" (-> response :headers (get "Content-Type"))))
(def resp (String. (:body response) "Windows-1252"))
(def parsed (csv/csv-map resp))
(def data (:data parsed))
(def columns (:columns parsed))

(def columns-non-value (->> columns (remove #(.startsWith (name %) "Inntekt etter skatt"))))

(test/is (= 1 (count data)))
(test/is (= 1 (count columns-non-value)))

(defn make-row [{hushaldningstype :hushaldningstype
                 :as              row}]
  (reduce (fn [o [k v]]
            (if (some #{k} columns-non-value)
              o
              (conj o {:hushaldningstype hushaldningstype
                       :år (string/join (take-last 4 (name k)))
                       :value (read-string v)}))) [] row))

(def parsed-data (->> data
                      (mapv make-row)
                      (flatten)
                      (sort-by :år)))
