# affinis

An IRC connection layer written in Clojure.

## Usage

```clojure
(require '[affinis.connection :refer [connect run]])

(run (connect "irc.example.com" 6667 "nick" "user" "realname"))
```

## License

Copyright © 2013 Max Countryman

Distributed under the BSD License.
