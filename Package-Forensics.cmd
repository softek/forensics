@ECHO OFF
SETLOCAL
git pull
set Status=.status.txt
ECHO Forensics Package %date% %time%>%status%
ECHO.>>%status%
git describe --long --tags --dirty >>%status%
git rev-parse --abbrev-ref HEAD >>%status%
ECHO.>>%status%
ECHO Recent Changes:>>%status%
git log "--pretty=format:%%h %%ad %%s%%d [%%an]%%x0D" --graph --date=short -n 10 >>%status%
ECHO.>>%status%
ECHO.>>%status%
call build.all.cmd
dir /b *dll *.exe |fileinfo --sha1 --ignore-system-path >>%status%
if exist Forensics.zip del Forensics.zip
zip -9 Forensics.zip * -x *.zip .gitignore .*set.txt
ECHO ============================== Packaging Complete ============================
type %status%
dir Forensics.zip