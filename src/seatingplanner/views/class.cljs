(ns seatingplanner.views.class
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.views.editor :as editor]
   [seatingplanner.views.forms :as form]
;; [seatingplanner.db :as db]
   ;; [seatingplanner.helpers :as h]
   ;; [seatingplanner.stylesgarden :as gstyle]
   ;; [seatingplanner.toolsview :as vt]
))




;;==============================
;;STUDENTS =====================
;;==============================
(defn student-list [class-id student]
  [:li [:span.tag student [:button.delete.is-small
                           {:on-click
                            #(re-frame/dispatch [:delete-student class-id student])
                            }
                           ] ]]
  )

(defn students [class-id class]
  [:<>
   [:p.font-bold (str "Students (" (str (count (:students class)))")")]
   [:label
    [:ul
     (for [student  (:students class)]
       ^{:key student} [student-list class-id student])]]]
  )


;;==============================
;;CONSTRAINTS ==================
;;==============================
;;

(defn constraint->string [input]
  (let [[directive name1 name2 distance] input
        result (if (= directive :non-adjacent)
                 (str name1 " and " name2 " are " distance " spaces apart")
                 (str name1 " and " name2 " are within " distance " spaces"))]
    result))

(defn constraints-list [class-id constraint]
  [:li [:span.tag (constraint->string constraint) [:button.delete.is-small
                           {:on-click
                            #(re-frame/dispatch [:delete-constraint class-id constraint])
                            }
]]])

(defn constraints [class-id class]
  [:<>
   [:p.font-bold "Constraints"]
   [:label
    [:ul
     (for [constraint  (:constraints class)]
       ^{:key constraint} [constraints-list class-id constraint])]]]
  )


;;==============================
;;SEATING PLANS ================
;;==============================

(defn get-active-id [data]
  (->> data
       (filter :active)
       (map :id)
       first))


(def data
  {1 {:name "C4 Computer Lap", :layout [[nil :student nil] [:student :student nil] [nil nil "Jack"] [:desk nil :student]]}
   2 {:name "B2 Science Lap", :layout [["Sallly" :student nil] [:student :student nil] [nil nil "Jack"] [:desk nil :student]]}
   3 {:name "Hellow", :layout [[:person nil "Sally"] [nil :desk "Noah"] ["John" "James" nil]]}})

  (select-keys data [2 1])



(defn layout-classes [class-id class]
  (let [
        full-screen @(re-frame/subscribe [:full-screen])
        class-seating-plan-ids (:seating-plans class)
        seating-plans @(re-frame/subscribe [:seating-plans])

        active-class-seating-plan-id  (get-active-id (:seating-plans class))
        active-class-seating-plans (select-keys seating-plans (vec (map #(:id %) class-seating-plan-ids)))
        seating-plan (get seating-plans active-class-seating-plan-id)
        name (:name seating-plan)
        ]

    [:<>
(if full-screen
     [:div;;.card
      {:style {:display "grid"
               :grid-template-columns (str "1fr 6fr 1fr")
               ;; :grid-template-rows (str "repeat(2, 1fr)")
               ;; :border "solid"
               }}

      ;;LAYOUT NAME

      [:div.col-span-full.text-center ;;.card
       [:p.font-bold.text-xl (str (:name class) ": "name)]
       ]
      ;; LAYOUT AND CONSTRAINTS
      [:div;;.card
       [:div.px-2
        ;;TODO change here
        ;; [:div (str class-seating-plan-ids)]
        ;; [:div (str seating-plans)]
        ;; [:div (str class-seating-plan-ids)]
        ;; [:div (str active-class-seating-plans)]
        ;; [:div (str (vec (map #(:id %) class-seating-plan-ids)))]
        [:div.py [editor/layout-dropdown class-id active-class-seating-plan-id active-class-seating-plans]]
        [:div [:button.button.w-full
               {:on-click #(re-frame/dispatch [:toggle-add-layout-form-status])}
               "New Seating Plan"]]
        [form/add-layout class-id]

        [constraints class-id class]
        ;;TODO add ability to add constriains here
        [:div.py-1 [:button.button.w-full
          {:on-click #(re-frame/dispatch [:toggle-add-constraint-form-status])
           } "Add"]]
        [form/add-constraint class-id (:students class)]
        ]]

      ;;EDITOR
      [:div;;.card
       [editor/editor [:seating-plans, active-class-seating-plan-id, :layout] seating-plan]]

      ;;STUDENT AND ALLOCATE
      [:div;;.card

      [:div.px-2 [:button.button
              {:on-click #(re-frame/dispatch [:full-screen-toggle])}
                  "Full Screen"]]
       [:div.px-2 [students class-id class]
        [:button.button.w-full
         {:on-click #(re-frame/dispatch [:toggle-add-student-form-status])

          ;; (js/alert "add students")
          }
         "Add"]
        [form/add-student class-id]
        [:div [:p.font-bold "Allocate"]]
        [:div.py-1 [:button.button.w-full.bg-red-500.py-1
                    {:on-click #(re-frame/dispatch [:organise class-id active-class-seating-plan-id])}
                    "Allocate"]]

        ]]


      ]
     [:div.card
      [:div.card-content
       [editor/layout-display [:seating-plans, active-class-seating-plan-id, :layout] (:layout seating-plan)]
       [:div [:button.button
              {:on-click #(re-frame/dispatch [:full-screen-toggle])}
              "Back"]]
       ]]

     )
     ]


    )
  )




;;==============================
;;CLASS  =======================
;;==============================
(defn main []
  (let [
        class-id @(re-frame/subscribe [:class-id])
        class @(re-frame/subscribe [:get-class class-id])
        ]
    [:<>
     ;; [seating-plans class]
     [layout-classes class-id class]
     [:br]

     ]))
