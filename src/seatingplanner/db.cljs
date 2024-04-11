(ns seatingplanner.db
  (:require
   [cljs.reader :as reader]
   [clojure.edn :as c]
   [re-frame.core :as re-frame]
   ;; [fork.re-frame]
   [seatingplanner.helpers :as h]
   [tick.core :as t]))

(def default-db

  {
   :full-screen false
   :forms {
           :add-class false
           :add-student false
           :add-constraint false
           :add-layout false
           :add-room false
           }


   :toggle-spot nil
   :class-id 1
   :classes (sorted-map
             1 {
                :name "Year 7 Digital Technology"
                :students ["Sally" "Jill" "James" "Jack" "John"]
                :constraints [[:non-adjacent, "James", "John", 2]
                              [:proximity, "Jill", "Sally", 1]]
                :seating-plans [{:id 2 :active true}
                                {:id 1 :active false}]
                }
             2 {
                :name "Year 10 Digital Technology"
                :students ["Mally" "Jill" "Eleanor" "Alan"]
                :constraints [[:non-adjacent "Eleanor" "Jill" 2]]
                :seating-plans [{:id 3 :active true}]
                })

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
           0 {:name "Please Select"
              :layout [[nil nil nil]
                       [nil nil nil]
                       ]
              }
           1 {
              :name "C4 Computer Lap"
              :layout [[nil :student nil]
                       [:student :student nil]
                       [nil nil :student]
                       [:desk nil :student]]
              }
           2 {
              :name "C4 Science Lap"
              :layout [[:student :student nil]
                       [:student :student nil]
                       [nil :desk :student]
                       [:desk nil :student]]
              }
           )

   })

(get-in default-db [:classes 1 :seating-plans 1 :layout])





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
