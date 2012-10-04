@echo off

set HOME=%1%
set OPTION=%2%

echo [ bin/make %1 %2 %3 %4 %5 %6 %7 %8 %9 ]

if -%OPTION% == -xmf goto xmf
if -%OPTION% == -xmfc goto xmfc
if -%OPTION% == -xmfe goto xmfe
if -%OPTION% == -xmfs goto xmfs
if -%OPTION% == -xmfm goto xmfm
cmd /C bin\xmfe %HOME% Boot/Makefile.o %2 %3 %4 %5 %6 %7 %8 %9
goto done

:xmf
cmd /C bin\xos %HOME% Boot/Makefile.o %3 %4 %5 %6 %7 %8 %9%
cmd /C bin\makexmf %HOME% %3 %4 %5 %6 %7 %8 %9
goto done

:xmfe
cmd /C bin\xos %HOME% Boot/Makefile.o %3 %4 %5 %6 %7 %8 %9
cmd /C bin\makexmf %HOME% %3 %4 %5 %6 %7 %8 %9
cmd /C bin\makexmfe %HOME% %3 %4 %5 %6 %7 %8 %9
goto done

:xmfs
cmd /C bin\xos %HOME% Boot/Makefile.o %3 %4 %5 %6 %7 %8 %9
cmd /C bin\makexmfs %HOME% %3 %4 %5 %6 %7 %8 %9
goto done

:xmfm
cmd /C bin\mosaic %HOME% Boot/Makefile.o %3 %4 %5 %6 %7 %8 %9
cmd /C bin\makexmfm %HOME% %3 %4 %5 %6 %7 %8 %9
goto done
  
:done
echo [ bin/make done. ]
