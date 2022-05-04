(ns pingcrm.models.organizations
  (:require [honey.sql :as h]
            [honey.sql.helpers :refer [where]]
            [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(defn list-organizations [db]
  (let [query (h/format {:select [:id :name]
                         :from [:organizations]
                         :where [:and
                                 [:= :account_id 1]
                                 [:<> :account_id nil]
                                 [:= :deleted_at nil]]
                         :order-by [:name]})]
    (jdbc/execute! db query)))

(defn retrieve-and-filter-organizations
  ([db filters]
   (retrieve-and-filter-organizations db filters nil))
  ([db {:keys [search trashed]} offset]
   (let [query (h/format
                (cond-> {:select (if offset [:*] [[:%count.* :aggregate]])
                         :from [:organizations]
                         :order-by [:name]}
                  offset (merge {:limit 10
                                 :offset offset})
                  search (where [:like :name (str "%" search "%")])
                  true (where (case trashed
                                "with" nil
                                "only" [:<> :deleted_at nil]
                                [:= :deleted_at nil]))))]
     (jdbc/execute! db query))))

(defn get-organization-by-id
  [db id]
  ;;TODO: Use with-open for better performance
  (let [organization (sql/get-by-id db :organizations id)
        contacts (sql/find-by-keys db :contacts {:organization_id id} {:columns [:id ["first_name || \" \" || last_name" :name] :city :phone]})]
    (assoc organization :contacts contacts)))

(defn insert-organization!
  [db organization]
  (let [query (h/format {:insert-into :organizations
                         :values [(merge organization {:created_at :current_timestamp
                                                       :updated_at :current_timestamp})]})]
    (jdbc/execute-one! db query)))

(defn update-organization!
  [db organization id]
  (sql/update! db :organizations organization {:id id}))

(defn soft-delete-organization!
  [db id]
  (let [query (h/format {:update :organizations
                         :set {:deleted_at :current_timestamp
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))

(defn restore-deleted-organization!
  [db id]
  (let [query (h/format {:update :organizations
                         :set {:deleted_at nil
                               :updated_at :current_timestamp}
                         :where [:= :id id]})]
    (jdbc/execute-one! db query)))
