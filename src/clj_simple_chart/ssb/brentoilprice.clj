(ns clj-simple-chart.ssb.brentoilprice
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [clj-simple-chart.csv.csvmap :as csv]
            [clj-simple-chart.ssb.valutakurser :as valutakurser]
            [clojure.set :as set]
            [clojure.test :as test])
  (:import (org.eclipse.jetty.util UrlEncoded MultiMap)))

(defn parse-query-string [query]
  (let [params (MultiMap.)]
    (UrlEncoded/decodeTo query params "UTF-8")
    (into {} params)))

; Original URL: https://fred.stlouisfed.org/series/DCOILBRENTEU

(def sample-url "chart_type=line&recession_bars=on&log_scales=&bgcolor=%23e1e9f0&graph_bgcolor=%23ffffff&fo=Open+Sans&ts=12&tts=12&txtcolor=%23444444&show_legend=yes&show_axis_titles=yes&drp=0&cosd=2012-04-02&coed=2017-04-01&height=450&stacking=&range=Custom&mode=fred&id=DCOILBRENTEU&transformation=lin&nd=1987-05-20&ost=-99999&oet=99999&lsv=&lev=&mma=0&fml=a&fgst=lin&fgsnd=2009-06-01&fq=Monthly&fam=avg&vintage_date=&revision_date=&line_color=%234572a7&line_style=solid&lw=2&scale=left&mark_type=none&mw=2&width=1168")
(def parsed (parse-query-string sample-url))
(def p (zipmap (keys parsed) (map first (vals parsed))))

(def last-month (time/minus- (time/now) (time/months 1)))

(def pp (-> p
            (assoc "cosd" "1987-06-01")
            (assoc "coed" (str (time/year last-month)
                               "-"
                               (format "%02d" (time/month last-month))
                               "-01"))))

(def url "https://fred.stlouisfed.org/graph/fredgraph.csv")
(defonce resp (client/get url {:query-params pp}))

(test/is (= 200 (:status resp)))
(test/is (= [:DATE :DCOILBRENTEU] (:columns (csv/csv-map (:body resp)))))

(def raw-data (->> (csv/csv-map (:body resp))
                   :data
                   (mapv #(set/rename-keys % {:DATE :dato :DCOILBRENTEU :usd}))
                   (mapv #(update % :dato (fn [x] (subs x 0 7))))
                   (sort-by :dato)))

(def columns [:dato :usd])

(csv/write-csv "./data/brent_monthly_usd.csv" {:columns columns
                                               :data    raw-data})

(def brent-numeric (->> raw-data
                        (mapv #(update % :usd read-string))))
(test/is (every? number? (map :usd brent-numeric)))

(def brent-12-mma
  (->> brent-numeric
       (map-indexed (fn [idx x] (assoc x :prev-rows (take-last 12 (take (inc idx) brent-numeric)))))
       (filter #(= 12 (count (:prev-rows %))))
       (mapv #(assoc % :usd (/ (reduce + 0 (mapv :usd (:prev-rows %))) 12)))
       (mapv #(dissoc % :prev-rows))))

(def brent-12-mma-dato-to-usd (reduce (fn [o v] (assoc o (:dato v) (:usd v))) {} brent-12-mma))

(def one-usd-date-to-nok valutakurser/one-usd-date-to-nok)

(def brent-12-mma-dato-to-nok (reduce (fn [o v] (assoc o (:dato v) (* (get one-usd-date-to-nok (:dato v))
                                                                      (:usd v)))) {} brent-12-mma))
