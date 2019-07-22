(ns factorio-blueprint-tools.controller.mirror
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.mirror :as mirror]))

(def default-config
  {:direction :vertically})

(defmulti mirror identity)

(defmethod mirror :init []
  {:state (assoc tools/default-state
                 :config default-config)})

(defmethod mirror :set-blueprint [_ [encoded-blueprint] state]
  {:state (tools/set-blueprint state encoded-blueprint)
   :dispatch [[:mirror :update]]})

(defmethod mirror :set-config [_ [k v] state]
  {:state (tools/set-config state k v)
   :dispatch [[:mirror :update]]})

(defmethod mirror :update [_ _ state]
  {:state (tools/update-result state
                               default-config
                               (fn [blueprint {:keys [direction] :as config}]
                                 (mirror/mirror blueprint direction)))})
