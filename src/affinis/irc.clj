(ns affinis.irc
  (:require [clojure.string :refer [split]]
            [affinis.io :refer [put-line]]))


(defn privmsg
  "
  Given a target `t` and a string to send `s`, sends the string to the target
  via a PRIVMSG.
  "
  [context t s] (prn t) (put-line context (str "PRIVMSG " t " :" s)))


(defn join
  "
  Given a channel `c`, JOINs the given channel.
  "
  [context c] (put-line context (str "JOIN " c)))


(defn part
  "
  Given a channel `c`, PARTs the given channel.
  "
  [context c] (put-line context (str "PART " c)))


(defn nick
  "
  Given a nick `nick`, NICKs with the given nick.
  "
  [context nick] (put-line context (str "NICK " nick)))


(defn user
  "
  Given a user `user` and a realname `realname`, USERs with the given
  parameters.
  "
  [context user realname]
  (put-line context (str "USER " user " 0 * :" realname)))


(defn quit [context & message]
  (put-line context (str "QUIT " (or message "exiting..."))))


;; Misc


(defn me
  [context t a] (privmsg context t (str (char 1) "ACTION " a (char 1))))


(defn reply
  "
  An abstraction over `privmsg`, given a string `s` to be sent and the keys
  `prefix` and `params`, sends a PRIVMSG to either a channel or user.

  If a channel is found in the params positon, then the reply is routed to the
  value of `params`. Otherwise the value of prefix is split and the message is
  routed to the presumed user.
  "
  [{:keys [prefix params] :as context} s]
  (if (= (first params) \#)
    (privmsg context (first (split params #" ")) s)  ;; channel reply
    (privmsg context ((split prefix #"!") 0) s)))    ;; user reply
