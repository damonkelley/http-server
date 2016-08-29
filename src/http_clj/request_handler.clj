(ns http-clj.request-handler
  (:require [http-clj.file :as f]
            [http-clj.response :as response]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]))

(defn directory [request dir]
  (let [files (presenter/files request (.listFiles dir))
        html (template/directory files)]
  (response/create request html :headers {"Content-Type" "text/html"})))

(defn head [handler request]
  (-> request
      handler
      (assoc :body nil)))

(defn not-found [request]
  (response/create request "Not Found" :status 404))

(defn method-not-allowed [request]
  (response/create request "Method Not Allowed" :status 405))

(defn file
  ([request io-file] ((file (.getPath io-file)) request))
  ([path]
   (fn [request]
     (response/create request (f/binary-slurp path)))))
