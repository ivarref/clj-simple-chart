(defproject clj-simple-chart "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [hiccup "1.0.5"]
                 [org.testfx/openjfx-monocle "1.8.0_20"]
                 [digest "1.4.5"]]
  :aot [clj-simple-chart.core]
  :main clj-simple-chart.core
  :profiles {:uberjar {:aot :all}})
