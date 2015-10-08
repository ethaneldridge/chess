(defproject chess "1.0.0-SNAPSHOT"
  :description "chess engine"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [criterium "0.4.3"] 
                 [org.clojure/math.combinatorics "0.1.1"]
                 [ring "1.4.0"] ; http basics
                 [compojure "1.4.0"] ; http routing
                 [hiccup "1.0.5"] ; generating html
                 [org.clojure/data.json "0.2.6"]
                 [com.typesafe.akka/akka-actor_2.10 "2.3.14"]
                 [com.typesafe.akka/akka-remote_2.10 "2.3.14"]
                 [com.typesafe.akka/akka-kernel_2.10 "2.3.14"]]
  :repositories {
    "Typesafe Repository for Akka" "http://repo.typesafe.com/typesafe/releases/"
    "sonatype-snapshots" "https://oss.sonatype.org/content/repositories/snapshots/"}
  :plugins [[lein-cljsbuild "1.1.0"]] ; see https://github.com/emezeske/lein-cljsbuild
  :cljsbuild {
              :builds
              [{:source-paths ["src-cljs"],
                :compiler
                {:optimizations :whitespace,
                 :output-to "resources/public/js/chess.js",
                 :pretty-print true}}],
              :crossover-path "crossover-cljs",
              :crossovers [chess.movelogic]}
  :aot [chess.util.actors]
  :warn-on-reflection false
  :jvm-opts ["-Xmx1024M" "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :main chess.web
  :java-source-paths ["src/chess/movelogic/bitboard"]
  :dev {:user {:plugins [[cider/cider-nrepl "0.9.1"]]}}
  ;:extra-classpath-dirs ["/usr/lib/jvm/java-6-sun/lib/tools.jar"]
  )
