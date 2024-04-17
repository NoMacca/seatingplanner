(ns seatingplanner.views.rooms
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [fontawesome.icons :as icons]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [seatingplanner.views.forms :as form]
   [fork.bulma :as bulma]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]))

(defn room-layout [room-id {:keys [name]}]
  [:tr
   [:td
    [:a.col-span-full
     {
      :title "Edit the layout of this room"
      :on-click #(re-frame/dispatch [:room-id room-id])
      :href (rtfe/href :routes/#room)}
     [:button.text-blue-500.underline.hover:text-blue-700
      name]]]
   [:td.flex.justify-end.gap-2
    [:button
     {
      :title "Create a copy of this room layout"
      :on-click #(re-frame/dispatch [:toggle-on-copy-room-form-status room-id])}
          (icons/render (icons/icon :fontawesome.solid/copy) {:size 20})
     ]

    [:button.delete
     {
      :title "Delete this room"
      :on-click #(re-frame/dispatch [:delete-room room-id])}
     ]
    ]
   ])

(defn rooms []
  (let [rooms @(re-frame/subscribe [:rooms])]
    (if (seq rooms)
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4

       [:table.table ;;.is-bordered
        [:thead
         [:tr
          [:td [:p.font-bold"Rooms"]]
          ;; [:td ""]
          [:td ""]
          ]]
        [:tbody
         (for [[room-id room] rooms]
           ^{:key room-id} [room-layout room-id room])]
        ]
       [:div.grid.grid-cols-3
        [:button.button {
                                    :on-click #(re-frame/dispatch [:toggle-add-room-form-status])}

          (icons/render (icons/icon :fontawesome.solid/plus) {:size 20})
         ;; "Add"
         ]]
       ]
      [:div.grid.rounded-xl.shadow-lg.items-center.p-6.m-4
       [:button.button {
                                   :on-click #(re-frame/dispatch [:toggle-add-room-form-status])} "Add a Room"]]

      )))

(defn main []
  [:<>
   ;; [:div [:h1.text-xl.text-center "Rooms"]]
   [rooms]
   [form/add-room]
   [form/copy-room]
   ]


  )
