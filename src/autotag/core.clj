(ns autotag.core
  (use [clojure.xml :only (parse)])
	(:require [clj-http.client :as client]
						[cheshire.core :as cheshire]
            [clojure.xml :as xml])
  (:import  [java.net URLEncoder]))

(defn fetch-page
  "Fetch a Facebook Graph Page by page-id"
  [page-id]
  (let [page-response (client/get (str "http://graph.facebook.com/" page-id))
        page-body (:body page-response)]
  page-body))

(defn as-json
  "Parse a JSON string and return it as JSON"
  [page-body]
  (cheshire/parse-string page-body true))

(defn feed-url
	[page-id access-token]
  (str "https://graph.facebook.com/" page-id "/posts?limit=200&access_token=" access-token))

(defn fetch-page-feed
  "Fetch the given Page-ids feed posts from Facebook"
  [page-id access-token]
  (let [page-response (client/get (feed-url page-id access-token))
        page-body (:body page-response)]
  page-body))

(defn access-token
  []
  (let [examples-page (client/get "https://developers.facebook.com/docs/reference/api/examples/")
				token "AAACEdEose0cBAK0g94eAJl0Qu6JFZCiPXYEmM9BEdiguf9TxvuKidfmPPUifWL5vJ7yle3DQaTHYiXgJhj3kxdeuevImk7bdD5IoaBgZDZD"]
  ;(map #(% 1) (re-seq #"\?access_token=([a-zA-Z0-9]+)" examples-page)))
  token))

(defn page-posts
  [page-feed]
  (let [id-messages (map (fn[{:keys [id message story]}] [id (or message story)]) (:data page-feed))]
  id-messages))

(defn encode-params [request-params]
  (let [encode #(URLEncoder/encode (str %) "UTF-8")
        coded (for [[n v] request-params] (str (encode n) "=" (encode v)))]
    (apply str (interpose "&" coded))))

(defn tag-post
  [[post-id content]]
  (do (println post-id)
  (:body (client/get (str "http://spotlight.dbpedia.org/rest/spot?" (encode-params {"text" content}))))))

(defn parse-xml-tags
  [document]
  (do (println document)
  (for [tag (xml-seq (parse document))
        :when (= :surfaceForm (:tag tag))]
    (:name (:attrs tag)))))

(->> (access-token)
		(fetch-page-feed "23550666633")
    (as-json)
    (page-posts)
    (first)
    (tag-post)
    (parse-xml-tags)
    (print-str))




