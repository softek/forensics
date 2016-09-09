(ns fileinfo
  (:require [clojure.string :as string])
  (:import
    [System Environment]
    [System.IO
      Directory
      DirectoryInfo
      File
      Path])
  (:gen-class))

(def usage-string (.ToString #"
FileInfo.exe reads a list of files from STDIN and shows information
like a SHA1 hash or the first directory in %PATH% that contains the file.

# Example 1
To show the sha1 of all the files in the current directory

dir /b|FileInfo --sha1

# Example 2
To show the first directory in %PATH% that contains the file.

echo scpview.exe|FileInfo --path --append-folder

# Example 3
To show the hash and location of scpview's dependencies

deps.exe scpview.exe|FileInfo --sha1 --append-folder
"))


(defn- stderr! [& args]
  (binding [*out* *err*]
    (apply println args)))

(defn get-path-directories []
  (string/split
    (Environment/GetEnvironmentVariable "PATH")
    #";"))

(defn get-existing-path-directories-and-warn! []
  (letfn [(exists-or-warn!? [dir]
            (if (Directory/Exists dir)
              dir
              (when-not (string/blank? dir)
                (stderr! "Warning: Directory specified in %path% does not exist:" dir))))]
    (filterv exists-or-warn!? (get-path-directories))))

(defn get-all-files
 ([dir] (get-all-files dir "*"))
 ([dir & patterns]
  (let [dir (DirectoryInfo. dir)
        file-names (->> patterns
                        (mapcat #(.GetFiles dir %))
                        (mapv #(.get_FullName %)))]
    (mapv string/lower-case file-names))))

(defn get-all-files-in-directories-grouped-by-filename [dirs & patterns]
  (->> (get-existing-path-directories-and-warn!)
       (mapcat #(apply get-all-files % patterns))
       (group-by #(Path/GetFileName %)))) ; the values retain original sequentiality

(def comment-chars #{\; \# \: \space \tab})
(defn comment-char? [c]
  (boolean (comment-chars c)))

(defn sha1-hash [file-name]
  ; TODO: generate sha1 hash
  (str "todo:hash:0000000000000 for " file-name))

(defn output-functions [args]
  (let [contains-arg? #(boolean (some % (map string/lower-case args)))
        sha1? (contains-arg? #{"--sha1"})
        folder? (contains-arg? #{"--append-folder"})
        fns [(fn output-file-name [[file-name full-paths]]
                file-name)]]
    (cond-> fns
      sha1?   (conj
                (fn output-SHA1 [[file-name full-paths]]
                  (when-let [f (first full-paths)]
                    (sha1-hash f))))
      folder? (conj
                (fn output-folder [[file-name full-paths]]
                  (->> full-paths
                       (map #(Path/GetDirectoryName %))
                       first))))))

(defn read-filenames-from-input-and-print-output [file-names args]
  (let [ ; TODO: look for --path arg to read system path
        dirs (concat ["."] (get-existing-path-directories-and-warn!))
        all-files-by-fn (get-all-files-in-directories-grouped-by-filename dirs)
        fns (output-functions args)
        output-fn (apply juxt fns)]
    (->> file-names
         (remove #(or (string/blank? %)
                      (comment-char? (first %))))
         (map #(let [file-name (string/lower-case %)]
                  (if-let [grp (all-files-by-fn (string/lower-case %))]
                    (string/join "," (output-fn [file-name grp]))
                    (str file-name " Not found"))))
         (map println)
         doall)))

; (read-filenames-from-input-and-print-output ["scpview.exe"] ["--SHA1" "--append-folder"])

(defn -main[& args]
  (if (some #{"/?" "-?" "--help"} args)
    (stderr! usage-string)
    (read-filenames-from-input-and-print-output
      (line-seq (Console/get_In))
      args)))
