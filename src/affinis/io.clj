(ns affinis.io
  (:require [clojure.core.async :refer [>! <! go]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as logging]))


(defn put-line [{:keys [irc-state]} line] (go (>! (:output @irc-state) line)))


(defn read-lines
  "
  Loops over a socket connection, reading lines and putting them into a queue.
  "
  [{:keys [socket irc-state]}]
  (go (let [{input :input} @irc-state]
        (doseq [line (line-seq (io/reader socket))]
          (>! input line)
          (logging/info line)))))


(defn write-lines
  "
  Takes a lines off the `output` queue and writes it to a socket connection.
  "
  [{:keys [socket irc-state]}]
  (go (while true
        (when-let [line (<! (:output @irc-state))]
          (binding [*out* (io/writer socket)]
            (println line))
          (logging/info (str ">>> " line))))))
