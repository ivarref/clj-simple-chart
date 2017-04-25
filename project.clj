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
                 [com.vdurmont/emoji-java "3.2.0"]]
  :aot [clj-simple-chart.jfx]
  :main clj-simple-chart.exp
  :profiles {:uberjar {:aot :all}})
