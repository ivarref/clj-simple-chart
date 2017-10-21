(ns clj-simple-chart.ssb.nettokontantstraumprognose)

(def data {
           "2000" [47.596 85.120] ; https://www.regjeringen.no/contentassets/d4c7e71774cd4e688a3093d2f5aebc3f/no/pdfa/stp199920000001guldddpdfa.pdf
           "2001" [160.152 188.980] ; https://www.regjeringen.no/contentassets/2bf8764f7e1246e58ad7633b02ea5e6f/no/pdfa/stp200020010001guldddpdfa.pdf
           "2002" [244.852 205.470] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2002/dokumenter/pdf/gulbok.pdf
           "2003" [169.587 172.755] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2003/dokumenter/pdf/gulbok.pdf
           "2004" [165.902 143.473] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2004/dokumenter/pdf/gulbok.pdf
           "2005" [205.562 204.477] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2005/dokumenter/pdf/gulbok.pdf
           "2006" [282.990 328.039] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2006/dokumenter/pdf/gulbok.pdf
           "2007" [360.878 364.893] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2007/dokumenter/pdf/gulbok.pdf
           "2008" [319.305 301.773] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2008/dokumenter/pdf/gulbok.pdf
           "2009" [424.6 407.1] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2009/dokumenter/pdf/gulbok.pdf
           "2010" [264.7 220.4] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2010/dokumenter/pdf/gulbok.pdf
           "2011" [264.7 288.0] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2011/dokumenter/pdf/gulbok.pdf
           "2012" [341.3 351.7] ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2012/dokumenter/pdf/gulbok.pdf
           "2013" [386.8 373.2] ; https://www.statsbudsjettet.no/upload/Statsbudsjett_2013/dokumenter/pdf/gulbok.pdf
           "2014" [349.9 314.1] ; https://www.statsbudsjettet.no/Statsbudsjettet-2014/Satsinger/?pid=59880#hopp
           "2015" [297.2 304.0] ; https://www.statsbudsjettet.no/Statsbudsjettet-2015/Satsinger/?pid=65153#hopp
           "2016" [217.8 204.1] ; https://www.statsbudsjettet.no/Statsbudsjettet-2016/Satsinger/?pid=69114#hopp
           "2017" [124.5 138.3] ; https://www.statsbudsjettet.no/Statsbudsjettet-2017/Satsinger/?pid=72986
           "2018" [175.4 183.0] ; https://www.statsbudsjettet.no/Statsbudsjettet-2018/Satsinger/?pid=83808
           })

(def gul-bok (->> (keys data)
                  (mapv (fn [year] {:year                year
                                    :netto-kontantstraum (last (get data year))}))
                  (sort-by :year)
                  (vec)))