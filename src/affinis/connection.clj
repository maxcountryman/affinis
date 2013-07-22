(ns affinis.connection
  (:require [affinis.fsm :refer [register run-states]]
            [affinis.io :refer [put-line read-lines write-lines]]
            [clojure.core.async :refer [chan]]
            [clojure.tools.logging :as logging])
  (:import [java.net Socket ServerSocket]))


;; ## Connection logic


(def ^:dynamic *irc-state*)


(defmacro defcallback
  [cname args & body]
  `(dosync
     (alter *irc-state* update-in [:callbacks] conj (fn ~args ~@body))))


;; Manually define the essential PONG callback here.
;;
;; While we could use ourspecialized macro, it's cumbersome to inline the
;; definition with the `with-connection` macro. Instead, it's simple enough to
;; define a private function here and populate the callbacks set with it by
;; default.
(defn- pong [{:keys [command line] :as context}]
  (when (= command "PING")
    (put-line context (.replace line "PING" "PONG"))))


;; In order to maintain state in a way that does not rely on globals and allows
;; for nice things like interactive reloading, we use a contructor pattern to
;; bootstrap our initial IRC state. This is passed around by agents who are
;; responsible for deref'ing and manipulating its values.
(defn construct-irc-state [nick user realname]
  (ref {:nick nick
        :user user
        :realname realname
        :callbacks #{pong}
        :input (chan)
        :output (chan)}))


;; Our API looks something like this:
;;
;; => (def conn
;;      (with-connection ["irc.example.com" 6667 "foo" "bar" "Foo Bar"]
;;        (defcallback foo [{:keys [command]}]
;;          (println command))
;;          ;; Any additional callbacks...
;;          ))
;;
;; => (run conn)
;;
;; Essentially we are wrapping the `defcallback` calls with a binding over
;; *irc-state* which contains our set of callbacks. Ideally, we would eschew
;; all global state, but a dynamic var which is contextually bound seems like
;; a decent compromise.
;;
;; Note that the value of irc-state is created upon invocation. This allows us
;; to pass around state cleanly without worrying about tricks like `defonce` to
;; allow for interactive reloading.
;;
;; Finally this macro will return an agent which is to be passed off to the
;; `run` function, as in the above example. `run` bootstraps the reader and
;; writing procedures in separate threads and then kicks off the state machine
;; which will handle application IO.
(defmacro with-connection
  [[host port nick user realname] & callbacks]
  `(let [irc-state# (construct-irc-state ~nick ~user ~realname)]
     (binding [*irc-state* irc-state#] ~@callbacks)
     (agent {:affinis.fsm/t #'register
             :socket (Socket. ~host ~port)
             :irc-state irc-state#})))


(defn run [a]
  (doseq [f [read-lines write-lines]] (f @a))
  (run-states a))
