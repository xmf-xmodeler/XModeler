@echo off

set XMFHOME=%1%
set LIB=%XMFHOME%;%XMFHOME%/EccpressoAll.jar;%XMFHOME%/flexlm.jar
set PORT=100
set EVALUATOR=%XMFHOME%/Images/eval.img
set HEAPSIZE=10000
set STACKSIZE=50
set FREEHEAP=20
set FILENAME=%2%
set MAXJAVAHEAP=-Xmx150m
set MAXJAVASTACKSIZE=-Xss2m
set VERSION=1.9
rem set PROFILE=-prof

echo [ bin/xos %1 %2 %3 %4 %5 %6 %7 %8 %9 ]

if exist %FILENAME% goto loadFile
if not defined FILENAME goto startEvaluator
if not exist %FILENAME% goto startEvaluator

:loadFile
java %MAXJAVAHEAP% %MAXJAVASTACKSIZE% -cp %LIB% XOS.OperatingSystem -port %PORT% -image %EVALUATOR% -heapSize %HEAPSIZE% -freeHeap %FREEHEAP% -stackSize %STACKSIZE% -arg filename:%FILENAME% -arg user:"%USERNAME%" -arg home:"%XMFHOME%" -arg license:license.lic -arg projects:"%XMFPROJECTS%" -arg doc:"%XMFDOC%" -arg version:"%VERSION%" %3 %4 %5 %6 %7 %8 %9
goto done

:startEvaluator
java %MAXJAVAHEAP% %MAXJAVASTACKSIZE% %PROFILE% -cp %LIB% XOS.OperatingSystem -port %PORT% -image %EVALUATOR% -heapSize %HEAPSIZE% -heapSize %HEAPSIZE% -stackSize %STACKSIZE% -arg user:"%USERNAME%" -arg home:"%XMFHOME%" -arg license:license.lic -arg projects:"%XMFPROJECTS%" -arg doc:"%XMFDOC%" -arg version:"%VERSION%" %2 %3 %4 %5 %6 %7 %8 %9
goto done

:done
echo [ bin\xos done. ]