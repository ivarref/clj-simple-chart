(ns clj-simple-chart.ncs.oilforecast.forecastdata
  (:require [clojure.edn :as edn]))

(def data
  {
   "2005"
   ; Sokkelåret 2005 http://www.npd.no/no/Nyheter/Nyheter/2006/Sokkelaret-2005-/Sokkelaret-2005---petroleumsproduksjon-/
   {:prfPrdOilNetMillSm3        [140.8 152.7 152.6 145.8 143.6]
    :prfPrdCondensateNetMillSm3 [5.6 3.4 5.1 5.1 5.6]
    :prfPrdNGLNetMillSm3        [8.7 9.4 10.4 10.1 9.8]}

   "2006"
   ; Sokkelåret 2006 http://www.npd.no/no/Nyheter/Nyheter/2007/Sokkelaret-2006/Sokkelaret-2006---petroleumsproduksjon-/
   {:prfPrdOilNetMillSm3        [129.4 130.0 126.5 120.6 119.0]
    :prfPrdCondensateNetMillSm3 [3.4 4.4 4.7 5.3 5.7]
    :prfPrdNGLNetMillSm3        [17.3 18.6 17.8 16.1 15.7]
    :prfPrdGasNetBillSm3        [93.0 108.8 115.3 117.8 117.7]}

   "2007"
   ; Sokkelåret 2007 http://www.npd.no/no/Nyheter/Nyheter/2008/Sokkelaret-2007/Sokkelaret-2007---Petroleumsproduksjon-/
   {:prfPrdOilNetMillSm3        [117.9 115.3 114.3 112.9 110.3]
    :prfPrdCondensateNetMillSm3 [4.0 4.9 5.1 6.2 6.6]
    :prfPrdNGLNetMillSm3        [18.8 19.6 17.8 18.8 19.1]
    :prfPrdGasNetBillSm3        [99.4 108.8 110.4 115.9 115.3]}

   "2008"
   ; Sokkelåret 2008 http://www.npd.no/no/Nyheter/Nyheter/2009/Sokkelaret-2008/Sokkelaret-2008---Petroleumsproduksjon/
   {:prfPrdOilNetMillSm3        [110.8 104.4 98.5 95.0 94.4]
    :prfPrdCondensateNetMillSm3 [4.4 4.6 5.3 5.3 5.5]
    :prfPrdNGLNetMillSm3        [17.5 17.3 17.4 17.5 16.7]
    :prfPrdGasNetBillSm3        [102.9 106.7 112.0 111.6 111.9]}

   "2009"
   ; Sokkelåret 2009 http://www.npd.no/no/Nyheter/Nyheter/2010/Sokkelaret-2009/Sokkelaret-2009---Petroleumsproduksjon/
   {:prfPrdOilNetMillSm3        [108.7 100.1 97.6 93.0 91.6]
    :prfPrdCondensateNetMillSm3 [4.2 4.0 3.8 3.3 3.5]
    :prfPrdNGLNetMillSm3        [16.9 17.1 17.0 16.3 16.1]
    :prfPrdGasNetBillSm3        [105.0 109.0 112.0 112.0 112.2]}

   "2010"
   ; Sokkelåret 2010 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkel%C3%A5ret-2010/Tabell.pdf
   {:prfPrdOilNetMillSm3        [98.3 94.0 91.9 89.8 88.9]
    :prfPrdCondensateNetMillSm3 [4.4 4.2 4.0 3.4 3.2]
    :prfPrdNGLNetMillSm3        [16.8 16.9 16.2 16.9 16.8]
    :prfPrdGasNetBillSm3        [109.1 111.0 111.6 111.6 112.2]}

   "2011"
   ; Sokkelåret 2011 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret2011/Produksjon-tabell.pdf
   {:prfPrdOilNetMillSm3        [93.8 92.7 90.8 90.6 90.2]
    :prfPrdCondensateNetMillSm3 [4.3 3.4 2.9 2.7 2.4]
    :prfPrdNGLNetMillSm3        [17.5 17.6 18.3 18.7 18.1]
    :prfPrdGasNetBillSm3        [106.7 109.5 111.8 112.0 112.1]}

   "2012"
   ; Sokkelåret 2012 http://www.npd.no/Nyheter/Nyheter/2013/Sokkelaret-2012---pressemeldinger/Petroleumsproduksjon/
   {:prfPrdOilNetMillSm3        [85.5 88.3 90.7 90.1 91.0]
    :prfPrdCondensateNetMillSm3 [3.7 3.2 2.9 2.6 2.8]
    :prfPrdNGLNetMillSm3        [16.8 19.2 20.6 21.5 20.1]
    :prfPrdGasNetBillSm3        [106.5 109.5 110.5 110.6 111.2]}

   "2013"
   ; Sokkelåret 2013 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2013/Datagrunnlag_til_nettpubl_alle.xlsx
   {:prfPrdOilNetMillSm3        [85.50 86.50 86.70 86.98 85.94]
    :prfPrdCondensateNetMillSm3 [3.17 2.85 2.62 2.91 3.53]
    :prfPrdNGLNetMillSm3        [18.89 19.43 19.94 19.38 19.62]
    :prfPrdGasNetBillSm3        [107.01 106.29 107.69 113.33 115.79]}

   "2014"
   ; Sokkelåret 2014 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaaret-2014/Datagrunnlag-til-publisering.xlsx
   {:prfPrdOilNetMillSm3        [86.31 86.62 85.68 82.41 80.68]
    :prfPrdCondensateNetMillSm3 [2.10 2.09 2.44 2.76 3.00]
    :prfPrdNGLNetMillSm3        [19.55 19.55 19.08 19.04 18.29]
    :prfPrdGasNetBillSm3        [107.59 107.08 109.96 112.91 112.89]}

   "2015"
   ; Sokkelåret 2015 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2015/Sokkelaaret-figur-grunnlag.xlsx
   {:prfPrdOilNetMillSm3        [89.04 87.25 81.96 80.21 81.67]
    :prfPrdCondensateNetMillSm3 [2.01 1.83 2.37 2.61 2.30]
    :prfPrdNGLNetMillSm3        [19.43 18.79 18.44 17.92 17.09]
    :prfPrdGasNetBillSm3        [106.57 107.25 109.71 110.96 111.05]}

   "2016"
   ; Sokkelåret 2016 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2016/Figurgrunnlag-Sokkelaret-2016.xlsx
   {:prfPrdOilNetMillSm3        [93.88 88.05 83.30 91.27 98.90]
    :prfPrdCondensateNetMillSm3 [1.83 2.37 2.61 2.30 2.22]
    :prfPrdNGLNetMillSm3        [19.37 18.42 18.56 18.13 17.32]
    :prfPrdLiquidsNetMillMboed  [1.98 1.88 1.80 1.92 2.04]
    :prfPrdLiquidsNetMillSm3    [115.08 108.84 104.47 111.70 118.44]
    :prfPrdGasNetBillSm3        [114.47 114.54 114.46 114.34 113.76]}

   "2017"
   ; http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2017/Figurgrunnlag-til-publisering-Sokkelaret2017.xlsx
   {:prfPrdOilNetMillSm3        [90.15 87.23 100.69 109.50 111.32]
    :prfPrdCondensateNetMillSm3 [1.78 1.69 1.50 1.50 1.40]
    :prfPrdNGLNetMillSm3        [19.84 19.28 18.70 20.10 20.69]
    :prfPrdLiquidsNetMillMboed  [1.93 1.86 2.08 2.26 2.30]
    :prfPrdLiquidsNetMillSm3    [111.77 108.20 120.89 131.10 133.41]
    :prfPrdGasNetBillSm3        [121.17 121.27 121.76 122.59 122.68]}})


