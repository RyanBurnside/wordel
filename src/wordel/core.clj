;;;; Implements that annoying Wordel game everyone is playing
;;;; Ryan Burnside 02/03/2022
;;;; Dedicated to the rebels in #guvnor-guys
;;;; See a physician immediatly if rash developes

(ns wordel.core
  (:require [clojure.string :as c-str]))

;;; Setup section
(def debug false) ; Spits out word if true
(def correct-place :GREEN)
(def inside-word :YELLOW)

;;; Color Voodoo Section
(def ansi-fg-colors
  "Foreground colors"
  {:BLACK   "\u001b[30m"
   :RED     "\u001b[31m"
   :GREEN   "\u001b[32m"
   :YELLOW  "\u001b[33m"
   :BLUE    "\u001b[34m"
   :MAGENTA "\u001b[35m"
   :CYAN    "\u001b[36m"
   :WHITE   "\u001b[37m"})


(def ansi-bg-colors
  "Background colors (doesn't force HIGH mode on fg colors)"
  {:BLACK    "\u001b[40m"
   :RED      "\u001b[41m"
   :GREEN    "\u001b[42m"
   :YELLOW   "\u001b[43m"
   :BLUE     "\u001b[44m"
   :MAGENTA  "\u001b[45m"
   :CYAN     "\u001b[46m"
   :WHITE    "\u001b[47m"})


(defn set-fg-color!
  "Set terminal foreground color to key color - mutates"
  [key-color]
  (print (key-color ansi-fg-colors))
  (flush))


(defn set-bg-color!
  "Set terminal background color to key color - mutates"
  [key-color]
  (print (key-color ansi-bg-colors))
  (flush))


(defn reset-color!
  "Defaults the terminal fg and bg colors - mutates"
  []
  (print "\u001b[0m")
  (flush))


;;; Accessory Functions
(defn get-new-word
  "Slurps from wordbank probably not safe if you go crazy..."
  [min-length max-length]
  (->> (slurp "./resources/scrabble")
       (c-str/split-lines ,,,)
       (filter #(<= min-length (count %) max-length) ,,,)
       (rand-nth ,,,)
       (c-str/upper-case ,,,)))


(defn process-answer
  "Prints a highlighted version of user answer, returns true if guessed"
  [word guess]

  ;; Process both words char by char, end on the shorter if one is longer
  (doseq [[w g] (map #(vector %1 %2) word guess)]
    (set-fg-color! :BLACK)

    ;; Determine color
    (cond
      (= w g) (set-bg-color! correct-place)
      (clojure.string/includes? word (str g)) (set-bg-color! inside-word)
      :else (reset-color!))

    ;; Print char with color
    (print g)
    (reset-color!))

  (println)
  (= word guess))


(defn ask-times
  "Asks a user n many times for a word"
  [n word]
  (loop [attempts-left n]
    (let [got-it (process-answer word (c-str/upper-case (read-line)))]
      (when (and (not got-it)
                 (> attempts-left 1))
        (recur (dec attempts-left))))))


(defn prompt-y-or-n
  "Prompts user to death, returns true of y false if n"
  [prompt]
  (loop []
    (println prompt)
    (flush)
    (let [answer (c-str/upper-case (read-line))]
      (if (or (= answer "Y")
              (= answer "N"))
        (= answer "Y")
        (recur)))))


(defn driver
  "The game's main loop"
  []
  (loop []
    (set-fg-color! :YELLOW)
    (println "Ryan Burnside Begrudgingly Presents ... (some copyrighted game)")
    (set-fg-color! :CYAN)
    (let [the-word    (get-new-word 4 5)
          word-length (count the-word)
          tries (inc word-length)]
      (when debug (println "DEBUG: " the-word))
      (println (str "The word has " (str word-length) " letters."))
      (println (str "You'll get " (str tries) " tries."))
      (reset-color!)
      (ask-times tries the-word))

    (when (prompt-y-or-n "Would you like to play again? y or n.")
      (recur))))


;; Program Entry Point
(defn -main
  [& args]
  (driver))
