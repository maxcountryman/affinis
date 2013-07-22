# affinis

An IRC connection layer written in Clojure.

## Usage

```clojure
(require '[affinis.connection :refer [run with-connection]])

(run (with-connection ["irc.example.com" 6667 "nick" "user" "realname"]))
```

## License

Copyright © 2013 Max Countryman

Distributed under the BSD License.
