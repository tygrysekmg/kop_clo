(ns pingcrm.shared.pagination
  (:require [clojure.string :as str]))

(defn pagination-links [uri query-string current-page total per-page]
  (let [uri (str uri "?" (when query-string (str/replace query-string #"&page=.*" "")) "&page=")
        page-number (/ total per-page)
        previous-link {:url (when (> current-page 1) (str uri (dec current-page))) :label "&laquo; Previous" :active nil}
        next-link {:url (when (< current-page page-number) (str uri (inc current-page))) :label "Next &raquo;" :active nil}
        links (for [item (range 1 (inc page-number))]
                {:url (str uri item) :label (str item) :active (when (= item current-page) true)})]
    (flatten [previous-link links next-link])))
