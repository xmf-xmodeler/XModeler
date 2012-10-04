@echo off

rem Use this utility to run exported java.
rem usage: xjava SOURCEDIR ENTRYCLASS
rem where SOURCEDIR is the directory containing the Java source.
rem where ENTRYCLASS is the class defining static main.

set SOURCEDIR=%1%
set ENTRYCLASS=%2%

java -verbose -cp "%XMFHOME%";%SOURCEDIR% %ENTRYCLASS%