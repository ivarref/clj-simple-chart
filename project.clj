(defproject clj-simple-chart "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                 [hiccup "1.0.5"]
                 [org.testfx/openjfx-monocle "1.8.0_20"]
                 [commons-io/commons-io "2.5"]
                 [digest "1.4.5"]
                 [base64-clj "0.1.1"]
                 [org.mozilla/rhino "1.7.7.1"]
                 [org.clojure/core.async "0.3.442"]
                 [clj-http "2.3.0"]
                 [camel-snake-kebab "0.4.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/data.json "0.2.6"]
                 [cheshire "5.7.1"]
                 [org.eclipse.jetty/jetty-util "9.4.5.v20170502"]
                 [clj-time "0.13.0"]
                 [com.vdurmont/emoji-java "3.2.0"]
                 [hickory/hickory "0.7.1"]]
  :aot [clj-simple-chart.jfx]
  :main clj-simple-chart.exp
  :profiles {:uberjar {:aot :all}})
