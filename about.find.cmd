@ECHO OFF
setlocal

set about=%~dp0\about.cmd

:: Usage:
:: about_find <FileFilter> "substring"
:: about_find *Cook*2016*.7z "Version"

IF "x"=="x%~1" set filter=*.7z
IF NOT "x"=="x%~1" set filter=%~1

for %%f in (%filter%) DO call :DoFindOrType "%%f" "%~2"
goto :EOF

:DoFindOrType
  ECHO ------- %~1
  IF "x%~2"=="x" GOTO :DoType "%~1" "%~2"
  IF NOT "x%~2"=="x" GOTO :DoFind "%~1" "%~2"
  GOTO :EOF

:DoFind
  "%about%" "%~1" |Find /i "%~2"
  goto :EOF

:DoType
  call "%about%" "%~1"
  goto :EOF