(defn process-prediction-year [[yr values]]
  (->>
    (for [[resource vals] values
          [idx val] (map-indexed (fn [idx x] [idx x]) vals)]
      {:predictionYear (inc (edn/read-string yr))
       :prfYear        (+ 1 idx (edn/read-string yr))
       :offsetYear     (inc idx)
       resource        val})
    (group-by :prfYear)
    (vals)
    (map #(reduce merge {} %))
    (flatten)
    (sort-by :prfYear)))

(defn add-liquids-mboed [e]
  (-> e
      (update :prfPrdLiquidsNetMillMboed (fn [o] (or o
                                                     (/ (* 6.29 (+ (:prfPrdOilNetMillSm3 e)
                                                                   (:prfPrdCondensateNetMillSm3 e)
                                                                   (:prfPrdNGLNetMillSm3 e)))
                                                        365))))))

(def pretty-data (->> data
                      (mapcat process-prediction-year)
                      (map add-liquids-mboed)
                      (vec)))

(def plus-5 (->> pretty-data
                 (filter #(= 5 (:offsetYear %)))
                 (sort-by :prfYear)
                 (vec)))

(def plus-4 (->> pretty-data
                 (filter #(= 4 (:offsetYear %)))
                 (sort-by :prfYear)
                 (vec)))

(def plus-3 (->> pretty-data
                 (filter #(= 3 (:offsetYear %)))
                 (sort-by :prfYear)
                 (vec)))

(def plus-2 (->> pretty-data
                 (filter #(= 2 (:offsetYear %)))
                 (sort-by :prfYear)
                 (vec)))

(def plus-1 (->> pretty-data
                 (filter #(= 1 (:offsetYear %)))
                 (sort-by :prfYear)
                 (vec)))

(def current-forecast (->> pretty-data
                           (filter #(= 2018 (:predictionYear %)))
                           (sort-by :prfYear)
                           (vec)))

(defn yearly-forecast-to-months
  [{:keys [prfYear prfPrdOilNetMillSm3 prfPrdLiquidsNetMillMboed]}]
  (for [month (range 1 13)]
    {:prfYear                   prfYear
     :prfMonth                  month
     :year                      (str prfYear)
     :date                      (format "%d-%02d" prfYear month)
     :sum                       prfPrdOilNetMillSm3
     :prfPrdOilNetMillSm3       prfPrdOilNetMillSm3
     :prfPrdLiquidsNetMillMboed prfPrdLiquidsNetMillMboed}))

(defn forecast-monthly [forecast]
  (->> forecast
       (mapv yearly-forecast-to-months)
       (flatten)
       (vec)))

(defn forecast-monthly-eoy [forecast]
  (->> forecast
       (forecast-monthly)
       (filter #(= 12 (:prfMonth %)))
       (vec)))