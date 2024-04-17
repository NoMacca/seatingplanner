(ns seatingplanner.views.editor
  (:require
   [cljs.pprint :as pp]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reitit.frontend.easy :as rtfe]
   [seatingplanner.db :as db]
   [seatingplanner.helpers :as h]
   [fork.bulma :as bulma]
   [seatingplanner.views.forms :as form]
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
      [:button {
                :title "Student space"
                :class (str toggle-buttons (if (= :student a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot :student])}

          (icons/render (icons/icon :fontawesome.solid/person) {:size 20})]

      [:button {
                :title "Desk space"
                :class (str toggle-buttons (if (= :desk a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot :desk])}
          (icons/render (icons/icon :fontawesome.solid/square) {:size 20})
       ]
      [:button {
                :title "Empty space"
                :class (str toggle-buttons (if (= nil a) active inactive))
                :on-click #(re-frame/dispatch [:toggle-spot nil])}

          (icons/render (icons/icon :fontawesome.regular/square) {:size 20})
       ]
      ;; [:button {
      ;;           :title "Undo"
      ;;           :class (str toggle-buttons " bg-blue-200")
      ;;           :on-click #(js/alert "add undo functionality")
      ;;           }
      ;;     (icons/render (icons/icon :fontawesome.solid/rotate-left) {:size 20})
      ;;  ]
      [:button {
                :title "Clear seating plan"
                :class (str toggle-buttons " bg-red-100") :on-click #(re-frame/dispatch [:clear-all path])}
          (icons/render (icons/icon :fontawesome.solid/trash) {:size 20})
       ]




      ]]))


(def item-class
  "border border grid place-items-center"
  )
(defn cell [path row column spot-value dragging-id valid-drop-id]
      (let [
            background-color (cond
                               (= spot-value :desk) "bg-gray-800"
                               (= spot-value :student) "bg-yellow-100" ;;Improve the look here
                               (string? spot-value) "bg-yellow-100"
                               :else "bg-gray-100")]

        [:div

         ;; (js/console.log (str "row " row " column " column "\nbackground-color " background-color))

         (if (or (string? spot-value ) (= spot-value :student))
           {
            :class (str item-class " " background-color)
            :on-click #(re-frame/dispatch [:change-cell path row column])

            :draggable  "true"

            ;; TODO ON DRAG START
            :on-drag-start (fn [event]
              ;; (set! (.. event -target -style -backgroundColor) "blue")
               ;; (js/console.log (str row ":" column))
               (reset! dragging-id [row, column])

              )
               ;; (reset! dragging-over-id spot-value)

            ;; (fn [e] (js/console.log "hello"))
            ;; #(js/console.log (str "START DRAGGED ELEMENT row " row " col " column " spot value " spot-value))
            ;; (fn [e] (js/console.log " on-drag-start: " e ))
            ;; :onDragStart #(js/console.log "DragStart")


            ;;TODO ONE-DRAG-OVER
            :on-drag-over (fn [event] (do
                                          (.preventDefault event)
                                          ;; (set! (.. event -target -style -backgroundColor) "red")
                                          )
                            )
            ;;#(preventDefault %)
            ;; (fn [event] (set! (.. event -target -style -backgroundColor) "red"))
            ;; (fn [event] (js/console.log "DragOver event occurred. Mouse coordinates: " (.-clientX event)))
            ;; :on-drag-over (fn [e] (js/console.log "on-drag-over: "))
            ;; ;; :onDragOver #(js/console.log "DragOver")


            ;;TODO ON DRAG ENTER
            :on-drag-enter #(do
                              (reset! valid-drop-id [row column])
                              (.preventDefault %)
                              )
            ;; #(js/console.log (str "ENTER - OVER ELEMENT row " row " col " column " spot value " spot-value))
            ;; (fn [e] (js/console.log "on-drag-enter: " e))


            ;; TODO ON DRAG LEAVE
            ;; :on-drag-leave (fn [event] (do
            ;;               ;; (reset! dragging-over-id nil)
            ;;               (set! (.. event -target -style -backgroundColor) "#fef9c3")))

            ;; #(reset! dragging-over-id nil)
            ;; :on-drag-leave (fn [event] (set! (.. event -target -style -backgroundColor) "#fef9c3"))
            ;; (fn [e] (js/console.log "on-drag-leave: " e))
            ;; #(js/console.log (str "LEAVE - OVER ELEMENT row " row " col " column " spot value" spot-value))

            ;;ON DRAG
            ;; ;; :on-drag (fn [e] (js/console.log "on-drag: "))


            ;; TODO ON DROP FROM CELL THAT IS BEING DROPPED ON
            :on-drop (fn [event] (do
                                   (re-frame/dispatch [:swap-cells path @dragging-id @valid-drop-id])
                                   ;; (set! (.. event -target -style -backgroundColor) "#fef9c3")
                                   ;; (js/console.log (str "ON-DROP (drop zone) coming from " spot-value " dragging-id: " @dragging-id ", dragging-over-id: " @valid-drop-id))
                                   ))


            ;; (fn [e] (js/console.log "ON DROP on-drop: " e))
            ;; :on-drop (fn [e] (js/console.log "ON DROP on-drop: " e))

            ;; TODO ON DRAG END
            :on-drag-end
            (fn [event] (do
               (reset! dragging-id nil)
               (reset! valid-drop-id nil)
               (js/console.log (str "ON-DRAG-END (drop item) coming from " spot-value " on-drag-end dragging-id: " @dragging-id ", dragging-over-id: " @valid-drop-id))
              ))

            ;; #(do ;;includes case where the drop is impossible

            ;; (fn [e] (js/console.log "on-drag-end: " e))


            }
           {:class (str item-class " " background-color)
            :on-click #(re-frame/dispatch [:change-cell path row column])}
           )
         (if (string? spot-value) spot-value)
         ;; spot-value

         ]))


(defn layout-display [path layout]
  (let [num-columns (count (first layout))
        num-rows (count layout)

        valid-drop-id (reagent/atom nil)
        dragging-id (reagent/atom nil)
        ]
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
          ^{:key [row column]} [cell path row column spot-value dragging-id valid-drop-id]
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

       [:div.card-header-title (str "Seating Plan: "name)]

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
       [:button.card-footer-item.bg-green-500
        {
         :title "Allocate students onto the seating plan considering the constraints"
         :on-click #(re-frame/dispatch [:organise class-id active-class-seating-plan-id])}
        [:p
         ;; "Allocate"
         (icons/render (icons/icon :fontawesome.solid/chair) {:size 20})
         ]]

       [:button.card-footer-item
        {:title "Validate whether seating plan is correct"
         :on-click #(re-frame/dispatch [:validate class-id active-class-seating-plan-id])
         }
        (icons/render (icons/icon :fontawesome.solid/check) {:size 20})
        ]


       [:button.card-footer-item
        {:title "Make a copy of this seating plan"
         :on-click #(re-frame/dispatch [:toggle-copy-seating-plan-form-status])
         ;; #(re-frame/dispatch [:copy-seating-plan class-id active-class-seating-plan-id])

         }
        (icons/render (icons/icon :fontawesome.solid/copy) {:size 20})
        ]
       [form/copy-seating-plan class-id active-class-seating-plan-id]



       [:button.card-footer-item..bg-red-100.hover:bg-red-500
        {
         :title "Delete this seating plan"
         :on-click #(re-frame/dispatch [:delete-layout class-id active-class-seating-plan-id])}
        [:p
         ;; "Delete"
         (icons/render (icons/icon :fontawesome.solid/trash) {:size 20})
         ]

        ]]]))
