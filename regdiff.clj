(ns regdiff
  (:import [Microsoft.Win32 RegistryKey Registry])
  (:gen-class))

(def hklm-software (.OpenSubKey Registry/LocalMachine "Software"))

(def reg-key? (partial instance? RegistryKey))

(defn reg-subkeys [^RegistryKey k]
  (map #(.OpenSubKey k %) (.GetSubKeyNames k)))
(defn reg-values [^RegistryKey k]
  (map
    (fn [value-name]
      {:key (.-Name k)
       :name value-name
       :kind (.GetValueKind k value-name)
       :value (.GetValue k value-name)})
    (.GetValueNames k)))

(reg-subkeys hklm-software)
(reg-values hklm-software)

(defn reg-children [^RegistryKey k]
  (concat (reg-subkeys k)
          (reg-values k)))
(reg-children hklm-software)

(defn reg-seq
  ([^RegistryKey k]
   (tree-seq reg-key? reg-children k)))

(clojure.pprint/pprint
  (take 100
    (reg-seq hklm-software)))
