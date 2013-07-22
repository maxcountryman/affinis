(defproject affinis "0.1.0-SNAPSHOT"
  :description "An IRC connection layer written Clojure."
  :url "https://github.com/maxcountryman/affinis"
  :license {:name "BSD License"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [org.clojure/tools.logging "0.2.3"]]
  :plugins [[lein-marginalia "0.7.1"]])
