(ns test-datom.core
  (require [clojure.core.async :refer (<!!)]
           [datomic.client :as client]))

(def conn
  (<!! (client/connect
        {:db-name "firstdb"
         :account-id client/PRO_ACCOUNT
         :secret "mysecret"
         :region "none"
         :endpoint "localhost:8998"
         :service "peer-server"
         :access-key "myaccesskey"})))

(defn make-idents
  [x]
  (mapv #(hash-map :db/ident %) x))

(defn make-uuid []
  (java.util.UUID/randomUUID))

(defn db-idents-in [input]
  (<!! (client/transact conn {:tx-data (make-idents input)})))

(defn db-get [input]
  (let [db (client/db conn)]
    (<!! (client/q
          conn
          {:query '[:find ?e
                    :in $ ?item
                    :where
                    [?e :db/ident ?item]]
           :args [db input]}))))

(def blog-schema
  [{:db/ident :post/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :post/content
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

(defn db-in [input]
  (<!! (client/transact conn {:tx-data input})))

(defn create-post [title content]
  (<!! (client/transact
        conn
        {:tx-data [{:post/id (make-uuid)
                    :post/title title
                    :post/content content}]})))

(defn db-get-by-title [input]
  (let [db (client/db conn)]
    (<!! (client/q
          conn
          {:query '[:find ?content ?id
                    :in $ ?item
                    :where
                    [?e :post/title ?item]
                    [?e :post/id ?id]
                    [?e :post/content ?content]]
           :args [db input]}))))
