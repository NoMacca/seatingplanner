(ns seatingplanner.views.rooms
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [seatingplanner.views.forms :as form]
   [fork.bulma :as bulma]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]))

;;TODO change here
(defn room-layout [room-id {:keys [name]}]
  [:tr
   [:td
    [:a.col-span-full
     {
      :on-click #(re-frame/dispatch [:room-id room-id])
      :href (rtfe/href :routes/#room)}
     [:button.text-blue-500.underline.hover:text-blue-700
      name]]]
   [:td [:button.delete
         {:on-click #(re-frame/dispatch [:delete-room room-id])}
         ]]
   ])

(defn rooms []
  (let [rooms @(re-frame/subscribe [:rooms])]
    (if (seq rooms)
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4

       [:table.table ;;.is-bordered
        [:thead
         [:tr
          [:td "Rooms"]
          ;; [:td "Numer of Students"]
          [:td ""]
          ]]
        [:tbody
         (for [[room-id room] rooms]
           ^{:key room-id} [room-layout room-id room])]
        ]
       [:div.grid.grid-cols-3
        [:button.button.is-primary {
                                    :on-click #(re-frame/dispatch [:toggle-add-room-form-status])} "Add"]]
       ]
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4
       [:button.button.is-primary {
                                   :on-click #(re-frame/dispatch [:toggle-add-room-form-status])} "Add a Room"]]

      )))

(defn main []
  [:<>
   [:div [:h1.text-xl.text-center "Rooms"]]
   [rooms]
   [form/add-room]]


  )
