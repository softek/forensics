@ECHO OFF
setlocal

:: Interpret arguments
IF "%~1"=="/?"     GOTO :Usage
IF "%~1"=="-?"     GOTO :Usage
IF "%~1"=="--help" GOTO :Usage
if "x%~1"=="x"     GOTO :Usage

set archive=%~1

:: Check archive
dir /b "%archive%"
if errorlevel 1 ECHO Archive file not found: %archive%

call :Zip "%archive%"

goto :EOF


:Usage
  ECHO Extracts specified archive to a subdirectory of the current directory.
  ECHO.
  ECHO Usage:
  ECHO   Extract.cmd your-archive.7z
  goto :EOF

:Zip
  :: Check 7-zip
  where 7z.exe >nul 2>nul
  if errorlevel 1 set path=%path%;c:\Program Files\7-Zip;c:\Program Files (x86)\7-Zip
  where 7z.exe >nul 2>nul
  if errorlevel 1 echo Can't find 7-zip

  7z.exe x -y -r -o"%~n1" "%archive%"
