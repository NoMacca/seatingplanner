(ns seatingplanner.helpers
(:require
   [tick.core :as t]
   [clojure.string :as str]
   [tick.alpha.interval :as t.i]
   [goog.string :as gstring]
   ))

(defn sdb [path]
  (fn [db [_ v]]
    (assoc-in db path v)))

(defn gdb
  [path]
  (fn [db _] (get-in db path)))

;;===============================================
;; STRING CONVERSIONS
;;===============================================

(defn clean-values [value]
  (->> (str/split value #"[,\n]")
       (map str/trim)
       (vec)
       (distinct)
       ))




(defn remove-item [item items]
  (vec (remove #(= item %) items)))
;;==============================================
;; ID MANAGEMENT HELPER FUNCTIONS ==============
;;==============================================
(defn create-item [items id data]
  (assoc items id data))

(defn read-item [items id]
  (get items id))

(defn update-item [items id new-data]
  (assoc items id new-data))

(defn delete-item [items id]
  (dissoc items id))

(defn allocate-next-id
  "Returns the next id for the next item.
  Assumes items are sorted.
  Returns one more than the current largest id."
  [items]
  ((fnil inc 0) (last (keys items))))

;; (def items (sorted-map))
;; (def items (create-item items 1 "Data for item 1"))
;; (def items (create-item items 2 "Data for item 2"))
;; (def items (update-item items 2 "Updated data for item 2"))

(sorted-map
2 "Hellosdf"
 )



(def foolasses (sorted-map
                1 {
                   :name "Year 7 Digital Technology"
                   :students ["Sally" "Noah" "John" "James"]
                   :constraints ["??"]
                   :seating-plans (sorted-map
                                   1
                                   {:name "Hellow"
                                    :layout [[:person :nil "Sally"]
                                             [:nil :desk "Noah"]
                                             ["John" "James" :nil]]}
                                   )}




                2 {
                   :name "Year 10 Digital Technology"
                   :students ["Mally" "Jill" "Eleanor" "Alan"]
                   :constraints ["??"]
                   :seating-plans (sorted-map
                                   1
                                   {:name "Hellow"
                                    :layout [[:person :nil "Sally"]
                                             [:nil :desk "Noah"]
                                             ["John" "James" :nil]]}
                                   )}


                )
  )

(for [[class-id class] foolasses
      ;; [seating-id seating-plan] (:seating-plans class)

      ]
  ;; (println "Class ID:" class-id)
  (println "Name:" (str class))
  ;; (println "Students:" (:students class))
  ;; (println "Constraints:" (:constraints class))
  ;; (println "Seating Plan ID:" seating-id)
  ;; (println "Seating Plan Name:" (:name seating-plan))
  ;; (println "Seating Plan Layout:" (:layout seating-plan)

  ;;          )

  )


;;==============================================
;; CONSTRAINT SATISFACTION PROBLEM =============
;;==============================================
(defn distance [seat1 seat2]
  (let [[x1 y1] seat1
        [x2 y2] seat2
        dx (- x2 x1)
        dy (- y2 y1)]
    (Math/sqrt (+ (* dx dx) (* dy dy)))))

(def counter (atom 0))

;; (empty? #{})

(defn init [domains variables constraints]
  {:domains domains
   :variables variables
   :any-constraints-violated? (apply some-fn constraints)
   :assignment (zipmap variables (repeat nil))})

(defn complete? [csp]
  (every? some? (vals (:assignment csp))))

(defn consistent? [{:keys [any-constraints-violated?]
                    :as csp}]
  (swap! counter inc)
  (not (any-constraints-violated? csp)))

(defn select-unassigned-variable [csp]
  (first (filter (comp nil?
                       (partial get-in csp)
                       (partial conj [:assignment]))
                 (:variables csp))))

(defn next-csps [csp]
  (map (fn [d]
         (-> csp
             (assoc-in [:assignment (select-unassigned-variable csp)] d)
             (assoc :domains (disj (:domains csp) d ))))
       (:domains csp)))


(defn backtracking-seq [csps]
  (lazy-seq (if-let [[$first & $rest] (seq csps)]
              (if (consistent? $first)
                (cons $first
                      (backtracking-seq (concat (next-csps $first)
                                                $rest)))
                (backtracking-seq $rest)))))

(defn backtracking [csp]
  (if-let [result (first (filter complete?
                                 (backtracking-seq (next-csps csp))))]
    result
    :failure))


(defmulti constraint (fn [type & args] type))

(defmethod constraint :non-adjacent [type & args]
  (let [[student1 student2 d] args]
    (fn [csp]
      (if-let [s1 (get-in csp [:assignment student1])]
        (<= (distance s1 (get-in csp [:assignment student2])) d) ;;If this is true, then it is bad!
        false
        ))))

(defmethod constraint :proximity [type & args]
  (let [[student1 student2 d] args]
    (fn [csp]
      (if-let [s1 (get-in csp [:assignment student1])]
        (> (distance s1 (get-in csp [:assignment student2])) d) ;;If this is true, then it is bad!
        false
        ))))

;;==============================================
;; ROOM CONVERSIONS ============================
;;==============================================
(defn convert-room-to-seats [room]
  (let [rows (count room)
        cols (count (first room))]
    (set
     (for [row (range rows)
           col (range cols)
           :when (and (not= (get-in room [row col]) nil)
                      (not= (get-in room [row col]) :desk))]
       [row col]
       ))))


(defn update-room [room row col new-value]
  (assoc-in room [row col] new-value))

(defn update-room-with-allocation [room allocations]
  (loop [a allocations
         r room]
    (if (empty? a)
      r
      (let [allocation (first a)
            row (first (second allocation))
            coll (second (second allocation))
            new-value (first allocation)]
          (recur (rest a)
                 (update-room r row coll new-value)
                               )))))

;; TODO maximum distance. teacher proximal. seating preference
;;==============================================
;; GENERATE SEATING PLAN =======================
;;==============================================



(defn generate-seating-plan [room students constraints]
  (reset! counter 0)
  (let [
        cleared-room (mapv (fn [row] (mapv (fn [item] (if (string? item) :student item)) row)) room)
        students (cljs.core.shuffle students)         ;;variables (students)
        csp (init (convert-room-to-seats cleared-room) ;;domains (seats)
                  students ;;variables (students)
                  (set (map (fn [[t s1 s2 d]] (constraint t s1 s2 d)) constraints))
                  ;; constraints
                  )
        result (backtracking csp)]
    (update-room-with-allocation cleared-room (:assignment result)))
  )


(def room [
  [:student nil nil]
  [nil      nil :student]
  [:student nil nil]
  [nil      nil nil]
  [:student nil :student]
  [nil      nil :student]
  [nil      nil nil]])

(def constraints [[:non-adjacent "James" "John" 2] [:proximity "Jill" "Sally" 1]])

;;Original
(def students ["Sally" "Jill" "James" "Jack" "John"])
;;Does not work
;; (def students ["John" "Jill" "Sally" "James" "Jack"])
;; (def students ["John" "James" "Jill" "Jack" "Sally"])
;; (def students ["Jill" "James" "John" "Sally" "Jack"])
;; (def students ["Jill" "James" "John" "Jack" "Sally"])
;; (def students ["James" "Jill" "John" "Jack" "Sally"])
;; (def students ["James" "Jill" "Sally" "John" "Jack"])
;; (def students ["James" "Jill" "John" "Sally" "Jack"])
;; (def students ["James" "John" "Jack" "Sally" "Jill"])
;; (def students ["Jack" "Jill" "John" "James" "Sally"])
;; (def students ["Jack" "Jill" "John" "James" "Sally"])
;; (def students ["Jack" "James" "Jill" "Sally" "John"])
;; (def students ["Jack" "James" "Jill" "John" "Sally"])
;; (def students ["Jack" "James" "John" "Jill" "Sally"])
;; (def students ["Jack" "John" "Jill" "Sally" "James"])

(generate-seating-plan room students constraints)

;; (def example-seats #{[0 1] [1 0] [1 1] [2 2] [3 2]})
;; (def example-students ["Sally" "Jill" "James" "Jack" "John"])

;; (def example-constraints
;;   [[:non-adjacent "James" "John" 1]
;;    [:proximity "Jill" "Sally" 1]
;;   ])

;; (generate-seating-plan example-room example-students example-constraints)
;; ;; => {"James" [2 2], "Sally" [1 0], "Jack" [3 2], "John" [0 1], "Jill" [1 1]}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; => [96
;;     {:domains #{},
;;      :variables ["Sally" "Jill" "James" "Jack" "John"],
;;      :any-constraints-violated? #object [cljs$core$sp2],
;;      :assignment
;;      {"Sally" [1 0], "Jill" [1 1], "James" [2 2], "Jack" [3 2], "John" [0 1]}}]

;; => [78
;;     {:domains #{},
;;      :variables ["Sally" "Jill" "James" "Jack" "John"],
;;      :any-constraints-violated? #object [cljs$core$sp1],
;;      :assignment
;;      {"Sally" [2 2], "Jill" [3 2], "James" [1 0], "Jack" [1 1], "John" [0 1]}}]

;; => [9
;;     {:domains #{},
;;      :variables ["Sally" "Jill" "James" "Jack" "John"],
;;      :any-constraints-violated? #object [cljs$core$sp2],
;;      :assignment
;;      {"Sally" [2 2], "Jill" [1 0], "James" [3 2], "Jack" [1 1], "John" [0 1]}}]

;; (def room-data
;;   {:room [[:person :desk] [nil :person]]             ;; Room representation
;;    :domains #{[0 1] [1 0] [1 1] [2 2] [3 2]}        ;; Domains (seats)
;;    :variables ["Sally" "Jill" "James" "Jack" "John"] ;; Variables (students)
;;    :constraints #{
;;                   ;; (constraint :non-adjacent "James" "Jack" 1)
;;                   (constraint :non-adjacent "James" "John" 1)
;;                   (constraint :proximity "Jill" "Sally" 1)
;;                 }
;;    :assignment {"Sally" [2 2]
;;                 "Jill" [1 0]
;;                 "James" [3 2]
;;                 "Jack" [1 1]
;;                 "John" [0 1]}}) ;; Initial assignment



;;=====================================
;;Playing Around
;;====================================


;; (def constraints #{(constraint :WA :NT)
;;                    (constraint :WA :SA)
;;                    (constraint :NT :SA)
;;                    (constraint :NT :Q)
;;                    (constraint :SA :Q)
;;                    (constraint :SA :NSW)
;;                    (constraint :SA :V)
;;                    (constraint :Q :NSW)
;;                    (constraint :V :T)})
;; ;; => Execution error (Error) at (<cljs repl>:1).
;; ;;    No method in multimethod 'seatingplanner.views.csp/constraint' for dispatch value: :V
;; ;;    :repl/exception!(apply some-fn constraints)


;; (def variables [:WA :NT :Q :NSW :V :SA :T])
;; (zipmap variables (repeat nil))



;; (def csp (init #{:red :green :blue}
;;             [:WA :NT :Q :NSW :V :SA :T]
;;             #{(constraint :WA :NT)
;;               (constraint :WA :SA)
;;               (constraint :NT :SA)
;;               (constraint :NT :Q)
;;               (constraint :SA :Q)
;;               (constraint :SA :NSW)
;;               (constraint :SA :V)
;;               (constraint :Q :NSW)
;;               (constraint :V :T)})
;; )




;; (def domains #{:red :green :blue})
;; (map (fn [d]
;;        (assoc-in csp
;;                  [:assignment (select-unassigned-variable csp)]
;;                  d))
;;        (:domains csp))
;; ;; => ({:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :green, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}}
;; ;;     {:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :red, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}}
;; ;;     {:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :blue, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}})

;;  (filter (comp nil?
;;                        (partial get-in csp)
;;                        (partial conj [:assignment]))
;;                  (:variables csp))

;; (defn add-five [x] (+ x 5))
;; ;; (defn double [x] (* x 2))
;; (def add-five-and-double (comp double add-five))
;; (add-five-and-double 3)


;; (def numbers (range 1 11)) ; A sequence of numbers from 1 to 10
;; (filter (comp even?
;;               #(mod % 3))
;;           numbers)

;; (def v (:variables csp))
;; (conj [:assignment] (first v))
;; ;; => [:assignment :WA]
;; (get-in csp [:assignment :WA])
;; (nil? nil)

;; (partial get-in csp)
;; (partial conj [:assignments])


;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;Next-CSPS

;; (map (fn [d]
;;        (assoc-in csp
;;                  [:assignment (select-unassigned-variable csp)]
;;                  d))
;;      (:domains csp))
;; ;; => ({:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :green, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}}
;; ;;     {:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :red, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}}
;; ;;     {:domains #{:green :red :blue},
;; ;;      :variables [:WA :NT :Q :NSW :V :SA :T],
;; ;;      :any-constraints-violated? #object [cljs$core$spn],
;; ;;      :assignment {:WA :blue, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil}})



