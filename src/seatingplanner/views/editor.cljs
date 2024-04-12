(ns seatingplanner.views.editor
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [fork.bulma :as bulma]
   [fontawesome.icons :as icons]
   [fork.re-frame :as fork]
   [seatingplanner.stylesgarden :as gstyle]
   [seatingplanner.toolsview :as vt]

   ))

(defn add-rows [path]
  [:<>
   [:button.button.is-small {:on-click #(re-frame/dispatch [:add-row path])}
          (icons/render (icons/icon :fontawesome.solid/plus) {:size 20})
    ]
   [:button.button.is-small {:on-click #(re-frame/dispatch [:remove-row path])}
          (icons/render (icons/icon :fontawesome.solid/minus) {:size 20})
    ]])

(defn add-columns [path]
  [:div.grid
   [:button.button.is-small {:on-click #(re-frame/dispatch [:add-column path])}
          (icons/render (icons/icon :fontawesome.solid/plus) {:size 20})
    ]
   [:button.button.is-small {:on-click #(re-frame/dispatch [:remove-column path])}
          (icons/render (icons/icon :fontawesome.solid/minus) {:size 20})
    ]
   ])


(def toggle-buttons "px-3 py-2 border border-gray-300 rounded cursor-pointer")
(def inactive " bg-gray-200 text-gray-700" )
(def active " bg-gray-300 text-black")

(defn toggle-spot [path]
  (let [a @(re-frame/subscribe [:toggle-spot])]
    [:<>
     [:div.flex.justify-end ;;.bg-gray-200 ;;.space-x-4
      [:button {:class (str toggle-buttons (if (= :student a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot :student])}

          (icons/render (icons/icon :fontawesome.solid/person) {:size 20})]

      [:button {:class (str toggle-buttons (if (= :desk a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot :desk])}
          (icons/render (icons/icon :fontawesome.solid/square) {:size 20})
       ]
      [:button {:class (str toggle-buttons (if (= nil a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot nil])}

          (icons/render (icons/icon :fontawesome.regular/square) {:size 20})
       ]
      [:button {:class (str toggle-buttons " bg-red-100") :on-click #(re-frame/dispatch [:clear-all path])}
          (icons/render (icons/icon :fontawesome.solid/trash) {:size 20})
       ]
      ]]))


(def item-class
  "border border grid place-items-center"
  )
(defn cell [path row column spot-value]
  (let [background-color (cond
                           (= spot-value :desk) "bg-gray-800"
                           (= spot-value :student) "bg-yellow-100" ;;Improve the look here
                           (string? spot-value) "bg-yellow-100"
                           :else "bg-gray-100")]

    [:div {:class (str item-class " " background-color)
           :draggable (if (or (string? spot-value ) (= spot-value :student)) "true" "false")
           :on-click #(re-frame/dispatch [:change-cell path row column])
           } (if (string? spot-value) spot-value)]
     ))

(defn layout-display [path layout]
  (let [num-columns (count (first layout))
        num-rows (count layout)]
    [:<>
     [:div
      {:style {:display "grid"
               :grid-template-columns (str "repeat("num-columns", minmax(40px, 1fr)")
               :grid-template-rows (str "repeat("num-rows", minmax(40px, 1fr)")
               ;; :border "solid"
               }}
      (for [row (range num-rows)
            column (range num-columns)]
        (let [spot-value (get-in layout [row column])]
          ^{:key [row column]} [cell path row column spot-value]
          ))]]))

;; This is the main rendering function
;;
(defn editor [path named-layout]
  (let [
        ;; name (:name named-layout)
        layout (:layout named-layout)]
    [:<>
       [:div.content {:style {:display "grid" :grid-template-columns (str "1fr 1fr 40px")}}
        [:div.col-start-1.col-span-2 [layout-display path layout]]
        [:div.col-start-3[add-columns path]]
        [:div.col-start-1 [add-rows path]]
        [:div.col-start-2 [toggle-spot path]]
        ]]
     ))


(defn complete-editor [path named-layout class-id active-class-seating-plan-id
                       ]
  (let [
        name (:name named-layout)
        layout (:layout named-layout)]
     [:div.card
      [:div.card-header

       [:div.card-header-title name]

       [:div.card-header-icon
        [:div.px-2
         [:button.button.is-white
          {:on-click #(re-frame/dispatch [:full-screen-toggle])}
          (icons/render (icons/icon :fontawesome.solid/maximize) {:size 20})]
         ]]]
      [:div.card-content

       [editor path named-layout]
]
      [:div.card-footer
       ;; [:div.card-footer-item [:p.font-bold "Allocate"]]
         [:div.card-footer-item [:button.button.w-full.bg-red-500.py-1
                     {:on-click #(re-frame/dispatch [:organise class-id active-class-seating-plan-id])}
                     "Allocate"]]
]]
  ))




;;GENERIC

(defn layout-dropdown [class-id id seating-plans]
  (let [current-seating-plan (get seating-plans id)
        seating-plans-rest (dissoc seating-plans id)]

    ;; (js/console.log id)
    [:div.field
     ;; {:class class}
     [:label.label "Layouts"]
     [:div.control
      [:div.select
       [:select.w-full
        {
         :name "Dropdown"
         ;; :value "hello mate"
         :on-change #(re-frame/dispatch [:change-layout class-id (-> % .-target .-value)])
         ;; (vec (concat [{0 "select layout"}] (map (fn [[num {:keys [name]}]] {num name}) named-layouts)))
         }

        (if (= id nil)
          [:option {:value "empty"} "Select or create layout"]
          [:option {:value id} (:name current-seating-plan)]
          )
        (for [sp seating-plans-rest]
          ^{:key (first sp)}
          [:option
           {:value (first sp)} (:name (second sp))])]
       ]]


     [:div.py-1 [:button.button.bg-red-100.w-full
                 {:on-click #(re-frame/dispatch [:delete-layout class-id id])}
                 "Delete"]]

     ])


  )

;;========================================================
;;DRAGGING
;;========================================================

;; [:div {:class item-class
;;        :draggable "true"
;;        :on-drag-start (fn [e] (js/console.log "on-drag-start: " e))
;;        :on-drag-over (fn [e] (js/console.log "on-drag-over: "))
;;        :on-drag-enter (fn [e] (js/console.log "on-drag-enter: " e))
;;        :on-drop (fn [e] (js/console.log "on-drop: " e))
;;        :on-drag-end (fn [e] (js/console.log "on-drag-end: " e))


;;        ;; :on-drag (fn [e] (js/console.log "on-drag: "))
;;        :on-drag-leave (fn [e] (js/console.log "on-drag-leave: " e))

;;        } "1"]



;; [:div
;;  [:p.bg-blue-500 {:class "draggable"
;;                   :draggable "true"
;;                   ;; :onDragStart #(js/console.log "DragStart")
;;                   ;; :onDragOver #(js/console.log "DragOver")
;;                   :on-drag-over
;;                   ;; (fn [event] (js/console.log "DragOver event occurred. Mouse coordinates: " (.-clientX event)))
;;                   (fn [event] (set! (.. event -target -style -backgroundColor) "red"))
;;                   ;; :onDragLeave #(js/console.log "DragLeave")
;;                   :on-drag-leave
;;                   (fn [event] (set! (.. event -target -style -backgroundColor) "green"))

;;                   } "foo"]
;;  ]
