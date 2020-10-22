(ns factorio-blueprint-tools.controller.landfill
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.landfill :as landfill]))

(def default-config
  {:mode :full})

(defmulti landfill identity)

(defmethod landfill :init []
  (tools/controller-init default-config))

(defmethod landfill :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :landfill state encoded-blueprint))

(defmethod landfill :set-config [_ [k v] state]
  (tools/controller-set-config :landfill state k v))

(defmethod landfill :update [_ _ state]
  (tools/controller-update-result state
                                  default-config
                                  (fn [blueprint config]
                                    (landfill/landfill config blueprint))))
