@echo off
ECHO Adding %~dp0 to system path
set path=%path%;%~dp0
setlocal
for /d %%d in (bin*) DO archive-sub1.cmd %%d