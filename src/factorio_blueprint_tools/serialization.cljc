(ns factorio-blueprint-tools.serialization
  #?(:clj (:require [cheshire.core :as json]
                    [clojure.java.io :as io]
                    [clojure.string :as str])
     :cljs (:require [goog.crypt.base64 :as b64]
                     [clojure.string :as str]))
  #?(:clj (:import (java.util.zip InflaterInputStream DeflaterOutputStream)
                   (java.io ByteArrayOutputStream)
                   (java.util Base64))))

(def ^:const preamble "0")

(defn blueprint-string?
  [blueprint-string]
  (str/starts-with? blueprint-string preamble))

(defn blueprint?
  [blueprint]
  (map? blueprint))


(defn add-preamble
  [blueprint-string]
  (str/replace-first blueprint-string #"^" preamble))

(defn strip-preamble
  [blueprint-string]
  (subs blueprint-string 1))


(defn json-encode
  [blueprint]
  (#?(:clj json/generate-string
      :cljs js/JSON.stringify)
   blueprint))

(defn json-decode
  [blueprint-string]
  (#?(:clj json/parse-string
      :cljs js/JSON.parse)
   blueprint-string))


(defn b64-encode
  [blueprint]
  #?(:clj (.encodeToString (Base64/getEncoder) blueprint)
     :cljs (b64/encodeString blueprint)))

(defn b64-decode
  [blueprint-string]
  #?(:clj (.decode (Base64/getDecoder) blueprint-string)
     :cljs (b64/decodeStringToUint8Array blueprint-string)))


(defn zlib-deflate
  [blueprint]
  #?(:clj (let [blueprint-os (ByteArrayOutputStream.)
                deflater (DeflaterOutputStream. blueprint-os)]
            (spit deflater blueprint)
            (.toByteArray blueprint-os))
     :cljs (js/pako.deflate blueprint #js{:to "string"})))

(defn zlib-inflate
  [blueprint-string]
  #?(:clj (-> blueprint-string io/input-stream InflaterInputStream. slurp)
     :cljs (js/pako.inflate blueprint-string #js{:to "string"})))


(defn encode-pre-process
  [blueprint]
  #?(:clj (identity blueprint)
     :cljs (clj->js blueprint)))

(defn decode-post-process
  [blueprint-string]
  #?(:clj (identity blueprint-string)
     :cljs (js->clj blueprint-string :keywordize-keys true)))


(defn decode
  "Decodes a Factorio blueprint string (`0` prefix, base64, zlib, JSON) into a plain map with keyword keys"
  [blueprint-string]
  {:pre [(blueprint-string? blueprint-string)]}
  (-> blueprint-string
      strip-preamble
      b64-decode
      zlib-inflate
      json-decode
      decode-post-process))

(defn encode
  "Encodes a map into a Factorio blueprint string (inverse of `decode`)"
  [blueprint]
  {:pre [(blueprint? blueprint)]}
  (-> blueprint
      encode-pre-process
      json-encode
      zlib-deflate
      b64-encode
      add-preamble))

