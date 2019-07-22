(ns factorio-blueprint-tools.controller.upgrade
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.upgrade :as upgrade]))

(defmulti upgrade identity)

(defmethod upgrade :init []
  (tools/controller-init upgrade/default-upgrade-config))

(defmethod upgrade :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :upgrade state encoded-blueprint))

(defmethod upgrade :set-config [_ [k v] state]
  (tools/controller-set-config :upgrade state k v))

(defmethod upgrade :update [_ _ state]
  (tools/controller-update-result state
                                  upgrade/default-upgrade-config
                                  (fn [blueprint config]
                                    (upgrade/upgrade-blueprint config blueprint))))
