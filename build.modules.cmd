@ECHO OFF
IF EXIST modules.exe del modules.exe
IF EXIST modules64.exe del modules64.exe
\Windows\Microsoft.NET\Framework64\v4.0.30319\csc /nologo /debug /platform:anycpu               /out:modules64.exe modules.cs
\Windows\Microsoft.NET\Framework64\v4.0.30319\csc /nologo /debug /platform:anycpu32bitpreferred /out:modules.exe   modules.cs
