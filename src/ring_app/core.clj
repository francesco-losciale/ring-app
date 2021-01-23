(ns ring-app.core
  (:require [reitit.ring :as reitit]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.http-response :as response]
            [muuntaja.middleware :as muuntaja]))

(defn html-handler [request-map]
  (response/ok
    (str "<html><body> Hello, your IP is: "
         (:remote-addr request-map)
         "</body></html>")))

(defn json-handler [request]
  (response/ok
    {:result (get-in request [:body-params :id])}))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(def routes
  [["/" {:get  html-handler
         :post html-handler}]
   ["/echo/:id"
    {:get
     (fn [{{:keys [id]} :path-params}]
       (response/ok (str "<p>the value is: " id "</p>")))}]
   ["/api"
    {:middleware [wrap-formats]}
    ["/multiply"
     {:post
      (fn [{{:keys [a b]} :body-params}]
        (response/ok {:result (* a b)}))}]]])

(def handler
  (reitit/routes
    (reitit/create-resource-handler
      {:path "/resources/"})
    (reitit/ring-handler
      (reitit/router routes)
      (reitit/create-default-handler
        {:not-found
         (constantly (response/not-found "404 - Not Found"))
         :method-not-allowed
         (constantly (response/method-not-allowed "405 - Method not allowed"))
         :not-acceptable
         (constantly (response/not-acceptable "406 - Not acceptable"))}))))

(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn -main []
  (jetty/run-jetty
    (-> #'handler
        wrap-nocache
        wrap-reload)
    {:port  3000
     :join? false}))

; curl -I localhost:3000
; curl localhost:3000
; curl -H "Content-Type: application/json" -X POST -d '{"id":1}' localhost:3000
; curl -H "Content-Type: application/edn" -X POST -d '{:id 1}' localhost:3000
; curl -X GET http://localhost:3000/resources/index.html
; curl -X POST http://localhost:3000/echo/4