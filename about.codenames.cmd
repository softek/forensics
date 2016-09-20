@ECHO OFF
setlocal

:: Interpret arguments
IF "%~1"=="/?"     GOTO :Usage
IF "%~1"=="-?"     GOTO :Usage
IF "%~1"=="--help" GOTO :Usage

set about_find=%~dp0\about.find.cmd
"%about_find%" "%~1" "codename:"
goto :EOF


:Usage
  ECHO Shows the folder version codenames for the specified archives.
  ECHO.
  ECHO Usage:
  ECHO   about.codenames.cmd [FileFilter]
  ECHO.
  ECHO Examples:
  ECHO   about.codenames.cmd                 -- shows *.7z
  ECHO   about.codenames.cmd *Cook*2016*.7z  -- shows matching files
