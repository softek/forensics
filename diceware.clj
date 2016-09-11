(ns diceware
  (:require [clojure.string :as string])
  (:import
    [System.IO
      StreamReader])
  (:gen-class))

(def rolls-per-word 5)

(defn lines->dice-map [lines]
  (->> lines
       (mapcat #(re-seq #"^([1-6]{5})\s+(\S+)" %))
       (mapcat (partial drop 1))
       (apply hash-map)))

(defn hexadecimal [bytes]
  (-> (BitConverter/ToString bytes)
      (.Replace "-" "")))

(defn base-dice
  "converts a number to a numeric base as described by a six-sided die (range 1 (inc 6)).
   The first roll is the least-significant digit.
   Returns a string."
  [number]
  (if (zero? number) "1"
    (loop [n number
           digits []]
      (if (= 0 n)
        (apply str digits)
        (let [zero-based (mod n 6)
              one-based (inc zero-based)
              remaining (/ (- n zero-based) 6)]
          (recur remaining (conj digits one-based)))))))

(defn bytes->dice [bytes]
  (->> bytes
       hexadecimal
       (str "0x")
       read-string
       base-dice))

(defn title-case [word]
  (let [[w ord](split-at 1 word)
        W (string/upper-case (apply str w))]
    (apply str W ord)))

(defn bytes->words-string [dice-map bytes]
  (->> bytes
       bytes->dice
         ;; fill in incomplete 5-roll words with 1s at the end
         ;(partition rolls-per-word rolls-per-word "1")
       (partition rolls-per-word)
       (filter #(= rolls-per-word (count %)))
       (map #(dice-map (apply str %)))
       (map title-case)
       (apply str)))

(defn read-dice-map
  ([] (read-dice-map "diceware.wordlist.asc"))
  ([file-name]
   (-> (StreamReader. file-name)
       line-seq
       lines->dice-map))) 

(def dice-map (delay (read-dice-map)))

(defn -main [& args]
  (let [words (if (empty? args) ["11111" "22222" "33333"] args)]
    (->> words
         (mapcat (fn [dice]
                   (@dice-map dice (str "not found in dice-map: " dice))))
         (apply str)
         println)))
