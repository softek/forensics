@ECHO OFF
SETLOCAL
git pull
for /f "usebackq" %%x in (`git rev-parse --abbrev-ref HEAD`) do set GIT_BRANCH=%%x
for /f "usebackq" %%x in (`git describe --long --tags --dirty`) do set GIT_DESCRIPTION=%%x

set Status=.archive.version.txt
ECHO Forensics Package %GIT_BRANCH% %GIT_DESCRIPTION% %date% %time%>%status%
ECHO.>>%status%
ECHO Recent Changes:>>%status%
git log "--pretty=format:%%h %%ad %%s%%d [%%an]%%x0D" --graph --date=short -n 10 >>%status%
ECHO.>>%status%
ECHO.>>%status%
call build.all.cmd
dir /b *dll *.exe |fileinfo --sha1 --ignore-system-path >>%status%
if exist Forensics.zip del Forensics.zip
zip -9 Forensics.zip * -x *.zip *.7z .gitignore .*set.txt
ECHO ============================== Packaging Complete ============================
type %status%
dir Forensics.zip
