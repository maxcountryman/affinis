(ns affinis.parser
  (:require [clojure.string :refer [split join]]))


(defn starts-with-semi? [line]
  (= (ffirst line) \:))


(defn remove-leading-semi [line]
  (when (starts-with-semi? line)
    (cons (join (rest (first line))) (rest line))))


(defn irc-line->map
  [line]
  (let [l (split line #" ")
        prefix (first (remove-leading-semi l))
        params (if prefix (rest (rest l)) (rest l))]
    {:prefix prefix
     :command (if prefix (first (rest l)) (first l))
     :params (join " " params)
     :line line}))
