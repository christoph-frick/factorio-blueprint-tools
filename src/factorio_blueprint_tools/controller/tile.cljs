(ns factorio-blueprint-tools.controller.tile
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.tile :as tile]))

(defmulti tile identity)

(defmethod tile :init []
  (tools/controller-init tile/default-config))

(defmethod tile :set-blueprint [r [encoded-blueprint] state]
  (tools/controller-set-blueprint :tile state encoded-blueprint))

(defmethod tile :set-config [r [k v] state]
  (tools/controller-set-config :tile state k v))

(defmethod tile :update [_ _ state]
  (tools/controller-update-result state
                                  tile/default-config
                                  (fn [blueprint config]
                                    (tile/tile config blueprint))))
