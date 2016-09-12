call build.modules.cmd
del fileinfo.clj.dll fileinfo.exe regdiff.clj.dll regdiff.exe diceware.clj.dll diceware.exe vordsion.clj.dll vordsion.exe
Clojure.Compile.exe deps regdiff diceware fileinfo vordsion
dir /b Clojure* |fileinfo --sha1-dice --ignore-system-path --full-path
