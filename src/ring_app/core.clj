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

(def routes
  [["/" {:get  html-handler
         :post html-handler}]
   ["/echo/:id"
    {:get
     (fn [{{:keys [id]} :path-params}]
       (response/ok (str "<p>the value is: " id "</p>")))}]])

(def handler
  (reitit/ring-handler
    (reitit/router routes)))

(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats [handler]
  (-> handler
      (muuntaja/wrap-format)))

(defn -main []
  (jetty/run-jetty
    (-> #'handler
        wrap-nocache
        wrap-reload
        wrap-formats)
    {:port  3000
     :join? false}))

; curl -I localhost:3000
; curl localhost:3000
; curl -H "Content-Type: application/json" -X POST -d '{"id":1}' localhost:3000
; curl -H "Content-Type: application/edn" -X POST -d '{:id 1}' localhost:3000
