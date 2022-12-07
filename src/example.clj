(ns example
  (:require [com.wsscode.pathom3.cache :as p.cache]
            [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
            [com.wsscode.pathom3.connect.built-in.plugins :as pbip]
            [com.wsscode.pathom3.connect.foreign :as pcf]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.operation.transit :as pcot]
            [com.wsscode.pathom3.connect.planner :as pcp]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.error :as p.error]
            [com.wsscode.pathom3.format.eql :as pf.eql]
            [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.interface.smart-map :as psm]
            [com.wsscode.pathom3.path :as p.path]
            [com.wsscode.pathom3.plugin :as p.plugin]
            [clojure.string :as str]))

(def users-db
  {1 #:acme.user{:name     "Usuario 1"
                 :email    "user@provider.com"
                 :birthday "1989-10-25"}
   2 #:acme.user{:name     "Usuario 2"
                 :email    "anuser@provider.com"
                 :birthday "1975-09-11"}})

; pull stored user info from id
(pco/defresolver user-by-id [{:keys [acme.user/id]}]
  {::pco/output
   [:acme.user/name
    :acme.user/email
    :acme.user/birthday]}
  (get users-db id))

; extract birth year from birthday
(pco/defresolver birth-year [{:keys [acme.user/birthday]}]
  {:acme.user/birth-year (first (str/split birthday #"-"))})

(pco/defresolver birth-year [env {:keys [acme.user/id]}]
  {:acme.user/all-attrs (->> (p.eql/process env {:acme.user/id id} [:acme.user/name
                                                                    :acme.user/email
                                                                    :acme.user/birthday])
                             vals
                             (str/join ", "))})

;; works
(prn (p.eql/process (->
                     {}
                     (pci/register [user-by-id birth-year] ))
                    {:acme.user/id 1}
                    [:acme.user/all-attrs]))

;; fails
(prn (p.eql/process (->
                     {:com.wsscode.pathom3.error/lenient-mode? true}
                     (pci/register [user-by-id birth-year] ))
                    {:acme.user/id 1}
                    [:acme.user/all-attrs]))






