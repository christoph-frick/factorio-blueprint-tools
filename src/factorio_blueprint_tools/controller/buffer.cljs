(ns factorio-blueprint-tools.controller.buffer
  (:require [factorio-blueprint-tools.controller.tools :as tools]
            [factorio-blueprint-tools.buffer :as buffer]))

(def default-config
  {})

(defmulti buffer identity)

(defmethod buffer :init []
  (tools/controller-init default-config))

(defmethod buffer :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :buffer state encoded-blueprint))

(defmethod buffer :set-config [_ [k v] state]
  (tools/controller-set-config :buffer state k v))

(defmethod buffer :update [_ _ state]
  (tools/controller-update-result state
                                  default-config
                                  (fn [blueprint {:keys [] :as config}]
                                    (buffer/buffer-chest-blueprint blueprint))))
