(ns factorio-blueprint-tools.controller.tools
  (:require [clojure.string :as str]
            [factorio-blueprint-tools.serialization :as ser]))

(def default-state
  {:input {:encoded nil
           :blueprint nil
           :error nil}
   :config {}
   :output {:encoded nil
            :blueprint nil}})

(defn decode-blueprint
  [encoded-blueprint]
  (if (or (not encoded-blueprint) (str/blank? encoded-blueprint))
    [nil nil]
    (try
      [(ser/decode encoded-blueprint) nil]
      (catch :default e
        [nil e]))))

(defn set-blueprint
  [state encoded-blueprint]
  (let [[blueprint error] (decode-blueprint encoded-blueprint)]
    (update state :input assoc :encoded encoded-blueprint :blueprint blueprint :error error)))

(defn set-config
  [state k v]
  (update state :config assoc k v))

(defn update-result
  [state default-config update-fn]
  (let [blueprint (some-> state :input :blueprint)
        config (or (some-> state :config) default-config)
        result (some-> blueprint (update-fn config))
        encoded-result (some-> result ser/encode)]
    (update state :output assoc :blueprint result :encoded encoded-result)))
