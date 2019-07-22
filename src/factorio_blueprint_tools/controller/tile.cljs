(ns factorio-blueprint-tools.controller.tile
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.tile :as tile]))

(def default-config
  {:tile-x 2
   :tile-y 2})

(defmulti tile identity)

(defmethod tile :init []
  {:state (assoc tools/default-state
                 :config default-config)})

(defmethod tile :set-blueprint [r [encoded-blueprint] state]
  {:state (tools/set-blueprint state encoded-blueprint)
   :dispatch [[:tile :update]]})

(defmethod tile :set-config [r [k v] state]
  {:state (tools/set-config state k v)
   :dispatch [[:tile :update]]})

(defmethod tile :update [_ _ state]
  {:state (tools/update-result state
                               default-config
                               (fn [blueprint {:keys [tile-x tile-y] :as config}]
                                 (tile/tile blueprint tile-x tile-y)))})

