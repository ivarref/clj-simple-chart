(ns clj-simple-chart.ssb.oljekorrigert-overskudd)

(def data {1996 -22.730                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2002/dokumenter/pdf/gulbok.pdf s.195
           1997 -20.067                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2003/dokumenter/pdf/gulbok.pdf s.192
           1998 -17.453                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2004/dokumenter/pdf/gulbok.pdf s.216
           1999 -12.066                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2005/dokumenter/pdf/gulbok.pdf s.202
           2000 -7.943                                      ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2006/dokumenter/pdf/gulbok.pdf s.215
           2001 -1.640                                      ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2007/dokumenter/pdf/gulbok.pdf s.214
           2002 -62.392                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2008/dokumenter/pdf/gulbok.pdf s.216
           2003 -66.150                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2009/dokumenter/pdf/gulbok.pdf s.212
           2004 -79.249                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2010/dokumenter/pdf/gulbok.pdf s.233
           2005 -64.763                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2011/dokumenter/pdf/gulbok.pdf s.214
           2006 -44.002                                     ; https://www.statsbudsjettet.no/Upload/Statsbudsjett_2012/dokumenter/pdf/gulbok.pdf s.210
           2007 -1.342                                      ; https://www.statsbudsjettet.no/upload/Statsbudsjett_2013/dokumenter/pdf/gulbok.pdf s.216
           2008 -11.797
           2009 -96.561
           2010 -104.070                                    ; https://www.statsbudsjettet.no/upload/Statsbudsjett_2013/dokumenter/pdf/gulbok.pdf s.216
           2011 -79.4                                       ; https://www.statsbudsjettet.no/Statsbudsjettet-2014/Satsinger/?pid=59880#hopp
           2012 -100.9                                      ; https://www.statsbudsjettet.no/Statsbudsjettet-2015/Satsinger/?pid=65153#hopp
           2013 -116.5                                      ; https://www.statsbudsjettet.no/Statsbudsjettet-2016/Satsinger/?pid=69114#hopp
           2014 -160.0                                      ; https://www.statsbudsjettet.no/Statsbudsjettet-2017/Satsinger/?pid=72986
           2015 -185.3                                      ; https://www.statsbudsjettet.no/Statsbudsjettet-2017/Satsinger/?pid=72986
           2016 -208.4                                      ; https://www.statsbudsjettet.no/Statsbudsjettet-2018/Satsinger/?pid=83808
           2017 -232.4})                                    ; https://www.statsbudsjettet.no/Statsbudsjettet-2018/Satsinger/?pid=83808
