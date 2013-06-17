(ns affinis.connection
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [clojure.tools.logging :as logging]
            [affinis.callback :refer [callbacks defcallback]]
            [affinis.io :refer [input output put-line]]
            [affinis.irc :as irc]
            [affinis.parser :refer [parse-line]])
  (:import java.net.Socket))

(declare apply-callbacks register run take-line)

;; Mutable IRC state.
(defonce irc-state (ref {:connected false :registered false}))


(defmacro defstate
  "
  Defines a state function, given a `sname`, `args`, and `body`. State
  functions are wrapped with calls to `run`, providing `*agent*` as its only
  argument.
  "
  [sname args & body]
  `(defn ~sname ~args (try ~@body (finally (~run *agent*)))))


(defn read-lines
  "
  Loops over a socket connection, reading lines and putting them into a queue.
  "
  [{:keys [socket]}]
  (doseq [line (line-seq (io/reader socket))]
    (.put input line)
    (logging/info line)))


(defn write-lines
  "
  Takes a lines off the `output` queue and writes it to a socket connection.
  "
  [{:keys [socket]}]
  (while true
    (when-let [line (.take output)]
      (binding [*out* (io/writer socket)]
        (println line))
      (logging/info (str ">>> " line)))))


(defcallback pong [{:keys [command line]}]
  (when (= command "PING")
    (put-line (.replace line "PING" "PONG"))))


(defcallback registered [{:keys [command]}]
  (when (and (not (:registered @irc-state)) (= command "001"))
    (dosync (alter irc-state assoc :registered true))))


(defn connect
  "
  Given `host` `port` `nick` `user` and `realname`, starts socket connection
  with `host` on `post`. Updates the `irc-state` ref with `nick` `user` and
  `realname`.

  Return an agent suitable to be passed to `run`.
  "
  [host port nick user realname]
  (dosync (alter irc-state assoc :nick nick :user user :realname realname))
  (agent {::t #'register
          :socket (Socket. host port)
          :irc-state @irc-state}))


(defn run
  "
  Given an agent `a`, bootstraps the IRC connection.
  "
  [a]
  (if-not (get-in @a [:irc-state :connected])
    (do
      (send-off (agent @a) read-lines)
      (send-off (agent @a) write-lines)
      (run (send a assoc-in [:irc-state :connected] true)))
    (send a (fn [{transition ::t :as state}]
              (send *agent* transition)
              state))))


(defstate register [{{:keys [nick user realname]} :irc-state :as state}]
  (dosync (ref-set irc-state (:irc-state state)))
  (put-line (str "NICK " nick))
  (put-line (str "USER " user " 0 * :" realname))
  (assoc state ::t #'take-line))


(defstate take-line [{{:keys [connected registered]} :irc-state :as state}]
  (assoc state
         ::t #'apply-callbacks
         :irc-state @irc-state
         :line (.take input)))


;; TODO: setup multimethod to transition immediately after `take-line`: this
;; method would examine the just-taken line and determine if the connection was
;; healthy or not. In the event that things should go wrong a `disconnected`
;; state should be invoked. Otherwise we should carry on by moving to
;; `apply-callbacks`.


(defstate apply-callbacks [{:keys [line] :as state}]
  (doseq [f (vals @callbacks)]
    (send (agent (parse-line line)) f))
  (assoc state ::t #'take-line))
