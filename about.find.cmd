@ECHO OFF
setlocal

:: Interpret arguments
IF "%~1"=="/?"     GOTO :Usage
IF "%~1"=="-?"     GOTO :Usage
IF "%~1"=="--help" GOTO :Usage

IF "x"=="x%~1"     set filter=*.7z
IF NOT "x"=="x%~1" set filter=%~1


set about=%~dp0\about.cmd

:: Operate on matched files
for %%f in (%filter%) DO call :DoFindOrType "%%f" "%~2"
goto :EOF


:Usage
  ECHO Usage:
  ECHO   about.find.cmd [FileFilter [substring]]
  ECHO.
  ECHO Examples:
  ECHO   about.find.cmd                    -shows .about.txt file for all archives
  ECHO   about.find.cmd ""  "codename:"    -find matching substrings in all archives
  ECHO   about.find.cmd *Cook*.7z                 -shows .about.txt file for matching archives
  ECHO   about.find.cmd *2016*.7z  "Cumulative:"  -find lines with "Cumulative:"
  GOTO :EOF

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
