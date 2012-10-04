@echo off

set XMFHOME=%1%
set HEAPSIZE=15000

echo [ bin/makexmfs %1 %2 %3 %4 %5 %6 %7 %8 %9 ]

bin\xos %XMFHOME% Boot/Server/Boot.o -heapSize %HEAPSIZE%