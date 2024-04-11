(ns seatingplanner.events
  (:require
   [re-frame.core :as re-frame]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h :refer [gdb sdb]]
   [fork.re-frame :as fork]
   [reitit.frontend.easy :as rtfe]
   [clojure.string :as str]
   [tick.core :as t]))

;;===============================================================================
;; -- Interceptors --------------------------------------------------------------
;; ==============================================================================
;;Puts classes into local store
(def ->local-store (re-frame/after db/seatingplanner->local-store))

;; (def interceptors [
;;                    (re-frame/path :class-timers)
;;                    ->local-store])

;; LOCAL STORE
(re-frame/reg-event-fx
 :initialize-db
 [(re-frame/inject-cofx :local-store-classes)]
 (fn [{:keys [db local-store-classes]} _]
   (if (empty? local-store-classes)
     {:db db/default-db}
   {:db (assoc db/default-db :class-timers local-store-classes)}))
)




;;==============================
;; ADD CLASS ===================
;;==============================
(re-frame/reg-event-fx
 :add-class
 (fn [{db :db} [_ {:keys [values dirty path]}]]
   (let [
         classes (:classes db)
         next-id (h/allocate-next-id classes)
         class-name (get values "input")
         area (get values "area")
         students (h/clean-values area)
         ]
     {:db
      (-> db
       (assoc-in [:forms :add-class] false)
       (assoc :classes
              (h/create-item classes next-id {:name class-name
                                              :students students
                                              :constaints []
                                              :seating-plans []
                                              }
                             )))
      })))


(re-frame/reg-sub
 :add-class-form-status
 (fn [db _ ]
   (get-in db [:forms :add-class])
   ))

(re-frame/reg-event-db
 :toggle-add-class-form-status
 (fn [db _ ]
   (let [status (get-in db [:forms :add-class])]
    (assoc-in db [:forms :add-class] (not status)))
   ))

(re-frame/reg-event-db
 :delete-class
 (fn [db [_ class-id]]
   (update-in db [:classes] dissoc class-id)
   )
 )


;;==============================
;; GET CLASS ===================
;;==============================
(re-frame/reg-sub
 :classes
 (fn [db _]
   (:classes db)))

(re-frame/reg-sub
 :class-id
 (fn [db _]
   (:class-id db)))


;;==============================
;; ADD STUDENT =================
;;==============================
(re-frame/reg-event-db
 :delete-student
 (fn [db [_ class-id name]]
   (let [students (get-in db [:classes class-id :students])
         new-students (h/remove-item name students)]
     (assoc-in db [:classes class-id :students] new-students)
     )))

(re-frame/reg-event-fx
 :add-student
 (fn [{db :db} [_ class-id {:keys [values dirty path]}]]
 (let [student-name (get values "input")
       students (get-in db [:classes class-id :students])]
   {:db
    (-> db
        (assoc-in [:forms :add-student] false)
        (assoc-in [:classes class-id :students] (conj students student-name) )
        )
    }
   )))

 (re-frame/reg-sub
  :add-student-form-status
  (fn [db _ ]
    (get-in db [:forms :add-student])
    ))

(re-frame/reg-event-db
 :toggle-add-student-form-status
 (fn [db _ ]
   (let [status (get-in db [:forms :add-student])]
    (assoc-in db [:forms :add-student] (not status)))
   ))


;;==============================
;; CONSTRAINTS =================
;;==============================

(re-frame/reg-event-db
 :delete-constraint
 (fn [db [_ class-id constraint]]
   (let [constraints (get-in db [:classes class-id :constraints])
         new-constraints (h/remove-item constraint constraints)
         ]
     (assoc-in db [:classes class-id :constraints] new-constraints)
     )))

(re-frame/reg-event-fx
 :add-constraint
 (fn [{db :db} [_ class-id {:keys [values dirty path]}]]
   (let [s1 (get values "s1")
         s2 (get values "s2")
         type (get values "type")
         space (get values "space")
         new-constraint [(keyword type) s1 s2 (int space)]
         constraints (get-in db [:classes class-id :constraints])]
       {:db
      (-> db
          (assoc-in [:forms :add-constraint] false)
          (assoc-in [:classes class-id :constraints] (conj constraints new-constraint) )
          )}
     )))

 (re-frame/reg-sub
  :add-constraint-form-status
  (fn [db _ ]
    (get-in db [:forms :add-constraint])
    ))

(re-frame/reg-event-db
 :toggle-add-constraint-form-status
 (fn [db _ ]
   (let [status (get-in db [:forms :add-constraint])]
    (assoc-in db [:forms :add-constraint] (not status)))
   ))



