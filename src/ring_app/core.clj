(ns ring-app.core)

(defn handler [request-map]
  {:status 200
   :headers {"Content/type" "text/html"}
   :body (str "<html><body> your IP is: "
              (:remote-addr request-map)
              "</body></html>")})
(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
