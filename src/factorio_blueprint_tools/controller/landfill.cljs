(ns factorio-blueprint-tools.controller.landfill
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.landfill :as landfill]))

(def default-config
  {})

(defmulti landfill identity)

(defmethod landfill :init []
  {:state (assoc tools/default-state
                 :config default-config)})

(defmethod landfill :set-blueprint [_ [encoded-blueprint] state]
  {:state (tools/set-blueprint state encoded-blueprint)
   :dispatch [[:landfill :update]]})

(defmethod landfill :set-config [_ [k v] state]
  {:state (tools/set-config state k v)
   :dispatch [[:landfill :update]]})

(defmethod landfill :update [_ _ state]
  {:state (tools/update-result state
                               default-config
                               (fn [blueprint _]
                                 (landfill/landfill blueprint)))})
