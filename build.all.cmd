call build.modules.cmd
call pre-build.cmd .
del fileinfo.clj.dll fileinfo.exe regdiff.clj.dll regdiff.exe diceware.clj.dll diceware.exe codename.clj.dll codename.exe
Clojure.Compile.exe deps regdiff diceware fileinfo codename
dir /b *dll *.exe |fileinfo --sha1 --ignore-system-path
