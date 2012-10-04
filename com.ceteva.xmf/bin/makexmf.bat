@echo off

set XMFHOME=%1%
set LIB=%XMFHOME%;%XMFHOME%/EccpressoAll.jar;%XMFHOME%/flexlm.jar
set PORT=100
set HEAPSIZE=3000

java -cp %LIB% XOS.OperatingSystem -port %PORT% -initFile %XMFHOME%/Boot/Boot.o -heapSize %HEAPSIZE% -arg home:%XMFHOME% -arg license:license.lic %2 %3 %4 %5 %6 %7 %8