(ns factorio-blueprint-tools.controller.upgrade
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.upgrade :as upgrade]))

(defmulti upgrade identity)

(defmethod upgrade :init []
  {:state (assoc tools/default-state
                 :config upgrade/default-upgrade-config)})

(defmethod upgrade :set-blueprint [_ [encoded-blueprint] state]
  {:state (tools/set-blueprint state encoded-blueprint)
   :dispatch [[:upgrade :update]]})

(defmethod upgrade :set-config [_ [k v] state]
  {:state (tools/set-config state k v)
   :dispatch [[:upgrade :update]]})

(defmethod upgrade :update [_ _ state]
  {:state (tools/update-result state
                               upgrade/default-upgrade-config
                               (fn [blueprint config]
                                 (upgrade/upgrade-blueprint config blueprint)))})
