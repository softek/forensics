call build.modules.cmd
del fileinfo.clj.dll fileinfo.exe regdiff.clj.dll regdiff.exe diceware.clj.dll diceware.exe codename.clj.dll codename.exe
Clojure.Compile.exe deps regdiff diceware fileinfo codename
dir /b Clojure* |fileinfo --sha1-dice --ignore-system-path --full-path
