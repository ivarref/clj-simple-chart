(ns clj-simple-chart.bp.wb-raw
  (:require [clj-http.client :as client]
            [clojure.test :as test]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.data.json :as json]))

(def urls {"SP.POP.TOTL"    :population
           "NY.GDP.MKTP.CD" :gdp
           "NE.IMP.GNFS.ZS" :imports-of-goods-and-services-percentage
           })

(def translate-countries
  {"Russian Federation"                "Russia"
   "Venezuela, Bolivarian Republic of" "Venezuela"
   "United Arab Emirates"              "UAE"
   "Iran, Islamic Republic of"         "Iran"
   "Brunei Darussalam"                 "Brunei"
   "Equatorial Guinea"                 "Eq. Guinea"
   "Trinidad and Tobago"               "Trinidad & T."
   "Bolivia, Plurinational State of"   "Bolivia"
   "Viet Nam"                          "Vietnam"
   "Korea, Republic of"                "S. Korea"
   "Moldova, Republic of"              "Moldova"
   })

(def bp-country-code-to-name
  {:BP_TNA     "North America"
   :BP_OSCA    "Other S. & Cent. America"
   :BP_TSCA    "S. & Cent. America"
   :BP_OEE     "Other Europe & Eurasia"
   :BP_TEE     "Europe & Eurasia"
   :BP_OME     "Other Middle East"
   :BP_TME     "Middle East"
   :BP_OECD    "OECD"
   :BP_OAP     "Other Asia Pacific"
   :BP_TAP     "Asia Pacific"
   :BP_WORLD   "World"
   :BP_EU2     "Eurozone"
   :BP_NONOECD "Non-OECD"
   :BP_NONOPEC "Non-OPEC"
   :BP_OPEC    "OPEC"
   :BP_FSU     "Former Soviet Union"
   :BP_TAF     "Total Africa"
   :BP_OAF     "Other Africa"})

(def cached-get (memoize client/get))

(def cc2 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-2.json"))))
(def cc3 (json/read-str (:body (cached-get "https://raw.githubusercontent.com/ivarref/bp-diagrams/master/data/slim-3.json"))))

(def cc3-to-cc2
  (->> [cc2 cc3]
       (flatten)
       (group-by (fn [x] (get x "name")))
       (vals)
       (mapv #(reduce merge {} %))
       (reduce (fn [o v] (assoc o (get v "alpha-3") (get v "alpha-2"))) {})))

(def cc2-to-name
  (merge
    (reduce (fn [o v] (assoc o (keyword (get v "alpha-2"))
                               (get translate-countries (get v "name") (get v "name")))) {} cc2)
    bp-country-code-to-name))

(def country-name-kw (keyword "Country Name"))
(def country-code-kw (keyword "Country Code"))

(defn parse-country-record [prop c]
  (let [country (get c country-name-kw)
        country-code (get c country-code-kw)]
    (reduce (fn [o [k v]]
              (if (or (= k country-name-kw)
                      (= k country-code-kw)
                      (zero? (count (name k)))
                      (= "" v))
                o
                (let [year-value (read-string (name k))]
                  (assert (integer? year-value) (str "Expected column/year to be integer, was >" year-value "<"))
                  (conj o {:country      country
                           :country_code (keyword (get cc3-to-cc2 country-code))
                           :year         year-value
                           prop          (read-string v)})))) [] c)))

(defn parse-url [indicator prop]
  (let [resp (cached-get (str "http://api.worldbank.org/countries/all/indicators/" indicator "?format=csv"))
        expected-columns [country-code-kw country-name-kw "1990" "2015"]
        csv-map (csv/csv-map (:body resp))]
    (spit (str "./data/wb/" indicator ".csv") (csv/debomify (:body resp)))
    (test/is (= 200 (:status resp)))
    (->> csv-map
         (csv/assert-columns expected-columns)
         (:data)
         (mapv (partial parse-country-record prop))
         (flatten))))

(def all-data (->> urls
                   (mapv #(parse-url (first %) (second %)))
                   (flatten)
                   (group-by (fn [x] [(:year x) (:country x)]))
                   (vals)
                   (mapv #(reduce merge {} %))
                   (mapv #(assoc % :country (get cc2-to-name (:country_code %))))
                   (group-by (fn [x] [(:country_code x) (:year x)]))
                   (reduce (fn [o [k v]] (assoc o k (first v))) {})))
