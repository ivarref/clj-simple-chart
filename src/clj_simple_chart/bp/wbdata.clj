(ns clj-simple-chart.bp.wbdata
  (:require [clojure.test :as test]
            [clj-http.client :as client]
            [clojure.pprint :refer [pprint]]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.set :as set]))

(def cached-get (memoize client/get))

(def cc2 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-2.json"))))
(def cc3 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-3.json"))))

(def urls {"https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/population.csv" :population
           "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/gdp.csv" :gdp})

(def cc3-to-cc2
  (->> [cc2 cc3]
       (flatten)
       (group-by (fn [x] (get x "name")))
       (vals)
       (mapv #(reduce merge {} %))
       (reduce (fn [o v] (assoc o (get v "alpha-3") (get v "alpha-2"))) {})))

(def bp-country-code-to-name
  {:BP_TNA  "North America"
   :BP_OSCA "Other S. & Cent. America"
   :BP_TSCA "S. & Cent. America"
   :BP_OEE  "Other Europe & Eurasia"
   :BP_TEE  "Europe & Eurasia"
   :BP_OME  "Other Middle East"
   :BP_TME  "Middle East"
   :BP_OECD "OECD"
   :BP_OAP "Other Asia Pacific"
   :BP_TAP "Asia Pacific"
   :BP_WORLD "World"
   :BP_EU2 "Eurozone"
   :BP_NONOECD "Non-OECD"
   :BP_NONOPEC "Non-OPEC"
   :BP_OPEC "OPEC"
   :BP_FSU "Former Soviet Union"
   :BP_TAF "Total Africa"
   :BP_OAF "Other Africa"})

(defn string-country-names [names]
  nil)

(defn cc3-country-names [names]
  nil)

(def bp-group-to-countries
  {:BP_FSU
  (string-country-names "Armenia| Azerbaijan| Belarus| Estonia| Georgia| Kazakhstan| Kyrgyzstan| Latvia| Lithuania| Moldova, Republic of| Russian Federation| Tajikistan| Turkmenistan| Ukraine| Uzbekistan")
   :BP_TAF
  (cc3-country-names "DZA, AGO, SHN, BEN, BWA, BFA, BDI, CMR, CPV, CAF, TCD, COM, COG, DJI, EGY, GNQ, ERI, ETH, GAB, GMB, GHA, GNB, GIN, CIV, KEN, LSO, LBR, LBY, MDG, MWI, MLI, MRT, MUS, MYT, MAR, MOZ, NAM, NER, NGA, STP, REU, RWA, STP, SEN, SYC, SLE, SOM, ZAF, SHN, SDN, SWZ, TZA, TGO, TUN, UGA, COD, ZMB, TZA, ZWE, SSD, COD")
  })

(def cc2-to-name
  (merge (reduce (fn [o v] (assoc o (keyword (get v "alpha-2")) (get v "name"))) {} cc2) bp-country-code-to-name))

(defn parse-url [url prop]
  (let [resp (cached-get url)
        expected-columns [(keyword "Country Code") (keyword "Country Name") :Year :Value]
        csv-map (csv/csv-map (:body resp))]
    (test/is (= 200 (:status resp)))
    (->> csv-map
         (csv/assert-columns expected-columns)
         (:data)
         (csv/read-string-columns [:Year :Value])
         (csv/number-or-nil-columns [:Year :Value])
         (mapv #(set/rename-keys % {:Year :year
                                    (keyword "Country Name") :country
                                    (keyword "Country Code") :country_code
                                    :Value prop})))))

(def all-data (->> urls
                   (mapv #(parse-url (first %) (second %)))
                   (flatten)
                   (group-by (fn [x] [(:year x) (:country x)]))
                   (vals)
                   (mapv #(reduce merge {} %))
                   (mapv #(assoc % :country_code (keyword (get cc3-to-cc2 (:country_code %)))))
                   (group-by (fn [x] [(:country_code x) (:year x)]))
                   (reduce (fn [o [k v]] (assoc o k (first v))) {})))
