(ns affinis.irc
  (:require [clojure.string :refer [split]]
            [affinis.io :refer [put-line]]))


(defn privmsg
  "
  Given a target `t` and a string to send `s`, sends the string to the target
  via a PRIVMSG.
  "
  [t s] (put-line (str "PRIVMSG " t " :" s)))


(defn join
  "
  Given a channel `c`, JOINs the given channel.
  "
  [c] (put-line (str "JOIN " c)))


(defn part
  "
  Given a channel `c`, PARTs the given channel.
  "
  [c] (put-line (str "PART " c)))


(defn nick
  "
  Given a nick `n`, NICKs with the given nick.
  "
  [n] (put-line (str "NICK " n)))


(defn reply
  "
  An abstraction over `privmsg`, given a string `s` to be sent and the keys
  `prefix` and `params`, sends a PRIVMSG to either a channel or user.

  If a channel is found in the params positon, then the reply is routed to the
  value of `params`. Otherwise the value of prefix is split and the message is
  routed to the presumed user.
  "
  [s {:keys [prefix params]}]
  (if (= (first params) \#)
    (privmsg params s)                     ;; channel reply
    (privmsg ((split prefix #"!") 0) s)))  ;; user reply
