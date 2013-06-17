(ns affinis.parser
  (:require [clojure.string :refer [split trim]]))


;; This regular expression attempts to find four groups:
;;
;;  1. prefix, always prefixed with a semicolon
;;  2. command, never prefixed with a semicolon, always terminated by space
;;  3. params, never prefixed with a semicolon, always terminated by semicolon
;;  4. crlf, always prefixed with a semicolon
;;
;;  Matched groups that are prefixed with a semicolon will includes the
;;  semincolons this despite the fact that ideally we would sanitize the input
;;  at this stage. Unfortunately, it seems that it is prohibitively difficult
;;  to do so without introducing additional groups or multiple sets of matching
;;  groups, neither of which is necessarily desired. Later these groups are
;;  cleaned up inline.
(def RE-LINE #"^(\:\S+.*?|)([^\: ]\S+.*?|)([^\: ]\S+.*?|)(\:\S+.*?|)$")


(defn parse-line
  "
  Given a `line`, produces a map containing the `:prefix`, `:command`,
  `:params`, and `:crlf` keys. Values may be empty strings.
  "
  [line]
  (let [drop-semicolon (fn [s] (if (.startsWith (str s) ":") (drop 1 s) s))
        trimmed-line (for [g (drop 1 (re-find RE-LINE line))] (trim g))
        cleaned-line (map #(apply str (drop-semicolon %)) trimmed-line)]
    (assoc (zipmap [:prefix :command :params :crlf] cleaned-line) :line line)))