;;==============================
;; LAYOUTS =====================
;;==============================

(re-frame/reg-event-db
 :delete-layout
 (fn [db [_ class-id id]]
   (let [class-seating-plans (get-in db [:classes, class-id, :seating-plans])
         updated-class-seating-plans (filter #(not= id (:id %)) class-seating-plans)]
   (-> db
       (update-in [:seating-plans] dissoc id)
       (assoc-in [:classes, class-id :seating-plans] updated-class-seating-plans)
       ))))


(defn add-id-change-active-status [data new-id]
  (let [updated-data (map #(assoc % :active false) data)]
    (conj updated-data {:id new-id, :active true})))

(re-frame/reg-event-fx
 :add-layout
 (fn [{db :db} [_ class-id {:keys [values dirty path]}]]
   (let [
         ;; ADDING TO SEATING PLAN

         room-id (int (get values "room"))
         seating-plans (:seating-plans db)

         new-seating-plan-id (h/allocate-next-id seating-plans)
         new-seating-plan-layout (get-in db [:rooms, room-id :layout])
         new-seating-plan-name (get values "name")
         new-seating-plan {:name new-seating-plan-name, :layout new-seating-plan-layout}
         ;; ;; ADDING TO CLASS SEATING PLAN
         class-seating-plans (get-in db [:classes, class-id, :seating-plans])
         new-class-seating-plans (add-id-change-active-status class-seating-plans new-seating-plan-id)
         ;; new-layout [(keyword type) s1 s2 (int space)]
         ;; width (get values "w")
         ;; height (get values "h")
         ;; layouts (get-in db [:classes class-id :layouts])
         ]
     (js/console.log room-id)
     {:db

      (-> db
          (assoc-in [:forms :add-layout] false)
          (assoc-in [:classes, class-id, :seating-plans] new-class-seating-plans)
          (assoc :seating-plans (h/update-item seating-plans new-seating-plan-id new-seating-plan))
          )}
     )))

 (re-frame/reg-sub
  :add-layout-form-status
  (fn [db _ ]
    (get-in db [:forms :add-layout])
    ))

(re-frame/reg-event-db
 :toggle-add-layout-form-status
 (fn [db _ ]
   (let [status (get-in db [:forms :add-layout])]
    (assoc-in db [:forms :add-layout] (not status)))
   ))





;;==============================
;; FULL SCREEN =================
;;==============================
(re-frame/reg-event-db
 :full-screen-toggle
 (fn [db _ ]
    (assoc db :full-screen (not (:full-screen db))))
   )

(re-frame/reg-sub
 :full-screen
 (fn [db _ ]
    (:full-screen db))
   )



;;==============================
;; SEATING PLANS ===============
;;==============================
(re-frame/reg-sub
 :seating-plans
 (fn [db _ ]
   (get db :seating-plans)
   ))

;;==============================
;; GET APP-DB ==================
;;==============================
(re-frame/reg-sub
 :app-db
 (fn [db _]
   db))

;;==============================
;; ROOMS =======================
;;==============================
(re-frame/reg-event-db
 :room-id
 (fn [db [_ id]]
   (assoc db :room-id id)
   ))

(re-frame/reg-sub
 :room-id
 (fn [db _]
   (:room-id db)))

(re-frame/reg-sub
 :rooms
 (fn [db _]
   (:rooms db)))

(re-frame/reg-sub
 :room
 (fn [db [_ id]]
   (get-in db [:rooms id])))


(re-frame/reg-event-fx
 :add-room
 (fn [{db :db} [_ {:keys [values dirty path]}]]
   (let [
         rooms (:rooms db)
         next-id (h/allocate-next-id rooms)
         room-name (get values "input")
         width (get values "w")
         height (get values "h")]
     {:db
      (-> db
          (assoc-in [:forms :add-room] false)
          (assoc :rooms
                 (h/create-item rooms next-id {:name room-name
                                               :layout (vec (repeat height (vec (repeat width nil))))
                                               })))})))

(re-frame/reg-event-db
 :delete-room
 (fn [db [_ room-id]]
   (update-in db [:rooms] dissoc room-id)
   )
 )

(re-frame/reg-sub
 :add-room-form-status
 (fn [db _]
   (get-in db [:forms :add-room])
   ))

(re-frame/reg-event-db
 :toggle-add-room-form-status
 (fn [db _ ]
   (let [status (get-in db [:forms :add-room])]
     (assoc-in db [:forms :add-room] (not status)))
   ))


;;==============================
;; EDITOR ======================
;;==============================

;;TODO write some constructors

(re-frame/reg-event-db
 :clear-all
 (fn [db [_ path]]
   (let [layout (get-in db path)
         num-rows (count layout)
         num-columns (count (first layout))
         new-layout (vec (repeat num-rows (vec (repeat num-columns nil))))]
     (assoc-in db path new-layout)
     )))



(re-frame/reg-event-db
 :change-cell
 (fn [db [_ path row column]]
   (let [toggle-spot (:toggle-spot db)
         layout (get-in db path)
         new-layout (assoc-in layout [row column] toggle-spot)]
     (assoc-in db path new-layout))))



(re-frame/reg-event-db
 :toggle-spot
 (fn [db [_ type]]
   (assoc db :toggle-spot type)))

(re-frame/reg-sub
 :toggle-spot
 (fn [db _]
   (:toggle-spot db)))

(re-frame/reg-event-db
 :change-layout
 (fn [db [_ class-id active-layout-id]]
   (let [seating-plans (get-in db [:classes, class-id, :seating-plans])
         new-seating-plans (map #(if (= (:id %) (int active-layout-id))
                                   (assoc % :active true)
                                   (assoc % :active false)) seating-plans
                                )]
     (assoc-in db [:classes, class-id, :seating-plans] new-seating-plans)
     )))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (def map   {:seating-plans (sorted-map
;;                             1 {:name "C4 Computer Lap"
;;                                :layout [[nil :student nil]
;;                                         [:student :student nil]
;;                                         [nil nil "Jack"]
;;                                         [:desk nil :student]]
;;                                }

;;                             2 {:name "B2 Science Lap"
;;                                :layout [["Sallly" :student nil]
;;                                         [:student :student nil]
;;                                         [nil nil "Jack"]
;;                                         [:desk "Jill" :student]]
;;                                }
;;                             )
;;             })

;; (update-in map [:seating-plans] dissoc 1)

;; (def foo '({:id 2, :active false} {:id 1, :active true}))

;; (def foo '({:id 2, :active false} {:id 1, :active true}))
;; (filter #(not= 1 (:id %)) foo)







;;     layout (get-in db path)
;;     new-layout (assoc-in layout [row column] toggle-spot)]
;; (assoc-in db path new-layout))))

(re-frame/reg-event-db
 :add-column
 (fn [db [_ path]]
   (let [
         layout (get-in db path)
         new-layout (into [] (map #(conj % nil) layout))]
     (assoc-in db path new-layout ))))

(re-frame/reg-event-db
 :remove-column
 (fn [db [_ path]]
   (let [
         layout (get-in db path)
         new-layout (into [] (map #(pop %) layout))]
     (assoc-in db path new-layout ))))

(re-frame/reg-event-db
 :add-row
 (fn [db [_ path]]

   (let [
         layout (get-in db path)
         new-row (-> layout
                     first
                     count
                     (repeat nil)
                     vec)
         new-layout (conj layout new-row)]
     (assoc-in db path new-layout ))))

(re-frame/reg-event-db
 :remove-row
 (fn [db [_ path]]
   (let [
         layout (get-in db path)
         new-layout (pop layout)]
     (assoc-in db path new-layout ))))

;;==============================
;; MODIFY ROOM =================
;;==============================
;; (re-frame/reg-sub
;;  :classroom
;;  (fn [db _]
;;    (:classroom db)
;;    )
;;  )






;;TODO NEEDS TO BE UPDATED
(re-frame/reg-sub
 :class-temp
 :<- [:classes]
 (fn [classes _]
   (first classes)
   ))

(re-frame/reg-sub
 :get-class
 :<- [:classes]
 (fn [classes [_ id]]
   ;; (js/alert
    ;; (get classes id)
   (h/read-item classes id)
   )
 )

(re-frame/reg-event-db
 :class-id
 (fn [db [_ id]]
   (assoc db :class-id id)
   )
 )

;;SELECT CLASS ===
;;TODO redo this function
(re-frame/reg-event-db
 :organise
 (fn [db [_ class-id seating-plan-id]]
   (let [
         class (get-in db [:classes, class-id])


         students (:students class)
         constraints (:constraints class)
         layout (get-in db [:seating-plans, seating-plan-id, :layout])
         updated-room (h/generate-seating-plan layout students constraints)]

;; TODO if it cannot form a allocation, it will show the followin
     ;; [[nil :student :student] [nil :student :student] [nil nil nil]]


     ;; (js/alert (str "class " class "students "students" constraints " constraints " layout " layout))
     ;; (js/alert (str class_s)  "CONS "))
     ;; (js/alert (str "room " room " students " students " constraints " con " updated room" updated-room ))
     ;; (assoc db :class-id id))
     (assoc-in db [:seating-plans seating-plan-id, :layout] updated-room)

     )
   ))