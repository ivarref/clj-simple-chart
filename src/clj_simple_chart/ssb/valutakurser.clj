(ns clj-simple-chart.ssb.valutakurser
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.test :as test]
            [hickory.core :as hickory]
            [clojure.edn :as edn]))

(defonce exchange-rate-page (client/get "http://www.norges-bank.no/en/Statistics/exchange_rates/"))
(test/is (= 200 (:status exchange-rate-page)))
(def valuta-url (->> exchange-rate-page
                     :body
                     (hickory/parse)
                     (hickory/as-hiccup)
                     (second)
                     (tree-seq coll? identity)
                     (filter vector?)
                     (filter #(>= (count %) 2))
                     (filter #(= :a (first %)))
                     (mapv second)
                     (mapv :href)
                     (remove nil?)
                     (filter #(.contains % "valuta_mnd.csv"))
                     (first)))

(defonce resp (client/get valuta-url))
(test/is (= 200 (:status resp)))

(def expected-columns ["Date" "1 AUD" "100 BGN" "1 BRL" "1 CAD" "100 CHF" "100 CNY" "100 CZK"
                       "100 DKK" "1 EUR" "1 GBP" "1 HKD" "100 HRK" "100 HUF" " I44" "100 IDR"
                       "1 ILS" "100 INR" "100 JPY" "100 KRW" "1 LTL" "100 MXN" "1 MYR" "1 NZD"
                       "100 PHP" "100 PKR" "1 PLN" "100 RON" "100 RUB" "100 SEK" "1 SGD"
                       "100 THB" "100 TRY" "100 TWD" " TWI" "1 USD" "1 XDR" "1 ZAR" "100 BYR"
                       "1 BYN"])

(def columns (:columns (csv/csv-map-assert-columns (:body resp) expected-columns)))
(def data (:data (csv/csv-map-assert-columns (:body resp) expected-columns)))

(test/is (= "Jan-60" (:Date (first data))))

(def month-to-num (zipmap ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
                          [1 2 3 4 5 6 7 8 9 10 11 12]))

(defn get-date [date]
  (let [month (subs date 0 3)
        month-numeric (format "%02d" (get month-to-num month))
        yr (subs date 4 6)
        yr (if (.startsWith yr "0") (subs yr 1 2) yr)
        yr-two-digit (edn/read-string yr)
        yr (if (< yr-two-digit 60) (str "20" (format "%02d" yr-two-digit))
                                   (str "19" (format "%02d" yr-two-digit)))]
    (str yr "-" month-numeric)))

(test/is (= "1960-01" (get-date "Jan-60")))
(test/is (= "2000-01" (get-date "Jan-00")))
(test/is (= "2008-01" (get-date "Jan-08")))
(test/is (= "2017-04" (get-date "Apr-17")))

(def parsed (mapv (fn [x] (assoc x :Date (get-date (:Date x)))) data))

(def one-usd-numeric (->> parsed
                          (mapv (fn [x] {:dato (:Date x)
                                         :usd  (edn/read-string (get x (keyword "1 USD")))}))
                          (sort-by :dato)))

(def one-usd-numeric-12-mma
  (->> one-usd-numeric
       (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 12 (take (inc idx) one-usd-numeric)))))
       (filter #(= 12 (count (:prev-rows %))))
       (mapv #(assoc % :usd (/ (reduce + 0 (mapv :usd (:prev-rows %))) 12)))
       (mapv #(dissoc % :prev-rows))))

(def one-usd-date-to-nok (reduce (fn [o v] (assoc o (:dato v) (:usd v))) {} one-usd-numeric-12-mma))

(csv/write-csv "./data/valuta_monthly.csv" {:columns columns :data parsed})