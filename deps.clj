(ns deps
  (:require [clojure.string :as string])
  (:gen-class))

(def usage-string (.ToString #"
USAGE to list the dependencies of a program: 

deps.exe scpview.exe

- OR to show the places where these files would be found -

@echo off
for /F %f in ('deps.exe scpview.exe') DO ECHO ======================= %f ======================= &&where %f
@echo on
"))


(defn- stderr! [& args]
  (binding [*out* *err*]
    (apply println args)))

(defn- title! [& args]
  (System.Console/set_Title
    (apply print-str args)))

(def currently-reading-files (atom #{}))

(defn update-currently-reading-title! []
  (title! "Reading dependences from " @currently-reading-files))

(defn read-dependencies-from-file [f-name]
  (let [file-name (.ToLower f-name)
        _ (swap! currently-reading-files #(conj % file-name))
        _ (update-currently-reading-title!)
        deps
          (->> (System.IO.File/ReadAllText file-name)
               (re-seq #"(?i)[%a-z0-9._-]{1,100}\.dll")
               (remove #(>= (.IndexOf % \%) 0))   ; remove C-language printf-style names
               (mapv #(.ToLower %)))
        _ (swap! currently-reading-files #(disj % file-name))
        _ (update-currently-reading-title!)]
    deps))

(defn dependencies-from-folder [directory-name file-name]
  (let [dir (System.IO.DirectoryInfo. directory-name)
        file-names (->> [file-name "*.dll"]
                        (mapcat #(.GetFiles dir %))
                        (map #(.ToLower (.get_Name %))))]
    (->> file-names
         (pmap
            (fn[f][f (->> (read-dependencies-from-file f)
                          (remove #(= f %)))]))
         (apply concat)
         (apply hash-map))))

;(clojure.pprint/pprint (dependencies-from-folder "."))

(defn deps-recursive [directory-name file-name]
  (let [file-deps (dependencies-from-folder directory-name file-name)
        deps (tree-seq
                 #(let [f (last %)]
                    (and (->> f file-deps boolean)
                         (-> % frequencies (get f) (= 1))))
                 (fn[n]
                   (mapv #(conj n %) (file-deps (last n))))
                 [file-name])]              ; root node
    deps))


(defn deps-recursive2 [directory-name file-name]
  (let [file-deps (dependencies-from-folder directory-name file-name)
        deps (tree-seq
                 #(->> % file-deps boolean)
                 file-deps
                 file-name)]              ; root node
    deps))

(defn -main[& args]
  (if (or (empty? args) (some #{"/?" "-?" "--help"} args))
    (stderr! usage-string)
    (let [root-file-name (string/lower-case (first args))
          _ (stderr! "Running dependencies in current directory"
              (System.Environment/get_CurrentDirectory)
              "for" root-file-name)
          deps (map last (deps-recursive "." root-file-name))]

      (title! "Reading dependences from " root-file-name)
      (loop [[d & r] deps
             printed #{}]
        (if d
          (do
            (title! "Found" (count printed) "Evaluating" d)
            (when-not (printed d)
              (stderr! "Found" d))
            (recur r (conj printed d)))
          (->> printed
               sort
               (map println)
               doall))))))
