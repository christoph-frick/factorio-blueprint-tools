(defproject factorio-blueprint-tools "0.1.0-SNAPSHOT"
    :description "Factorio Blueprint Tools"
    :url "https://github.com/christoph-frick/factorio-blueprint-tools"
    :min-lein-version "2.7.1"

    :dependencies [[org.clojure/clojure "1.11.1"]
                   [org.clojure/clojurescript "1.11.60"]
                   ; [org.clojure/core.async  "0.3.443"]
                   [com.rpl/specter "1.1.4"]
                   [markdown-clj "1.11.4"]
                   [rum "0.12.10"]
                   [net.ofnir/antizer "3.26.17-1"]
                   [hiccups "0.3.0"]
                   [clj-commons/pushy "0.3.10"]
                   [clj-commons/citrus "3.3.0"]
                   [cheshire "5.11.0"]]

    :plugins [[lein-figwheel "0.5.20"]
              [lein-cljsbuild "1.1.8" :exclusions [[org.clojure/clojure]]]]

    :source-paths ["src"]

    :cljsbuild {:builds
                [{:id "dev"
                  :source-paths ["src"]

                  ;; The presence of a :figwheel configuration here
                  ;; will cause figwheel to inject the figwheel client
                  ;; into your build
                  :figwheel {:on-jsload "factorio-blueprint-tools.core/on-js-reload"
                             ;; :open-urls will pop open your application
                             ;; in the default browser once Figwheel has
                             ;; started and compiled your application.
                             ;; Comment this out once it no longer serves you.
                             :open-urls ["http://localhost:3449/factorio-blueprint-tools/index.html"]
                             }

                  :compiler {:main factorio-blueprint-tools.core
                             :asset-path "js/compiled/out"
                             :output-to "resources/public/factorio-blueprint-tools/js/compiled/factorio_blueprint_tools.js"
                             :output-dir "resources/public/factorio-blueprint-tools/js/compiled/out"
                             :source-map-timestamp true
                             ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                             ;; https://github.com/binaryage/cljs-devtools
                             :preloads [devtools.preload]}}
                 ;; This next build is a compressed minified build for
                 ;; production. You can build this with:
                 ;; lein cljsbuild once min
                 {:id "min"
                  :source-paths ["src"]
                  :compiler {:output-to "resources/public/factorio-blueprint-tools/js/compiled/factorio_blueprint_tools.js"
                             :externs ["externs/pako-externs.js"]
                             :main factorio-blueprint-tools.core
                             :optimizations :advanced
                             :pretty-print false}}]}

    :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
               ;; :server-port 3449 ;; default
               ;; :server-ip "127.0.0.1"

               :css-dirs ["resources/public/factorio-blueprint-tools/css"] ;; watch and update CSS

               ;; Start an nREPL server into the running figwheel process
               ;; :nrepl-port 7888

               ;; Server Ring Handler (optional)
               ;; if you want to embed a ring handler into the figwheel http-kit
               ;; server, this is for simple ring servers, if this

               ;; doesn't work for you just run your own server :) (see lein-ring)

               ;; :ring-handler hello_world.server/handler

               ;; To be able to open files in your editor from the heads up display
               ;; you will need to put a script on your path.
               ;; that script will have to take a file path and a line number
               ;; ie. in  ~/bin/myfile-opener
               ;; #! /bin/sh
               ;; emacsclient -n +$2 $1
               ;;
               ;; :open-file-command "myfile-opener"

               ;; if you are using emacsclient you can just use
               ;; :open-file-command "emacsclient"

               ;; if you want to disable the REPL
               ;; :repl false

               ;; to configure a different figwheel logfile path
               ;; :server-logfile "tmp/logs/figwheel-logfile.log"

               ;; to pipe all the output to the repl
               ;; :server-logfile false
               }

    :aliases {"test-refresh" ["kaocha" "--watch"]
              "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}

    ;; Setting up nREPL for Figwheel and ClojureScript dev
    ;; Please see:
    ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
    :profiles {:kaocha {:dependencies [[lambdaisland/kaocha "1.71.1119"]]}
               :dev {:dependencies [[binaryage/devtools "1.0.6"]
                                    [figwheel-sidecar "0.5.20"]
                                    [cider/piggieback "0.5.3"]]
                     ;; need to add dev source path here to get user.clj loaded
                     :source-paths ["src" "dev"]
                     ;; for CIDER
                     ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                     :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                     ;; need to add the compliled assets to the :clean-targets
                     :clean-targets ^{:protect false} ["resources/public/factorio-blueprint-tools/js/compiled"
                                                       :target-path]}})
