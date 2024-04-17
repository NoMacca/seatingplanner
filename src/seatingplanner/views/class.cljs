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
(defn student-list [class-id student active-class-seating-plan-id]
  [:li [:div.flex.p-2.gap-2;;button.hover:bg-blue-500
        [:p student]
        [:div.flex-grow]
        [:button
         {:title "Add to seating plan"
          :on-click #(re-frame/dispatch [:student-to-seating-plan student active-class-seating-plan-id])}
         (icons/render (icons/icon :fontawesome.solid/plus) {:size 15})]

        [:button.delete.is-small ;;.order-first;.;.invisible.hover:visible
         {:on-click #(re-frame/dispatch [:delete-student class-id student])
          }]]])

(defn students [class-id class active-class-seating-plan-id]
  [:<>
   [:p.menu-label (str "Students (" (str (count (:students class)))")")]
   [:ul.menu-list
     (for [student  (:students class)]
       ^{:key student} [student-list class-id student active-class-seating-plan-id])]
   [:button.button.rounded-full
    {
     :title "Add a student"
     :on-click #(re-frame/dispatch [:toggle-add-student-form-status])}
    (icons/render (icons/icon :fontawesome.solid/plus) {:size 15})]
   [form/add-student class-id active-class-seating-plan-id]
   ]
  )
;;==============================
;;CONSTRAINTS ==================
;;==============================
(defn constraint->string [input]
  (let [[checked? directive name1 name2 distance] input
        result (if (= directive :non-adjacent)
                 (str name1 " and " name2 " are " distance " spaces apart")
                 (str name1 " and " name2 " are within " distance " spaces"))]
    result))

(defn constraints-list [class-id constraint]
  [:li [:div.flex.p-2;;.hover:bg-blue-500
        [:p
        [:input {:type "checkbox"
                 :checked (first constraint)
                 :on-change #(re-frame/dispatch [:toggle-constraint class-id constraint])
                 }]
        (str " " (constraint->string constraint))]
        [:div.flex-grow]
         [:button.delete.is-small
          {:on-click
           #(re-frame/dispatch [:delete-constraint class-id constraint])
           }
          ]]])

(defn constraints [class-id class]
  [:<>
   [:p.menu-label "Constraints"]
    [:ul.menu-list
     (for [constraint  (:constraints class)]
       ^{:key constraint} [constraints-list class-id constraint])]

  [:div.py-1 [:button.button.rounded-full
              {:on-click #(re-frame/dispatch [:toggle-add-constraint-form-status])
               :title "Create a rule or limit on how students are assigned to the seating plan"

               }
              (icons/render (icons/icon :fontawesome.solid/plus) {:size 15})]]

   ]
  )

;;==============================
;;SEATING PLANS ================
;;==============================

(defn get-active-id [data]
  (->> data
       (filter :active)
       (map :id)
       first))


;; (def data
;;   {1 {:name "C4 Computer Lap", :layout [[nil :student nil] [:student :student nil] [nil nil "Jack"] [:desk nil :student]]}
;;    2 {:name "B2 Science Lap", :layout [["Sallly" :student nil] [:student :student nil] [nil nil "Jack"] [:desk nil :student]]}
;;    3 {:name "Hellow", :layout [[:person nil "Sally"] [nil :desk "Noah"] ["John" "James" nil]]}})

;;   (select-keys data [2 1])


(defn class-m [class-id {:keys [name students]} active-class-id]
  [:li [:a {:class (if  (= active-class-id class-id) "is-active" "")
            :on-click #(re-frame/dispatch [:class-id class-id])
            }
        name]]

  )

(defn classes-list [active-class-id]
  (let [classes @(re-frame/subscribe [:classes])]
    [:div
     [:p.menu-label
;; {:title "d"
;;  }
      "Classes"]
    [:ol.menu-list
    (for [[class-id class] classes]
      ^{:key class-id} [class-m class-id class active-class-id])
     ]]
    ))

(defn seating-plan-m [seating-plan-id {:keys [name layout]} active-seating-plan-id class-id]

  [:li [:a {:class (if  (= seating-plan-id active-seating-plan-id) "is-active" "")
            :on-click #(re-frame/dispatch [:change-layout class-id seating-plan-id])
            }
        name]]

  )

(defn seatingplans-list [class-id active-seating-plan-id active-class-seating-plans]
  (let [
        ;; current-seating-plan (get active-class-seating-plans active-seating-plan-id)
        ;; seating-plans-rest (dissoc seating-plans active-seating-plan-id)
        ]
    [:<>
     [:p.menu-label "Seating Plans"]
     [:ol.menu-list
      (for [[seating-plan-id seating-plan] active-class-seating-plans]
        ^{:key seating-plan-id} [seating-plan-m seating-plan-id seating-plan active-seating-plan-id class-id])
      ]

    [:div.py-1 [:button.button.rounded-full
                {:on-click #(re-frame/dispatch [:toggle-add-layout-form-status])
                 :title "Create a seating plan for the selected class"}
                (icons/render (icons/icon :fontawesome.solid/plus) {:size 15})]]

    [form/add-layout class-id]]
  )
  )


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
                 :grid-template-columns (str "1fr 3fr")
                 ;; :grid-template-rows (str "repeat(2, 1fr)")
                 ;; :border "solid"
                 }}
        ;;Navigation Menue
        [:aside.menu.p-2.border.bg-gray-100

         ;;CLASSES
         [classes-list class-id]

         ;; LAYOUT
         [seatingplans-list class-id active-class-seating-plan-id active-class-seating-plans]

         ;; CONSTRAINTS

         [constraints class-id class]
         [form/add-constraint class-id (:students class)]

         ;;STUDENT
         [students class-id class active-class-seating-plan-id]

         ]
        ;;EDITOR
        ;; [:p (str "hello " (empty? active-class-seating-plans))]
        (if (not (empty? active-class-seating-plans)) ;;this there are no seating plan false
          [:div ;;.card
           [editor/complete-editor [:seating-plans, active-class-seating-plan-id, :layout] seating-plan
            class-id active-class-seating-plan-id
            ]])
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

)]))

;;==============================
;;CLASS  =======================
;;==============================
(defn main []
  (let [
        class-id @(re-frame/subscribe [:class-id])
        class @(re-frame/subscribe [:get-class class-id])
        ]
    [:<>
     (if (empty? class)
     ;; [seating-plans class]
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4
        [:button.button {:on-click #(re-frame/dispatch [:toggle-add-class-form-status])} "Add a Class"]]
     [layout-classes class-id class]
     )

     [form/add-class]
     [form/add-room]
     ]))
