(ns seatingplanner.views.home
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [seatingplanner.db :as db]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.helpers :as h]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]))

(defn main []
  [:<>
   [:div.container
    [:div.columns.is-centered.is-vcentered
     [:div.column
      [:div.box.has-text-centered
       [:h1.title
        "Seating Planner"]
       [:p.subtitle
        "Steps"]
       [:div.is-centered.grid
        [:a.button.is-medium
         {:href (rtfe/href :routes/#rooms)}
         "1. Create Rooms"]
        [:a.button.is-medium
         {:href (rtfe/href :routes/#classes)}
         "2. Create Classes"]
        [:a.button.is-medium
         {:href (rtfe/href :routes/#class)}
         "3. Create Seating Plans"]
        ]

       ]]]]

;; [:div
;;  [:iframe {:width "560"
;;            :height "560"
;;            :src "https://www.youtube.com/embed/o2M88s9SFw4?si=3G_Sw7ZzKZhHTM-Y"
;;            :frameborder "0"
;;            :allowfullscreen true
;;            :class ""}]]
]
;; https://github.com/micmarsh/re-frame-youtube-fx
  )
;; ROUTING
(def toolbar-items
  [
   ["Home"    :routes/#frontpage]
   ["Rooms"    :routes/#rooms]
   ;; ["Room"    :routes/#room]
   ["Classes" :routes/#classes]
   ["Seating Plans" :routes/#class]
   ])

(defn route-info [route]
  [:div.m-4
   [:p "Routeinfo"]
   [:pre.border-solid.border-2.rounded
    (with-out-str (pp/pprint route))]])

(defn show-panel [route]
  (when-let [route-data (:data route)]
    (let [view (:view route-data)
         ;; app-db @(re-frame/subscribe [:app-db])
          ]
      [:<>
       [view]
       ;; [:div (str app-db)]
       ;; [route-info route]
       ])))

(defn main-panel []
  (let [active-route (re-frame/subscribe [:routes/current-route])]
    [:<>
     [:nav.bg-gray-200.p-4
      [:div.container.mx-auto.flex.justify-between.items-center
       [:div.text-black.font-bold.text-lg "Seating Planner"]
       [vt/navigation toolbar-items]]]


     [show-panel @active-route]]))
