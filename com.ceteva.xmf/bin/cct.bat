@echo off

rem Use this utility to compile a ClassTool deployable system.
rem usage: cct SOURCEDIR
rem where SOURCEDIR is the directory containing the Java source.

set SOURCEDIR=%1%

javac -verbose -classpath "%XMFHOME%";%SOURCEDIR% %SOURCEDIR%/*.java