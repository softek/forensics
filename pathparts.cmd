@echo off
powershell -Command ($env:Path).Replace(';',\"`r`n\")
