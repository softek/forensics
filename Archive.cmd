@ECHO OFF
SETLOCAL
set ArchiveVersion=Alpha
set Status=.status.txt
set binset=.binset.txt
set archiveset=.archiveset.txt
echo Forensics Archive %date% %time%>%status%
echo.>>"%status%"

call :Identify
call :MQ
call :Zip
goto :EOF

:Identify
  if exist "%binset%" del "%binset%"
  dir /b apptest.exe authview.exe crmtest.exe ddirdump.exe ejstest.exe enumlog.exe getlog.exe mon_ss.exe mqss_audit.exe msgview.exe qcpview.exe quemon.exe qxcr.exe setlog.exe srvconfig.exe srvmon.exe sysmon.exe tdbview.exe testdns.exe testsec.exe >> "%binset%"
  deps.exe scpview.exe >> "%binset%" 
  type "%binset%"|find /i /v "logical.dll"|FileInfo --sha1 --ignore-system-path|codename.exe>>"%status%"
  ECHO.>>"%status%"
  ECHO Archive Script Version: %ArchiveVersion%>>"%status%"
  ECHO.>>"%status%"
  goto :EOF

:MQ
  ECHO.>>"%status%"
  ECHO============================================================================>>"%status%"
  where dspmqver.exe >nul 2>nul
  if errorlevel 1        ECHO Can't find dspmqver.exe>>"%status%"
  if not errorlevel 1    dspmqver.exe -p 1 >>"%status%"
  ECHO.>>"%status%"
  ECHO============================================================================>>"%status%"
  ECHO.>>"%status%"
  goto :EOF

:Zip
  where 7z.exe >nul 2>nul
  if errorlevel 1 set path=%path%;c:\Program Files\7-Zip;c:\Program Files (x86)\7-Zip
  where 7z.exe >nul 2>nul
  if errorlevel 1 echo Can't find 7-zip
  type "%binset%">"%archiveset%"
  dir /b "%Status%">>"%archiveset%"
  call :SetArchive %cd%
  if exist "%archive%" del "%archive%"
  type "%archiveset%"|FileInfo --ignore-system-path>"%archiveset%2"
  7z a "%archive%" @"%archiveset%2"
  goto :EOF

:SetArchive
  SET Archive=%computername%.%Date:~10,4%-%Date:~4,2%-%Date:~7,2%.%~n1.7z
  ECHO ARCHIVE: %archive%
  goto :EOF
