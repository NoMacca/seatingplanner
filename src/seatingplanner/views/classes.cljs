(ns seatingplanner.views.classes
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [seatingplanner.views.forms :as form]
   [fork.bulma :as bulma]
   [fontawesome.icons :as icons]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]))

(defn class-layout [class-id {:keys [name students constraints]}]
  [:tr
   [:td
    [:a.col-span-full
     {
      :title "Create class seating plans"
      :on-click #(re-frame/dispatch [:class-id class-id])
      :href (rtfe/href :routes/#class)}
     [:button.text-blue-500.underline.hover:text-blue-700
         name]]]
   [:td (str (count students))]
   [:td [:button.delete
         {
          :title "Delete class"
          :on-click #(re-frame/dispatch [:delete-class class-id])}
         ]]
   ])

(defn classes []
  (let [classes @(re-frame/subscribe [:classes])]
    (if (seq classes)
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4
       [:table.table ;;.is-bordered
        [:thead
         [:tr
          [:td [:p.font-bold "Classes"]]
          [:td [:p.font-bold"Numer of Students"]]
          [:td ""]
          ]]
        [:tbody
         (for [[class-id class] classes]
           ^{:key class-id} [class-layout class-id class])]
        ]
       [:div.grid.grid-cols-3
        [:button.button {
                         :title "Add a class"
                         :on-click #(re-frame/dispatch [:toggle-add-class-form-status])}
         ;; "Add"
          (icons/render (icons/icon :fontawesome.solid/plus) {:size 20})
         ]]
       ]

      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4
        [:button.button {
                         :on-click #(re-frame/dispatch [:toggle-add-class-form-status])} "Add a Class"]]

      )))

(defn main []
  [:<>
   ;; [:div [:h1.text-xl.text-center "Classes"]]
   [classes]
   [form/add-class]
   ]
  )
