@echo off

set XMFHOME=%1%
set LIB=%XMFHOME%;%XMFHOME%/EccpressoAll.jar;%XMFHOME%/flexlm.jar

echo [ bin/makexmfe %1 %2 %3 %4 %5 %6 %7 %8 %9 ]

bin\xmf %XMFHOME% Boot/BootEval.o %2 %3 %4 %5 %6 %7 %8 %9