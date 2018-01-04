(ns clj-simple-chart.ncs.production-cumulative-yearly-fields
  (:require [clj-simple-chart.csv.csvmap :as csv]
            [clj-http.client :as client]
            [clojure.test :as test]
            [clojure.string :as str]))

(def url "http://factpages.npd.no/ReportServer?/FactPages/TableView/field_production_yearly&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=81.191.112.135&CultureCode=en")

(defonce raw (-> url
                 (client/get)
                 (:body)
                 (csv/csv-map)))

(def columns (:columns raw))
(def data (:data raw))

(test/is (= [:prfInformationCarrier
             :prfYear
             :prfPrdOilNetMillSm3
             :prfPrdGasNetBillSm3
             :prfPrdNGLNetMillSm3
             :prfPrdCondensateNetMillSm3
             :prfPrdOeNetMillSm3
             :prfPrdProducedWaterInFieldMillSm3
             :prfNpdidInformationCarrier]
            columns))

(def bad-data (->> data
                   (filter #(str/starts-with? (:prfPrdNGLNetMillSm3 %) "("))))
(test/is (= 1 (count bad-data)))

(defn parens-to-negative [v]
  (if (str/starts-with? v "(")
    (str "-" (str/replace v #"\(|\)" ""))
    v))

(def parsed (->> data
                 (map #(update % :prfPrdNGLNetMillSm3 parens-to-negative))
                 (map #(update % :prfPrdOilNetMillSm3 parens-to-negative))
                 (csv/read-number-or-throw-columns [:prfYear
                                                    :prfPrdOilNetMillSm3
                                                    :prfPrdGasNetBillSm3
                                                    :prfPrdNGLNetMillSm3
                                                    :prfPrdCondensateNetMillSm3
                                                    :prfPrdOeNetMillSm3])
                 (map #(assoc % :prfPrdLiquidsNetMillSm3
                                (+ (:prfPrdOilNetMillSm3 %)
                                   (:prfPrdNGLNetMillSm3 %)
                                   (:prfPrdCondensateNetMillSm3 %))))))

(def start-year (->> parsed
                     (map :prfYear)
                     (apply min)))

(def stop-year (->> parsed
                    (map :prfYear)
                    (apply max)))

(def field-names (->> (map :prfInformationCarrier parsed)
                      (distinct)
                      (sort)
                      (vec)))

(defn yearly-production
  [fields year kind]
  {:pre [(every? #(some #{%} field-names) fields)
         (pos? year)
         (some #{kind} [:prfPrdLiquidsNetMillSm3 :prfPrdGasNetBillSm3 :prfPrdOeNetMillSm3 :liquids :gas :petroleum])]}
  (cond (= kind :liquids)
        (recur fields year :prfPrdLiquidsNetMillSm3)

        (= kind :gas)
        (recur fields year :prfPrdGasNetBillSm3)

        (= kind :petroleum)
        (recur fields year :prfPrdOeNetMillSm3)

        :else (->> parsed
                   (filter #(= (:prfYear %) year))
                   (filter #(some #{(:prfInformationCarrier %)} fields))
                   (map kind)
                   (reduce + 0)
                   (double))))

(defn cumulative-production
  [fields year kind]
  {:pre [(every? #(some #{%} field-names) fields)
         (pos? year)
         (some #{kind} [:prfPrdLiquidsNetMillSm3 :prfPrdGasNetBillSm3 :prfPrdOeNetMillSm3 :liquids :gas :petroleum])]}
  (cond (= kind :liquids)
        (recur fields year :prfPrdLiquidsNetMillSm3)

        (= kind :gas)
        (recur fields year :prfPrdGasNetBillSm3)

        (= kind :petroleum)
        (recur fields year :prfPrdOeNetMillSm3)

        :else (->> parsed
                   (filter #(<= (:prfYear %) year))
                   (filter #(some #{(:prfInformationCarrier %)} fields))
                   (map kind)
                   (reduce + 0)
                   (double))))

(test/is (= (Math/round 4650.93)
            ; This value 4650.93 is from
            ; Sokkelåret 2016 Excel Sheet Figur 2-1
            ; Summing column I (without forecast)
            (Math/round (cumulative-production field-names 2016 :liquids))))

(test/is (= (Math/round 2193.00)
            ; This value 2193.00 (2188.14) is from
            ; Sokkelåret 2016 Excel Sheet Figur 2-1
            ; Summing column G (without forecast)
            (Math/round (cumulative-production field-names 2016 :gas))))

(test/is (< (cumulative-production ["STATFJORD"] 1990 :prfPrdLiquidsNetMillSm3)
            (cumulative-production ["STATFJORD"] 2000 :prfPrdLiquidsNetMillSm3)))