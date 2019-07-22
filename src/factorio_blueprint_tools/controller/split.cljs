(ns factorio-blueprint-tools.controller.split
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.split :as split]))

(def default-config
  {:tile-size 64})

(defmulti split identity)

(defmethod split :init []
  {:state (assoc tools/default-state
                 :config default-config)})

(defmethod split :set-blueprint [_ [encoded-blueprint] state]
  {:state (tools/set-blueprint state encoded-blueprint)
   :dispatch [[:split :update]]})

(defmethod split :set-config [_ [k v] state]
  {:state (tools/set-config state k v)
   :dispatch [[:split :update]]})

(defmethod split :update [_ _ state]
  {:state (tools/update-result state
                               default-config
                               (fn [blueprint {:keys [tile-size]}]
                                 (when tile-size (split/split blueprint tile-size))))})
