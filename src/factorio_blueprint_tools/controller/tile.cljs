(ns factorio-blueprint-tools.controller.tile
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.tile :as tile]))

(def default-config
  {:tile-x 2
   :tile-y 2})

(defmulti tile identity)

(defmethod tile :init []
  (tools/controller-init default-config))

(defmethod tile :set-blueprint [r [encoded-blueprint] state]
  (tools/controller-set-blueprint :tile state encoded-blueprint))

(defmethod tile :set-config [r [k v] state]
  (tools/controller-set-config :tile state k v))

(defmethod tile :update [_ _ state]
  (tools/controller-update-result state
                                  default-config
                                  (fn [blueprint {:keys [tile-x tile-y] :as config}]
                                    (tile/tile blueprint tile-x tile-y))))
