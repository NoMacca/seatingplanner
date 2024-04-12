(ns seatingplanner.views.class
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [fontawesome.icons :as icons]
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
  [:li [:div.flex.p-2.hover:bg-gray-100
        [:p student ]
        [:div.flex-grow]
        [:button.delete.is-small
         {:on-click
          #(re-frame/dispatch [:delete-student class-id student])
          }
         ]]]
  )

(defn students [class-id class]
  [:<>

[:div.flex.bg-gray-100
   [:p.text-lg (str "Students (" (str (count (:students class)))")")]
    [:div.flex-grow]
    [:button.button.rounded-full
     {:on-click #(re-frame/dispatch [:toggle-add-student-form-status])
      }
     (icons/render (icons/icon :fontawesome.solid/plus) {:size 15})

    ]]
    [:ul.menu-list
     (for [student  (:students class)]
       ^{:key student} [student-list class-id student])]]
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






(defn classes-list []
  (let [classes @(re-frame/subscribe [:classes])]
[:p "Classes"]


         ;; (for [[class-id class] classes]
         ;;   ^{:key class-id} [class-layout class-id class])]
    (str classes)
    ))



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
       [:div ;;.card
        {:style {:display "grid"
                 :grid-template-columns (str "1fr 6fr")
                 ;; :grid-template-rows (str "repeat(2, 1fr)")
                 ;; :border "solid"
                 }}
        ;;Navigation Menue
        [:aside.menu.p-4



         ;;CLASSES
         [classes-list]

         ;;STUDENT AND ALLOCATE
         ;; LAYOUT AND CONSTRAINTS


         ;;LAYOUT NAME
         [:p.font-bold.text-xl
          (str (:name class))]
         [students class-id class]

         [form/add-student class-id]


         ;; LAYOUT AND CONSTRAINTS

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

         ]
        ;;EDITOR
        [:div ;;.card
         [editor/complete-editor [:seating-plans, active-class-seating-plan-id, :layout] seating-plan
          class-id active-class-seating-plan-id
          ]]



        ]





       [:div.card
        [:div.card-header

         [:div.card-header-title name]

         [:div.card-header-icon
          [:div.px-2
           [:button.button.is-white
            {:on-click #(re-frame/dispatch [:full-screen-toggle])}
            (icons/render (icons/icon :fontawesome.solid/minimize) {:size 20})]
           ]]]
        [:div.card-content
         [editor/layout-display [:seating-plans, active-class-seating-plan-id, :layout] (:layout seating-plan)]
         ]

        ]




       ;; [:div.card
       ;;  [:div.card-content
       ;;   [editor/layout-display [:seating-plans, active-class-seating-plan-id, :layout] (:layout seating-plan)]
       ;;   [:div [:button.button
       ;;          {:on-click #(re-frame/dispatch [:full-screen-toggle])}
       ;;          "Back"]]
       ;;   ]]






       )
     ]


    ))




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
