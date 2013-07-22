(ns affinis.fsm
  (:require [affinis.irc :as irc]
            [affinis.parser :refer [irc-line->map]]
            [clojure.core.async :refer [<! <!! go]]
            [clojure.tools.logging :as logging]))


(declare apply-callbacks take-line)


(defn run-states [a]
  (defn handler-fn [a ex]
    (logging/error (str "Error: " ex " With agent: " @a)))
  (set-error-handler! a handler-fn)
  (send a (fn [{transition :affinis.fsm/t :as context}]
            (send *agent* transition)
            context)))


(defmacro defstate
  "
  Defines a state function, given a `sname`, `args`, and `body`. State
  functions are wrapped with calls to `run`, providing `*agent*` as its only
  argument.
  "
  [sname args & body]
  `(defn ~sname ~args (try ~@body (finally (~run-states *agent*)))))


;; States


(defstate register [{:keys [irc-state] :as context}]
  (let [{:keys [nick user realname]} @irc-state]
    (irc/nick context nick)
    (irc/user context user realname)
    (assoc context ::t #'take-line)))


(defstate take-line [{:keys [irc-state] :as context}]
  (let [{input :input} @irc-state]
    (assoc context ::t #'apply-callbacks :line (<!! (go (<! input))))))


(defstate apply-callbacks [{:keys [line irc-state] :as context}]
  (doseq [f (:callbacks @irc-state)]
    (->
      (agent (merge context (irc-line->map line)))
      (send f)))
  (assoc context ::t #'take-line))
