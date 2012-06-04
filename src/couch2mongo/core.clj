(ns couch2mongo.core
  "Being able to map incoming dependencies to our specific names reminds me of Inferno's mechanisms for mapping
   external namespaces to process name spaces."
  (:require [com.ashafa.clutch :as couch]
	    [somnium.congomongo :as mongo]))

(def config
     (read-string (slurp "config.clj")))

(def myDashName (:mydashname config))

;; Each of these will have an :id, a :key and a :value.
;; The :value will have a :DateTime and a :DashValue.
(def couchdashes
     (couch/with-db (config :couchurl) 
       (couch/get-view "dashboards" "dashvals")))

(def dashvalues
     (map :value couchdashes))

(def conn (mongo/make-connection (config :mongodbname) :host (config :mongodbhost)))


;; This also dumps the results since the insert! is the last call in -main.
;; Not great since we have 15,000 rows of spew.  Would be nice to have a summary.
(defn -main [ & args]
  (mongo/set-connection! conn)
  (println "Adding " (count dashvalues) " values to " myDashName)
  (mongo/insert! :dashes {:name myDashName :values dashvalues }))


;; possible ideas for the future
;; 1) Convert dates to something useful (even though json doesn't have a date).
;;    See what the charting api needs.
;; 2) Merge values from current doc instead of replacing.