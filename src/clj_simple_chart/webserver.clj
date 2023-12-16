(ns clj-simple-chart.webserver
  (:require [aleph.http :as http]
            [manifold.stream :as s])
  (:import (java.net InetSocketAddress)))

(defonce websockets (atom #{}))

(defonce prev-svg (atom nil))

(defn handle-ws [req]
  (let [sock @(http/websocket-connection req)]
    (swap! websockets conj sock)
    (when @prev-svg
      (s/put! sock @prev-svg))
    (s/on-closed sock (fn [] (swap! websockets disj sock)))
    {:status  200
     :headers {"content-type" "text/plain"}
     :body    "okii"}))

(defn handler [{:keys [uri] :as ctx}]
  ;(println "uri:" uri)
  (cond (= "/" uri)
        {:status  200
         :headers {"content-type" "text/html"}
         :body    (slurp "resources/index.html")}

        (= "/onchange" uri)
        (handle-ws ctx)

        :else
        {:status  404
         :headers {"content-type" "text/plain"}
         :body    (str "Did not find: '" uri "'")}))

(defonce server
         (do
           (let [s (http/start-server
                     (fn [ctx] (handler ctx))
                     {:socket-address (InetSocketAddress. "127.0.0.1" 8080)})]
             (println "started webserver on http://localhost:8080")
             s)))

(defn push-svg! [svg]
  (reset! prev-svg svg)
  (doseq [sock @websockets]
    (s/put! sock svg)))
