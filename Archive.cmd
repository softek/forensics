@ECHO OFF
SETLOCAL
set Status=.status.txt
set binset=.binset.txt
set archiveset=.archiveset.txt
echo Forensics Archive %date% %time%>%status%
echo.>>%status%

call :Identify
call :Zip
goto :EOF

:Identify
  if exist "%binset%" del "%binset%"
  dir /b apptest.exe authview.exe crmtest.exe ddirdump.exe ejstest.exe enumlog.exe getlog.exe mon_ss.exe mqss_audit.exe msgview.exe qcpview.exe quemon.exe qxcr.exe scpview.exe setlog.exe srvconfig.exe srvmon.exe sysmon.exe tdbview.exe testdns.exe testsec.exe >> "%binset%"
  deps.exe scpview.exe >> "%binset%" 
  type "%binset%"|find /i /v "logical.dll"|FileInfo --sha1|codename.exe>>%status%
  goto :EOF

:Zip
  type "%binset%">"%archiveset%"
  dir /b "%Status%">>"%archiveset%"
  call :SetArchive %cd%
  type "%archiveset%"|FileInfo --ignore-system-path| zip -9 "%archive%" -@ -xi *.zip 
  goto :EOF

:SetArchive
  SET Archive=%computername%.%Date:~10,4%-%Date:~4,2%-%Date:~7,2%.%~n1.zip
  ECHO ARCHIVE: %archive%
  goto :EOF
