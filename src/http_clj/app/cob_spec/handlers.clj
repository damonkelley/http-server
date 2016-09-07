(ns http-clj.app.cob-spec.handlers
  (:require [http-clj.request-handler :as handler]
            [http-clj.request-handler.filesystem :as filesystem]
            [http-clj.file :as file-helper]
            [http-clj.response :as response]
            [clojure.core.match :refer [match]]
            [clojure.string :as string]))

(defn -static [request file]
  (let [method (:method request)
        directory? (.isDirectory file)
        exists? (.exists file)]

    (match [method directory? exists?]
      ["GET" true _] (filesystem/directory request file)
      ["GET" false true] (filesystem/file request (.getPath file))
      ["PATCH" false true] (filesystem/patch-file request (.getPath file))
      [_ _ _] (handler/not-found request))))

(defn static [request directory]
  (let [file (file-helper/resolve directory (:path request))]
    (-static request file)))

(defn log [request log]
    (response/create request (.toString log) :status 200))

(defn submit-form [request cache]
  (reset! cache (String. (:body request)))
  (response/create request ""))

(defn last-submission [request cache]
  (response/create request @cache))

(defn options [& allowed-options]
  (let [allow (string/join "," allowed-options)]
    #(response/create % "" :headers {"Allow" allow})))

(defn- present-query-params [query-params]
  (string/join "\n" (map #(string/join " = " %) query-params)))

(defn parameters [{:keys [query-params] :as request}]
  (response/create request (present-query-params query-params)))
