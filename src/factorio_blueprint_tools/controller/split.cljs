(ns factorio-blueprint-tools.controller.split
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.split :as split]))

(def default-config
  {:tile-size 64})

(defmulti split identity)

(defmethod split :init []
  (tools/controller-init default-config))

(defmethod split :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :split state encoded-blueprint))

(defmethod split :set-config [_ [k v] state]
  (tools/controller-set-config :split state k v))

(defmethod split :update [_ _ state]
  (tools/controller-update-result state
                                  default-config
                                  (fn [blueprint {:keys [tile-size]}]
                                    (when tile-size (split/split blueprint tile-size)))))
