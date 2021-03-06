(ns clj-simple-chart.ncs.reserve
  (:require [clj-http.client :as client]
            [clj-simple-chart.csv.csvmap :as csv]
            [clojure.pprint :as pprint]
            [clj-simple-chart.ncs.raw-production :as raw-production]
            [clojure.test :as test]))

(def field-reserves-url "https://factpages.npd.no/ReportServer_npdpublic?/FactPages/TableView/field_reserves&rs:Command=Render&rc:Toolbar=false&rc:Parameters=f&rs:Format=CSV&Top100=false&IpAddress=92.220.29.120&CultureCode=en")
(defonce field-reserves-raw (-> field-reserves-url
                                (client/get)
                                (:body)
                                (csv/csv-map)))

(def columns (:columns field-reserves-raw))
(def data (:data field-reserves-raw))

(test/is (= columns [:fldName
                     :fldRecoverableOil
                     :fldRecoverableGas
                     :fldRecoverableNGL
                     :fldRecoverableCondensate
                     :fldRecoverableOE
                     :fldRemainingOil
                     :fldRemainingGas
                     :fldRemainingNGL
                     :fldRemainingCondensate
                     :fldRemainingOE
                     :fldDateOffResEstDisplay
                     :fldNpdidField
                     :DatesyncNPD]))

(def numeric-columns [:fldRecoverableOil
                      :fldRecoverableGas
                      :fldRecoverableNGL
                      :fldRecoverableCondensate
                      :fldRecoverableOE
                      :fldRemainingOil
                      :fldRemainingGas
                      :fldRemainingNGL
                      :fldRemainingCondensate
                      :fldRemainingOE])

(def data-parsed (->> data
                      (csv/read-string-columns numeric-columns)
                      (csv/number-or-throw-columns numeric-columns)
                      (mapv #(assoc % :fldRecoverableLiquids (reduce + 0 (mapv % [:fldRecoverableOil :fldRecoverableCondensate :fldRecoverableNGL]))))))

(def field-names (map :fldName data))

(def field-names-all (vec (flatten [(map :fldName data)
                                    ["33/9-6 DELTA"]])))

(def banned-fields ["TAMBAR ØST"])

(def expand-field-map {"TAMBAR" ["TAMBAR" "TAMBAR ØST"]})

(defn get-reserve [field-name kind]
  (when-not (some #{field-name} field-names-all)
    (println field-name))
  (assert (some #{field-name} field-names-all))
  (assert (not (some #{field-name} banned-fields)))
  (assert (some #{kind} [:fldRecoverableOE :fldRecoverableOil :fldRecoverableGas :fldRecoverableLiquids]))
  (cond (not (some #{field-name} field-names))
        (let [prop (get {:fldRecoverableOE      :prfPrdOeNetMillSm3
                         :fldRecoverableOil     :prfPrdOilNetMillSm3
                         :fldRecoverableLiquids :prfPrdLiquidsNetMillSm3
                         :fldRecoverableGas     :prfPrdGasNetBillSm3} kind)]
          (->> raw-production/data
               (filter #(= field-name (:prfInformationCarrier %)))
               (mapv prop)
               (filter number?)
               (reduce + 0)
               (double)))
        :else
        (->> data-parsed
             (filter #(some #{(:fldName %)} (get expand-field-map field-name [field-name])))
             (map kind)
             (reduce + 0))))

(test/is (some #{"SINDRE"} field-names))
(test/is (not (some #{"33/9-6 DELTA"} field-names)))

(test/is (= 10.24 (get-reserve "JOHAN SVERDRUP" :fldRecoverableGas)))
(test/is (= 406.58 (get-reserve "JOHAN SVERDRUP" :fldRecoverableOil)))
(test/is (= 411.08 (get-reserve "JOHAN SVERDRUP" :fldRecoverableLiquids)))
