(ns factorio-blueprint-tools.controller.debug
  (:require [factorio-blueprint-tools.controller.tools :as tools]))

(defn debug-blueprint
  [blueprint]
  (js/JSON.stringify blueprint nil 4))

(defmulti debug identity)

(defmethod debug :init [_ _ state])

(defmethod debug :set-blueprint [_ [encoded-blueprint] state]
  (tools/controller-set-blueprint :debug state encoded-blueprint))

(defmethod debug :update [_ _ state]
  {:state (assoc state :output (prn-str (get-in state [:input :blueprint])))})

