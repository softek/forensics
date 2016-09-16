@echo off
setlocal
ECHO Temporarilly adding %~dp0 to system path
set path=%path%;%~dp0
for /d %%d in (bin*) DO archive-sub1.cmd %%d