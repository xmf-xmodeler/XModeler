@echo off

set XMFHOME=%1%
set LIB=%XMFHOME%;%XMFHOME%/EccpressoAll.jar;%XMFHOME%/flexlm.jar
set PORT=100
set XMFIMAGE=%XMFHOME%/Images/xmf.img
set HEAPSIZE=3000

echo [ bin/xmf %1 %2 %3 %4 %5 %6 %7 %8 %9 ]

java -cp %LIB% XOS.OperatingSystem -port %PORT% -image %XMFIMAGE% -heapSize %HEAPSIZE% -arg user:"%USERNAME%" -arg home:%XMFHOME% -arg license:license.lic -arg filename:%2 %3 %4 %5 %6 %7 %8 %9
 
