(ns seatingplanner.views.room
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [fork.bulma :as bulma]
   [fork.re-frame :as fork]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]
   [seatingplanner.views.editor :as editor]
   ))

(defn layout-rooms []
  (let [
        room-id @(re-frame/subscribe [:room-id])
        room @(re-frame/subscribe [:room room-id])
        ]
    [:div.card.p-4
     [editor/editor [:rooms, room-id, :layout] room]
     ]
    )
  )

(defn main []
  [:<>
   ;; [:div [:h1.text-xl.text-center "ROOMS"]]
   [layout-rooms]
   ])

