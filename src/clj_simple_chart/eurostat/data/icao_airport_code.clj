(ns clj-simple-chart.eurostat.data.icao-airport-code
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csvmap]))

(def url "https://raw.githubusercontent.com/datasets/airport-codes/master/data/airport-codes.csv")

(defonce response (client/get url))
(def csv (csvmap/csv-map (:body response)))

(csvmap/assert-columns [:ident
                        :type
                        :name
                        :coordinates
                        :elevation_ft
                        :continent
                        :iso_country
                        :iso_region
                        :municipality
                        :gps_code
                        :iata_code
                        :local_code] csv)

(def data (:data csv))

(def parsed (->> data
                 (map #(update % :iso_country (fn [c] (if (some #{c} ["IM" "GG" "GB" "JE"])
                                                          "UK"
                                                          c))))
                 (map #(assoc % :code (str (:iso_country %) "_" (:ident %))))
                 (map (juxt :code :municipality))
                 (into {})))

(def new-york (->> data
                   (filter #(= "New York" (:municipality %)))))

(def london (->> data
                 (filter #(= "London" (:municipality %)))))

(def codes (merge parsed
                  {"QA_OTHH" "Doha"
                   "NO_ENGM" "Oslo"
                   "NO_ENBR" "Bergen"                              ; Flesland
                   "NO_ENVA" "Trondheim"                           ; Værnes
                   "NO_ENZV" "Stavanger"                           ; Sola
                   "DK_EKCH" "København"
                   "DK_EKBI" "Billund"
                   "DK_EKAH" "Aarhus"
                   "SE_ESSA" "Stockholm"                           ; Arlanda
                   "SE_ESKN" "Stockholm"                           ; Skavsta
                   "SE_ESGG" "Göteborg"
                   "NO_ENTC" "Tromsø"                              ;  lufthavn, Langnes
                   "NO_ENBO" "Bodø"                                ; lufthavn
                   "NO_ENAL" "Ålesund"                             ;  lufthavn, Vigra
                   "UK_EGLL" "London"                              ;  Heathrow lufthavn
                   "UK_EGSS" "London"                              ;  London Stansted lufthavn  
                   "UK_EGKK" "London"                              ;  London Gatwick lufthavn  
                   "NL_EHAM" "Amsterdam"                           ;  lufthavn, Schiphol
                   "BE_EBBR" "Brussel"
                   "NO_ENEV" "Harstad"
                   "NO_ENCN" "Kristiansand"
                   "US_KJFK" "New York"                            ;NO_ENGM_US_KJFK

                   "DE_EDDF" "Frankfurt"
                   "DE_EDFH" "Frankfurt"                           ; Frankfurt-Hahn
                   "DE_EDDH" "Hamburg"
                   "DE_EDDL" "Düsseldorf"
                   "DE_EDDT" "Berlin"                              ; Berlin Tegel
                   "DE_EDDB" "Berlin"                              ; Berlin Schønefeld
                   "DE_EDDW" "Bremen"
                   "DE_EDDM" "München"
                   "DE_EDJA" "Memmingen"
                   "DE_EDLV" "Weeze"                               ; https://en.wikipedia.org/wiki/Weeze_Airport

                   "NO_ENHD" "Haugesund"
                   "NO_ENMS" "Mosjøen"

                   "FI_EFHK" "Helsinki"

                   "FR_LFPG" "Paris"

                   "ES_LEAL" "Alicante"
                   "ES_GCLP" "Gran Canaria"
                   "ES_LEBL" "Barcelona"
                   "ES_LEMG" "Málaga"
                   "TH_VTBS" "Bangkok"

                   "IS_BIKF" "Reykjavík"

                   "NO_ENKR" "Kirkenes"
                   "NO_ENRY" "Rygge"

                   "NO_ENML" "Molde"
                   "NO_ENDU" "Bardufoss"
                   "UK_EGCC" "Manchester"
                   "UK_EGPD" "Edinburgh"
                   "UK_EGPH" "Edinburgh"
                   "UK_EGNT" "Newcastle"
                   "UK_EGBB" "Birmingham"
                   "UK_EGGP" "Liverpool"
                   "UK_EGPK" "Glasgow"
                   "NL_EHEH" "Eindhoven"}))