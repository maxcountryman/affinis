(ns affinis.callback)

;; Registered callbacks; applied per-line.
(defonce callbacks (ref {}))


(defmacro defcallback
  "
  Defines a callback function, given a `cname`, `args`, `and body`. Defined
  function is added to the `callbacks` ref. This ref is used by the
  `apply-callbacks` state.
  "
  [cname args & body]
  `(dosync (alter callbacks assoc (keyword '~cname) (fn ~args ~@body))))
