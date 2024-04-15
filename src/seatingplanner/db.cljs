(ns seatingplanner.db
  (:require
   [cljs.reader :as reader]
   [clojure.edn :as c]
   [re-frame.core :as re-frame]
   ;; [fork.re-frame]
   [cljs.reader]
   [cljs.spec.alpha :as s]
   [seatingplanner.helpers :as h]
   [tick.core :as t]))

(def default-db

  {
   :full-screen true
   :forms {
           :add-class false
           :add-student false
           :add-constraint false
           :add-layout false
           :copy-seating-plan false
           :add-room false
           :copy-room false

           }


   :toggle-spot nil
   :class-id 1
   :classes (sorted-map
             1 {
                :name "Year 7 Digital Technology"
                :students ["Sally" "Jill" "James" "Jack" "John"]
                :constraints [[true :non-adjacent, "James", "John", 2]
                              [false :proximity, "Jill", "Sally", 1]]
                :seating-plans [{:id 2 :active true}
                                {:id 1 :active false}]
                }
             2 {
                :name "Year 10 Digital Technology"
                :students ["Mally" "Jill" "Eleanor" "Alan"]
                :constraints [[true :non-adjacent "Eleanor" "Jill" 2]]
                :seating-plans [{:id 3 :active true}]
                }
             )

   :seating-plans (sorted-map
                   1 {:name "C4 Computer Lap"
                      :layout [[nil :student nil]
                               [:student :student nil]
                               [nil nil "Jack"]
                               [:desk nil :student]]
                      }

                   2 {:name "B2 Science Lap"
                      :layout [["Sallly" :student nil]
                               [:student :student nil]
                               [nil nil "Jack"]
                               [:desk "Jill" :student]]
                      }


                   3 {:name "Hellow"
                      :layout [[:person nil "Sally"]
                               [nil :desk "Noah"]
                               ["John" "James" nil]]}
                   )
   :room-id 1
   :rooms (sorted-map
           1 {
              :name "3x3"
              :layout [
                       [nil nil nil]
                       [nil nil nil]
                       [nil nil nil]]
              }
           )

   })

(get-in default-db [:classes 1 :seating-plans 1 :layout])

(s/conform even? 1)
(s/valid? even? 12)

;;================================
;; SPEC ==========================
;; ===============================
;; (s/def ::seatingplanner.db/db map?)
(s/def ::full-screen boolean?)
;; (s/def ::full-screen int?)
;; (s/def ::full-screen int?)

;; (s/def ::id int?)
;; (s/def ::title string?)
;; (s/def ::done boolean?)
;; (s/def ::todo (s/keys :req-un [::id ::title ::done]))
;; (s/def ::todos (s/and                                       ;; should use the :kind kw to s/map-of (not supported yet)
;;                 (s/map-of ::id ::todo)                      ;; in this map, each todo is keyed by its :id
;;                 #(instance? PersistentTreeMap %)            ;; is a sorted-map (not just a map)
;;                 ))
;; (s/def ::showing                                            ;; what todos are shown to the user?
;;   #{:all                                                    ;; all todos are shown
;;     :active                                                 ;; only todos whose :done is false
;;     :done                                                   ;; only todos whose :done is true
;;     })
;; (s/def ::db (s/keys :req-un [::todos ::showing]))






;; -- Local Storage  ----------------------------------------------------------
(def ls-key "sp-classes")                         ;; localstore key

(defn seatingplanner->local-store
  "Puts todos into localStorage"
  [classes]
  ;; (js/console.log (str "Hello" classes))
  (.setItem js/localStorage ls-key (str classes)))     ;; sorted-map written as an EDN map

;; -- cofx Registrations  -----------------------------------------------------
;; (re-frame/reg-cofx
;;  :local-store-classes
;;  (fn [cofx _]
;;       ;; put the localstore todos into the coeffect under :local-store-todos
;;    (assoc cofx :local-store-classes
;;              ;; read in todos from localstore, and process into a sorted map
;;           (into (sorted-map)
;;                 (some->> (.getItem js/localStorage ls-key)
;;                          (cljs.reader/read-string)    ;; EDN map -> map
;;                          )))))

(re-frame/reg-cofx
 :local-store-classes
 (fn [cofx _]
   (let [
         ;; custom-tag-map {'time/time (fn [x] (t/time x)),
         ;;                 'time/date-time (fn [x] (t/date-time x))}
         data-from-storage (.getItem js/localStorage ls-key)]
     (js/console.log data-from-storage)
     ;; (reader/register-tag-parser! 'time/time (fn [x] (t/time x)))
     ;; (reader/register-tag-parser! 'time/date-time (fn [x] (t/date-time x)))
     (if-let [parsed-data (when data-from-storage
                            (try
                              (cljs.reader/read-string data-from-storage)
                              ;; (c/read-string {:readers custom-tag-map} data-from-storage)
                              (catch js/Error e
                                (js/console.error "Error parsing data from local storage:" e)
                                nil)))]
       (assoc cofx :local-store-classes (into (sorted-map) parsed-data))
       cofx))))

;; (def data
;;  {:rooms {1 {:name "asdfa", :layout [[nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil :student nil nil nil nil nil :student nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil :student nil nil nil nil nil nil :student nil :student] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [:student nil nil :student nil nil nil :student nil :student nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil :student nil nil nil nil :student nil nil :student] [nil nil nil nil nil nil nil nil nil nil nil nil]]}}, :current-route #reitit.core.Match{:template "/class", :data {:coercion #object[reitit.coercion.schema.t_reitit$coercion$schema58900], :name :routes/#class, :view #'seatingplanner.views.class/main}, :result nil, :path-params {}, :path "/class", :query-params {}, :fragment nil, :parameters {:path {}, :query {}, :fragment nil}}, :toggle-spot :student, :class-id 1, :full-screen true, :room-id 1, :seating-plans {1 {:name "asdfasdf", :layout [[nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil :student nil nil nil nil nil :student nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil :student nil nil nil :student nil nil :student nil :student] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [:student nil nil :student nil nil nil :student nil :student nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil :student nil nil nil nil :student nil nil :student] [nil nil nil nil nil nil nil nil nil nil nil nil]]}, 2 {:name "sdf", :layout [[nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil "jon" nil nil nil nil nil :student nil] [nil nil nil nil nil nil nil :student nil nil nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil "adf adf" nil nil nil nil nil nil "sdaf" nil :student] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [:student nil nil :student nil nil nil :student nil :student nil nil] [nil nil nil nil nil nil nil nil nil nil nil nil] [nil nil nil :student nil nil nil nil :student nil nil "adg"] [nil nil nil nil nil nil nil nil nil nil nil nil]]}}, :classes {1 {:name "sdf", :students ("sdaf" "adf adf" "adg" "jon"), :constaints [], :seating-plans ({:id 2, :active true}), :constraints ([:non-adjacent "sdaf" "jon" 2])}, 2 {:name "sdfad", :students ("sdf"), :constaints [], :seating-plans []}, 3 {:name "asdf", :students ("asdfsad"), :constaints [], :seating-plans []}}})
