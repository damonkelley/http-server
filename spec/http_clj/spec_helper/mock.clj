(ns http-clj.spec-helper.mock
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [http-clj.spec-helper.request-generator :refer [GET]])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

(defn socket [input output]
  (let [connected? (atom true)]
    (proxy [java.net.Socket] []
      (close []
        (reset! connected? false))

      (isClosed []
        (not @connected?))

      (getOutputStream []
        output)

      (getInputStream []
        (ByteArrayInputStream.
          (.getBytes input))))))

(defn socket-server [& args]
  (let [closed? (atom false)]
    (proxy [java.net.ServerSocket] []
      (accept []
        (socket "" (ByteArrayOutputStream.)))

      (close []
        (reset! closed? true))

      (isClosed []
        @closed?))))

(defrecord MockConnection [open input-stream reader]
  connection/Connection
  (write [conn text]
    (assoc conn :written-to-connection (String. text)))

  (read-char [conn]
    (.read reader))

  (read [conn buffer]
    (.read input-stream buffer))

  (close [conn]
    (assoc conn :open false)))

(defn connection
  ([]
   (connection ""))
  ([input]
   (let [input-stream (ByteArrayInputStream. (.getBytes input))
         reader (io/reader input-stream)]
   (MockConnection. true input-stream reader))))

(defrecord MockServer [started stopped]
  component/Lifecycle
  (start [server]
    (assoc server :started true))

  (stop [server]
    (-> server
        (assoc :stopped true)))

  server/Server
  (accept [server]
    (connection (GET "/" {"Host" "www.example.com"}))))

(defn server []
  (MockServer. false false))
