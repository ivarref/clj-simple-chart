(ns clj-simple-chart.ssb.nettokontantstraum
  (:require [clojure.data.json :as json]
            [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ssb.kpi :as kpi]
            [clojure.string :as string]))

(def url "http://data.ssb.no/api/v0/no/table/11013")
(def qq [{:code "ContentsCode" :selection {:filter "all" :values ["*"]}}
         {:code "Tid" :selection {:filter "all" :values ["*"]}}])
(def q {:query qq :response {:format "csv"}})
(defonce response (client/post url {:form-params q :content-type :json :as :byte-array}))
(test/is (= "text/csv; charset=Windows-1252" (get (:headers response) "Content-Type")))

(def resp (String. (:body response) "Windows-1252"))
(def csv-parsed (csv/csv-map resp))
(def columns (:columns csv-parsed))
(def data (:data csv-parsed))

(def sample-column "Skatter på utvinning av petroleum 1991K1")

(defn year-and-quarter [x]
  (cond (keyword? x) (recur (name x))
        (re-matches #"^.+ \d{4}K\d$" x) (last (string/split x #" "))
        :else nil))

(defn prop [x]
  (cond (keyword? x) (recur (name x))
        (re-matches #"^.+ \d{4}K\d$" x) (string/join " " (drop-last (string/split x #" ")))
        :else nil))

(defn process-entry [[k v]]
  {:dato  (year-and-quarter k)
   :prop  (keyword (prop k))
   :value v})

(defn process-grouped [values]
  (reduce (fn [o v]
            (assoc o (:prop v)
                     (read-string (:value v))))
          {:dato (:dato (first values))}
          values))

(def parsed (->> data
                 (mapcat (partial mapv process-entry))
                 (remove #(nil? (:dato %)))
                 (group-by :dato)
                 (vals)
                 (mapv process-grouped)
                 (sort-by :dato)))

(defn entry-to-2016-nok [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  :else (assoc o k (kpi/to-2016-nok (:dato x) v))))
          {}
          x))

(defn entry-to-2017-nok [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  :else (assoc o k (kpi/to-2017-nok (:dato x) v))))
          {}
          x))

(def inflation-adjusted (mapv entry-to-2017-nok parsed))

(def actual-columns (flatten [:dato (mapv keyword (distinct (filter string? (map prop columns))))]))

(csv/write-csv "./data/11013/11013.csv" {:data    parsed
                                         :columns actual-columns})

(defn produce-4-qms [x]
  (reduce (fn [o [k v]]
            (cond (= k :prev-rows) o
                  (= k :dato) (-> o
                                  (assoc :dato v)
                                  (assoc :year (read-string (subs v 0 4))))
                  :else (assoc o k (reduce + 0 (mapv k (:prev-rows x))))))
          {}
          x))

(def four-quarters-moving-sum (->> parsed
                                   (map-indexed (fn [idx x]
                                                  (assoc x :prev-rows (take-last 4 (take (inc idx) parsed)))))
                                   (filter #(= 4 (count (:prev-rows %))))
                                   (mapv produce-4-qms)
                                   (mapv #(dissoc % :prev-rows))))


(def four-quarters-moving-sum-adjusted (->> inflation-adjusted
                                            (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 4 (take (inc idx) inflation-adjusted)))))
                                            (filter #(= 4 (count (:prev-rows %))))
                                            (mapv produce-4-qms)
                                            (mapv #(dissoc % :prev-rows))))

(csv/write-csv "./data/11013/11013-4qms.csv" {:data    four-quarters-moving-sum
                                              :columns actual-columns})

(def four-quarters-moving-sum-eoy
  (->> four-quarters-moving-sum
       (filter #(string/ends-with? (:dato %) "K4"))
       (mapv #(assoc % :year (subs (:dato %) 0 4)))
       (mapv #(assoc % :netto-kontantstraum (get % (keyword "Statens netto kontantstrøm fra petroleumsvirksomhet"))))))

; Statens netto kontantstrøm fra olje- og gassnæringen, som inkluderer direkte inntekter,
; skatter og avgifter samt utbytte fra Statoil,
; øker fra 175 milliarder kroner i år (2017) til 183 milliarder kroner neste år, ifølge regjeringen.
; http://e24.no/makro-og-politikk/statsbudsjettet-2018/regjeringen-tapper-fortsatt-oljefondet-for-aa-budsjettet-til-aa-gaa-opp/24161664

; Statsbudsjettet 2014 https://www.statsbudsjettet.no/Statsbudsjettet-2014/Satsinger/?pid=59880#hopp
; netto 2013 og 2014: 343,9 og 314,1

; Statsbudsjettet 2015 https://www.statsbudsjettet.no/Statsbudsjettet-2015/Satsinger/?pid=65153#hopp
; netto 2014 og 2015: 297,2 og 304,0

; Statsbudsjettet 2016 https://www.statsbudsjettet.no/Statsbudsjettet-2016/Satsinger/?pid=69114#hopp
; netto 2015 og 2016: 217,9 og 204,1

; Statsbudsjettet 2017 https://www.statsbudsjettet.no/Statsbudsjettet-2017/Satsinger/?pid=72986
; netto 2016 og 2017: 124,5 og 138,3

; Statsbudsjettet 2018 https://www.statsbudsjettet.no/Statsbudsjettet-2018/Satsinger/?pid=83808
; netto 2017 og 2018: 175 og 183

(csv/write-csv-format "./data/11013/11013-4qms-eoy.csv" {:data    four-quarters-moving-sum-eoy
                                                         :columns [:year
                                                                   :netto-kontantstraum]
                                                         :format {:netto-kontantstraum "%,7.0f"}})

(defn produce-mrd [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  (= k :year) (assoc o k v)
                  :else (assoc o k (format "%.1f" (double (/ v 1000))))))
          {}
          x))

(defn produce-mrd-numeric [x]
  (reduce (fn [o [k v]]
            (cond (= k :dato) (assoc o k v)
                  (= k :year) (assoc o k v)
                  :else (assoc o k (double (/ v 1000)))))
          {}
          x))

(def four-quarters-moving-sum-adjusted-mrd (mapv produce-mrd-numeric four-quarters-moving-sum-adjusted))

(csv/write-csv "./data/11013/11013-mrd-4qms.csv"
               {:data    (mapv produce-mrd four-quarters-moving-sum)
                :columns actual-columns})

(csv/write-csv "./data/11013/11013-mrd-4qms-2017-NOK.csv"
               {:data    (mapv produce-mrd four-quarters-moving-sum-adjusted)
                :columns actual-columns})
