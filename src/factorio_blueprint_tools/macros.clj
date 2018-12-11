(ns factorio-blueprint-tools.macros
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defmacro load-edn
  [resource]
  (-> resource
      io/resource
      slurp
      edn/read-string))
