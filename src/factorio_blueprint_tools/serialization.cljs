(ns factorio-blueprint-tools.serialization
  (:require [goog.crypt.base64 :as b64]))

(defn decode
  "Decodes a Factorio blueprint string (`0` prefix, base64, zlib, JSON) into a plain map with keyword keys"
  [blueprint-string]
  {:pre [(= "0" (.charAt blueprint-string))]}
  (-> blueprint-string
      (.substring 1)
      (b64/decodeStringToUint8Array)
      (js/pako.inflate #js{:to "string"})
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(defn encode
  "Encodes a map into a Factorio blueprint string (inverse of `decode`)"
  [blueprint]
  {:pre [(map? blueprint)]}
  (-> blueprint
      (clj->js)
      (js/JSON.stringify)
      (js/pako.deflate #js{:to "string"})
      (b64/encodeString)
      (.replace #"^" "0")))

