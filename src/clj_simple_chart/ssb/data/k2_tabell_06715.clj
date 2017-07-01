(ns clj-simple-chart.ssb.data.k2-tabell-06715
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/06715")
(defonce metadata (client/get url))
(test/is (= 200 (:status metadata)))
(def metadata-body (json/read-str (:body metadata) :key-fn keyword))
(def variables-readable (mapv keyword (mapv :text (:variables metadata-body))))
(test/is (= [:valuta :låntakersektor :statistikkvariabel :måned] variables-readable))

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

(defn code [variable]
  {:pre [(some #{variable} variables-readable)]}
  (->> variables
       (filter #(= (name variable) (:text %)))
       (first)
       (:code)))

(def qq [{:code (code :valuta) :selection {:filter "item" :values [(get-code-for-variable :valuta "Norske kr")]}}
         {:code (code :låntakersektor) :selection {:filter "item" :values [(get-code-for-variable :låntakersektor "Husholdninger mv.")]}}
         {:code (code :statistikkvariabel) :selection {:filter "item" :values [(get-code-for-variable :statistikkvariabel "Innenlandsk bruttogjeld")]}}
         {:code (code :måned) :selection {:filter "all" :values ["*"]}}])

(def q {:query qq :response {:format "csv"}})

(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= 200 (:status response)))
(test/is (= "text/csv; charset=Windows-1252" (-> response :headers (get "Content-Type"))))
(def resp (String. (:body response) "Windows-1252"))
(def parsed (csv/csv-map resp))
(def data (:data parsed))
(def columns (:columns parsed))

(def columns-non-value (->> columns (remove #(.startsWith (name %) "Innenlandsk bruttogjeld"))))

(test/is (= 1 (count data)))
(test/is (= 2 (count columns-non-value)))

(defn make-row [{valuta          :valuta
                 lånetakersektor :låntakersektor
                 :as             row}]
  (reduce (fn [o [k v]]
            (if (some #{k} columns-non-value)
              o
              (conj o {:valuta         valuta
                       :låntakersektor lånetakersektor
                       :time           (string/join (take-last 7 (name k)))
                       :value          (read-string v)}))) [] row))

(def parsed-data (->> data
                      (mapv make-row)
                      (flatten)
                      (sort-by :time)))
