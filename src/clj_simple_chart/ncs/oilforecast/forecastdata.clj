(ns clj-simple-chart.ncs.oilforecast.forecastdata)

(def data {
           "2005"
           ; Sokkelåret 2005 http://www.npd.no/no/Nyheter/Nyheter/2006/Sokkelaret-2005-/Sokkelaret-2005---petroleumsproduksjon-/
           {:prfPrdOilNetMillSm3 [140.8 152.7 152.6 145.8 143.6]}

           "2006"
           ; Sokkelåret 2006 http://www.npd.no/no/Nyheter/Nyheter/2007/Sokkelaret-2006/Sokkelaret-2006---petroleumsproduksjon-/
           {:prfPrdOilNetMillSm3 [129.4 130.0 126.5 120.6 119.0]}

           "2007"
           ; Sokkelåret 2007 http://www.npd.no/no/Nyheter/Nyheter/2008/Sokkelaret-2007/Sokkelaret-2007---Petroleumsproduksjon-/
           {:prfPrdOilNetMillSm3 [117.9 115.3 114.3 112.9 110.3]}

           "2008"
           ; Sokkelåret 2008 http://www.npd.no/no/Nyheter/Nyheter/2009/Sokkelaret-2008/Sokkelaret-2008---Petroleumsproduksjon/
           {:prfPrdOilNetMillSm3 [110.8 104.4 98.5 95.0 94.4]}

           "2009"
           ; Sokkelåret 2009 http://www.npd.no/no/Nyheter/Nyheter/2010/Sokkelaret-2009/Sokkelaret-2009---Petroleumsproduksjon/
           {:prfPrdOilNetMillSm3 [108.7 100.1 97.6 93.0 91.6]}
           ;:prfPrdGasNetBillSm3 [105.0 109.0 112.0 112.0 112.2]

           "2010"
           ; Sokkelåret 2010 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkel%C3%A5ret-2010/Tabell.pdf
           {:prfPrdOilNetMillSm3 [98.3 94.0 91.9 89.8 88.9]}

           "2011"
           ; Sokkelåret 2011 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret2011/Produksjon-tabell.pdf
           {:prfPrdOilNetMillSm3 [93.8 92.7 90.8 90.6 90.2]}

           "2012"
           ; Sokkelåret 2012 http://www.npd.no/Nyheter/Nyheter/2013/Sokkelaret-2012---pressemeldinger/Petroleumsproduksjon/
           {:prfPrdOilNetMillSm3 [85.5 88.3 90.7 90.1 91.0]}

           "2013"
           ; Sokkelåret 2013 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2013/Datagrunnlag_til_nettpubl_alle.xlsx
           {:prfPrdOilNetMillSm3 [85.50 86.50 86.70 86.98 85.94]}

           "2014"
           ; Sokkelåret 2014 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaaret-2014/Datagrunnlag-til-publisering.xlsx
           {:prfPrdOilNetMillSm3 [86.31 86.62 85.68 82.41 80.68]}

           "2015"
           ; Sokkelåret 2015 http://www.npd.no/Global/Norsk/1-Aktuelt/Nyheter/Sokkelaret-2015/Sokkelaaret-figur-grunnlag.xlsx
           {:prfPrdOilNetMillSm3 [89.04 87.25 81.96 80.21 81.67]}

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
           {:prfPrdOilNetMillSm3       [90.15 87.23 100.69 109.50 111.32]
            :prfPrdLiquidsNetMillMboed [1.93 1.86 2.08 2.26 2.30]}})


(defn process-prediction-year [[yr values]]
  (->>
    (for [[resource vals] values]
      (for [[idx val] (map-indexed (fn [idx x] [idx x]) vals)]
        {:predictionYear (inc (read-string yr))
         :prfYear        (+ 1 idx (read-string yr))
         :offsetYear     (inc idx)
         resource        val}))
    (flatten)
    (group-by :prfYear)
    (vals)
    (mapv #(reduce merge {} %))
    (flatten)
    (sort-by :prfYear)))

(def pretty-data (flatten (mapv process-prediction-year data)))

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