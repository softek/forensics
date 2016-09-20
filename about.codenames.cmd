@ECHO OFF
setlocal

set about_find=%~dp0\about_find.cmd

:: Usage:
:: about_codenames <FileFilter>
:: about_codenames *Cook*2016*.7z

"%about_find%" "%~1" "codename:"
