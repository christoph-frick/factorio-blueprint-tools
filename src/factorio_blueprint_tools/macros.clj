(ns factorio-blueprint-tools.macros
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [markdown.core :as md]))

(defn load-resource
  [resource]
  (-> resource
      io/resource
      slurp))

(defmacro load-edn
  [resource]
  (edn/read-string (load-resource resource)))

(defmacro load-markdown
  [resource]
  (md/md-to-html-string (load-resource resource)))
