(ns regdiff
  (:import [Microsoft.Win32
            RegistryKey
            Registry
            RegistryHive
            RegistryView])
  (:require [clojure.string :as string])
  (:gen-class))

(defn stderr![& args]
  (binding [*out* *err*]
    (apply println args)))

;(def hklm-software (.OpenSubKey Registry/LocalMachine "Software"))

(def reg-key? (partial instance? RegistryKey))

(defn maybe-open-subkey [^RegistryKey k ^String name]
  (try
    (.OpenSubKey k name)
    (catch System.Security.SecurityException se
      (stderr! "Cannot open " (.-Name k) ":" name ".  " (.Name (.GetType se)))
      nil)))

(defn reg-subkeys [^RegistryKey k]
  (->> (.GetSubKeyNames k)
       (map (partial maybe-open-subkey k))
       (filter identity)))

(defn reg-values [^RegistryKey k]
  (map
    (fn [value-name]
      {:key (str (.-Name k) "::" value-name)
       :kind (.GetValueKind k value-name)
       :value (.GetValue k value-name)})
    (.GetValueNames k)))

;(reg-subkeys hklm-software)
;(reg-values hklm-software)

(defn reg-children [^RegistryKey k]
  (concat (reg-subkeys k)
          (reg-values k)))
;(reg-children hklm-software)

(defn reg-seq
  ([^RegistryKey k]
   (tree-seq reg-key? reg-children k)))

;(clojure.pprint/pprint
;  (take 100
;    (reg-seq hklm-software))))

(defn reg-key [regkey-or-valuemap]
  (cond (reg-key? regkey-or-valuemap) (.-Name ^RegistryKey regkey-or-valuemap)
        (associative? regkey-or-valuemap) (:key regkey-or-valuemap)
        :else regkey-or-valuemap))

(defn make-title-reporter [ct]
  (let [value (long-array 1)]
    (fn reporter [[path]]
      (let [v (nth value 0)]
        (when (zero? (mod v ct))
          (System.Console/set_Title (str v " comparisons. " path)))
        (aset value 0 (+ 1 v))))))

(defn reg-compare [s1 s2 filter? progress-reporter]
  (lazy-seq
    (let [[f1 & r1] s1
          [f2 & r2] s2]
      (let [k1 (reg-key f1)
            k2 (reg-key f2)
            key-comparison (compare k1 k2)
            [n1 n2 c]
            (cond (= key-comparison 0) [r1 r2 [k1 (if (= f1 f2) :same :different) f1 f2]]
                  (< key-comparison 0) [r1 s2 [k1 :left-only f1 nil]]
                  (> key-comparison 0) [s1 r2 [k2 :right-only nil f2]])]
        (when (or k1 k2)
          (progress-reporter c)
          (if (filter? c)
            (cons c (reg-compare n1 n2 filter? progress-reporter))
            (reg-compare n1 n2 filter? progress-reporter)))))))
#_
(->>
  (reg-compare (reg-seq hklm-software)
               (reg-seq hklm-software)
               (fn[[path difference]]
                 (and (reg-key? path)
                      (not= :same difference)))
               (make-title-reporter 10))
  (take 5))

(defn open-subkey [rkey subkeys]
  (reduce #(.OpenSubKey % %2) rkey subkeys))

(defn -main[& args]
  (let [[machine hive-name subkey-csv] args
        hive (enum-val RegistryHive hive-name)
        subkeys (remove empty? (string/split subkey-csv #","))
        remote-key (RegistryKey/OpenRemoteBaseKey hive machine)
        local-key (RegistryKey/OpenBaseKey hive RegistryView/Registry64)
        differences
        (reg-compare (reg-seq (open-subkey local-key subkeys))
                     (reg-seq (open-subkey remote-key subkeys))
                     (fn compare-filter [[path difference]]
                       (and (reg-key? path)
                            (not= :same difference)))
                     (make-title-reporter 100))]
    (stderr! "Comparing" hive "hive on this computer to" machine "under" subkey-csv)
    (println "[")
    (doseq [diff differences]
      (println diff))
    (println "]"))
  (stderr! "Done!"))
;(-main "SSI-TRVL-ENG01" "LocalMachine" "Software,7-Zip")