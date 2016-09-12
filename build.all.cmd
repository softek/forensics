call build.modules.cmd
del fileinfo.clj.dll fileinfo.exe regdiff.clj.dll regdiff.exe diceware.clj.dll diceware.exe
Clojure.Compile.exe deps regdiff diceware fileinfo
dir /b Clojure* |fileinfo --sha1-dice --ignore-system-path --full-path