@ECHO OFF
:: Downloads and extracts ClojureClr to the specified directory (Default: "..")
SETLOCAL
set dest=%~1
set ClojureVersion=1.7.0
if "x%dest%"=="x" SET DEST=..
ECHO Checking dependencies in %DEST%
call :Test
if not errorlevel 1 ECHO Good!  Dependencies are present.
if not errorlevel 1 goto :EOF

ECHO Couldn't find a dependency.  Let's go find it.

                             set ClojureZip=Clojure.%ClojureVersion%.zip
if not exist "%ClojureZip%". set ClojureZip=%temp%\Clojure.%ClojureVersion%.zip
if not exist "%ClojureZip%". CALL :Download . ELSE ECHO Found pre-downloaded Clojure Package: %ClojureZip%

call :Extract

call :Test

if     errorlevel 1     ECHO Couldn't find the dependencies.  This probably won't work.
if not errorlevel 1     ECHO Now we're ready to go!

GOTO :EOF

:Download
  ECHO Attempting retrieval from Nuget.  We're inter-networked right?  Oh, and we'll need curl.exe
  ECHO Downloading Clojure (CLR) nuget package to %ClojureZip%
  curl https://www.nuget.org/api/v2/package/Clojure/%ClojureVersion% -L -o "%ClojureZip%"
  if errorlevel 1 ECHO You may need to download the Clojure version=%ClojureVersion% nuget package and move it to "%ClojureZip%"
  goto :EOF

:Extract
  ECHO Extracting Clojure Clr to %dest%
  unzip -o -j "%ClojureZip%" tools\net40\* lib\net40\* -d "%dest%"
  goto :EOF

:Test
  :: See if the dependencies are in the destination.
                      where "%dest%":Clojure.dll
  if not errorlevel 1 where "%dest%":Microsoft.Dynamic.dll
  if not errorlevel 1 where "%dest%":Microsoft.Scripting.dll
  if not errorlevel 1 where "%dest%":Clojure.Main.exe
  if not errorlevel 1 where "%dest%":Clojure.Compile.exe
  GOTO :EOF
