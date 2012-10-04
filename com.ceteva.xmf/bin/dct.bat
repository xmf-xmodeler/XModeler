@echo off

rem Use this utility to deply a ClassTool.
rem usage: dct SOURCEDIR TOOL
rem where SOURCEDIR is the directory containing the Java source.
rem where TOOL is the tool description file in SOURCEDIR.

set SOURCEDIR=%1%
set TOOL=%2%
set TOOLDIR="%XMFHOME%"\..\com.ceteva.classToolEngine

java -cp "%XMFHOME%";%SOURCEDIR%;%TOOLDIR% tool/ToolFrame %SOURCEDIR%\%TOOL%