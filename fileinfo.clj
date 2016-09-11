(ns fileinfo
  (:require
    [clojure.string :as string]
    [diceware :as diceware])
  (:import
    [System
      Convert
      Environment]
    [System.IO
      Directory
      DirectoryInfo
      File
      Path
      Stream]
    [System.Security.Cryptography
      HashAlgorithm])
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

deps.exe scpview.exe|FileInfo --sha1 --sha1-dice --append-folder
"))

(defn base64 [bytes]
  (Convert/ToBase64String bytes))
(defn hexadecimal [bytes]
  (-> (BitConverter/ToString bytes)
      (.Replace "-" "")))
(defn bytes->dice-words [bytes]
  (diceware/bytes->words-string @diceware/dice-map (byte-array (take 4 bytes))))

(def bytes->string (atom hexadecimal))

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
  (->> dirs
       (mapcat #(apply get-all-files % patterns))
       (group-by #(Path/GetFileName %)))) ; the values retain original sequentiality

(def comment-chars #{\; \# \: \space \tab})
(defn comment-char? [c]
  (boolean (comment-chars c)))

(defn make-hash-bytes-fn [algorithm-name]
  (let [^HashAlgorithm hasher (HashAlgorithm/Create algorithm-name)]
    (fn hash-file-bytes [file-name]
      (with-open [^Stream stream (File/OpenRead file-name)]
        (.ComputeHash hasher stream)))))

(defn make-hash-string-fn [name hash-bytes-fn bytes->string]
  (comp #(str name ":" %)
        bytes->string
        hash-bytes-fn))

(def SHA1-hash-bytes (memoize (make-hash-bytes-fn "SHA1")))
(def SHA1-hash (make-hash-string-fn "SHA1" SHA1-hash-bytes hexadecimal))
(def SHA1-dice (make-hash-string-fn "SHA1-dice" SHA1-hash-bytes bytes->dice-words))

(def MD5-hash-bytes (memoize (make-hash-bytes-fn "MD5")))
(def MD5-hash (make-hash-string-fn "MD5" MD5-hash-bytes hexadecimal))
(def MD5-dice (make-hash-string-fn "MD5-dice" MD5-hash-bytes bytes->dice-words))

(defn output-functions [args]
  (let [contains-arg? #(boolean (some % (map string/lower-case args)))
        SHA1? (contains-arg? #{"--sha1"})
        MD5? (contains-arg? #{"--md5"})
        SHA1-dice? (contains-arg? #{"--sha1-dice"})
        MD5-dice? (contains-arg? #{"--md5-dice"})
        folder? (contains-arg? #{"--append-folder"})
        fns [(fn output-file-name [[file-name full-paths]]
                file-name)]]
    (cond-> fns
      SHA1?
       (conj
          (fn output-SHA1 [[file-name full-paths]]
            (when-let [f (first full-paths)]
              (SHA1-hash f))))
      MD5?
        (conj
          (fn output-MD5 [[file-name full-paths]]
            (when-let [f (first full-paths)]
              (MD5-hash f))))
      SHA1-dice?
       (conj
          (fn output-SHA1-dice [[file-name full-paths]]
            (when-let [f (first full-paths)]
              (SHA1-dice f))))
      MD5-dice?
        (conj
          (fn output-MD5-dice [[file-name full-paths]]
            (when-let [f (first full-paths)]
              (MD5-dice f))))
      folder? (conj
                (fn output-folder [[file-name full-paths]]
                  (->> full-paths
                       (map #(Path/GetDirectoryName %))
                       first))))))

(defn read-filenames-from-input-and-print-output [file-names args]
  (let [contains-arg? #(boolean (some % (map string/lower-case args)))
        dirs [(Environment/get_CurrentDirectory)
              (if (contains-arg? #{"--ignore-system-path"})
                []
                (get-existing-path-directories-and-warn!))]
        all-files-by-fn (get-all-files-in-directories-grouped-by-filename (flatten dirs))
        fns (output-functions args)
        output-fn (apply juxt fns)]
    (->> file-names
         (map #(.Trim %))
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