;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; ;; (every? (some? (vals
;; ;;           {:WA :blue, :NT nil, :Q nil, :NSW nil, :V nil, :SA nil, :T nil})))

;; ;; (def numbers [1 2 3 5 5])
;; (some? numbers)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (def csp-first (first (map (fn [d]
;;                (assoc-in csp
;;                          [:assignment (select-unassigned-variable csp)]
;;                          d))
;;              (:domains csp))))

;; (let [{:keys [any-constraints-violated?]
;;                     :as csp-e} csp-first ]

;;   (any-constraints-violated? csp-e))


;; ;; (defn consistent? [{:keys [any-constraints-violated?]
;; ;;                     :as csp}]
;; ;;   (swap! counter inc)
;; ;;   (not (any-constraints-violated? csp)))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; (defn even?
;; ;;   [n] (if (integer? n)
;; ;;         (zero? (bit-and n 1))
;; ;;         (throw (js/Error. (str "Argument must be an integer: " n)))))

;; (defn even-n? [n] false)
;; (defn divisible-by-three? [n] (zero? (mod n 3)))
;; (even? 12)
;; (def any-matching?
;;   (apply some-fn #{even-n? divisible-by-three?}))

;; (any-matching? 6)

;; (any-matching? 6) ;=> true (because 6 is even)
;; (any-matching? 9) ;=> true (because 9 is divisible by 3)
;; (any-matching? 5) ;=> false (because 5 is neither even nor divisible by 3)

;; ;;============================================================
;; ;; https://nextjournal.com/lomin/constraint-satisfaction-problems-and-functional-backtracking-search

;; (def counter (atom 0))

;; (defn init [domains variables constraints]
;;   {:domains domains
;;    :variables variables
;;    :any-constraints-violated? (apply some-fn constraints)
;;    :assignment (zipmap variables (repeat nil))})

;; (defn complete? [csp]
;;   (every? some? (vals (:assignment csp))))

;; (defn consistent? [{:keys [any-constraints-violated?]
;;                     :as csp}]
;;   (swap! counter inc)
;;   (not (any-constraints-violated? csp)))

;; (defn select-unassigned-variable [csp]
;;   (first (filter (comp nil?
;;                        (partial get-in csp)
;;                        (partial conj [:assignment]))
;;                  (:variables csp))))

;; (defn next-csps [csp]
;;   (map (fn [d]
;;          (assoc-in csp
;;                    [:assignment (select-unassigned-variable csp)]
;;                    d))
;;        (:domains csp)))

;; (defn backtracking-seq [csps]
;;   (lazy-seq (if-let [[$first & $rest] (seq csps)]
;;               (if (consistent? $first)
;;                 (cons $first
;;                       (backtracking-seq (concat (next-csps $first)
;;                                                 $rest)))
;;                 (backtracking-seq $rest)))))

;; (defn backtracking [csp]
;;   (if-let [result (first (filter complete?
;;                                  (backtracking-seq (next-csps csp))))]
;;     result
;;     :failure))


;; ;;This is the function that need to be changed.
;; (defn constraint [state-a state-b]
;;   (fn [csp]
;;     (if-let [a (get-in csp [:assignment state-a])]
;;       (= a (get-in csp [:assignment state-b]))
;;       false)))



;; (reset! counter 0)

;; (let [csp (init #{:red :green :blue}
;;                 [:WA :NT :Q :NSW :V :SA :T]
;;                 #{(constraint :WA :NT)
;;                   (constraint :WA :SA)
;;                   (constraint :NT :SA)
;;                   (constraint :NT :Q)
;;                   (constraint :SA :Q)
;;                   (constraint :SA :NSW)
;;                   (constraint :SA :V)
;;                   (constraint :Q :NSW)
;;                   (constraint :V :T)})
;;       result (backtracking csp)]
;;   [@counter result])
