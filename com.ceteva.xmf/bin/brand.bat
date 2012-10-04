@echo off

set XMFHOME=%1%
set LIB=%XMFHOME%;%XMFHOME%/EccpressoAll.jar;%XMFHOME%/flexlm.jar
set MAXJAVAHEAP=-Xmx150M

echo [ bin/brand %2 %3 %4 %5 %6 %7 %8 %9 ]

java %MAXJAVAHEAP% -cp %LIB% Engine.Brand %2 %3 %4 %5 %6 %7 %8 %9