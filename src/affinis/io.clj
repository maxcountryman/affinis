(ns affinis.io
  (:import java.util.concurrent.LinkedBlockingQueue))

;; Input queue. Holds messages we've received from the socket. This is
;; populated in another thread by `read-lines`. Later this is read from by the
;; `take-line` state, a state held by the agent active on the thread.
(defonce input (LinkedBlockingQueue.))

;; Output queue. Holds messages we're sending to the socket. This is populated
;; by invoking `put-line`. In another thread, `write-lines` takes off this
;; queue and sends those lines to the socket.
(defonce output (LinkedBlockingQueue.))


(defn put-line [line] (.put output line))
