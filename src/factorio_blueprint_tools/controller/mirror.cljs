(ns factorio-blueprint-tools.controller.mirror
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.mirror :as mirror]))

(def default-config
  {:direction :vertically})

(defmulti mirror identity)

(defmethod mirror :init []
  (tools/controller-init default-config))

(defmethod mirror :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :mirror state encoded-blueprint))

(defmethod mirror :set-config [_ [k v] state]
  (tools/controller-set-config :mirror state k v))

(defmethod mirror :update [_ _ state]
  (tools/controller-update-result state
                                  default-config
                                  (fn [blueprint {:keys [direction] :as config}]
                                    (mirror/mirror blueprint direction))))
