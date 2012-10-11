#! /bin/sh
clear
DIR=$1
echo "DIR is "$DIR
PORT=100
XMFHOME=$DIR
LIB=$XMFHOME
EVALUATOR=$XMFHOME/Images/eval.img
HEAPSIZE=10000
STACKSIZE=50
FREEHEAP=20
MAXJAVAHEAP=-Xmx150m
MAXJAVASTACKSIZE=-Xss2m
VERSION=2.0
XOSFILENAME=Boot/Makefile.o
EVALFILENAME=Boot/BootEval.o
XMFIMAGE=$XMFHOME/Images/xmf.img
SERVERIMAGE=$XMFHOME/Images/server.img
MOSAICFILENAME=Boot/Mosaic/Boot.o
EVALUATORIMAGE=$XMFHOME/Images/eval.img
SERVERFILENAME=Boot/Server/Boot.o
MOSAICMAKEFILENAME=Boot/Makefile.o
#wont work
echo "xos.img, derived from xos.bat"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $EVALUATORIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$XOSFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION" %3 %4 %5 %6 %7 %8 %9
echo "Done xos"
#works
echo "eval.img"
java -cp $LIB XOS.OperatingSystem -port $PORT -image $XMFIMAGE -heapSize $HEAPSIZE -arg user:"$USERNAME" -arg home:$XMFHOME -arg license:license.lic -arg filename:$EVALFILENAME
echo "Done eval"
#works
echo "xmf.img, derived from makexmf.bat"
java -cp $LIB XOS.OperatingSystem -port $PORT -initFile $DIR/Boot/Boot.o -image $EVALUATORIMAGE -heapSize $HEAPSIZE -arg home:$DIR
echo "Done xmf"
#works
echo "server.img, derived from makexmfs.bat"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $EVALUATORIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$SERVERFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION" %3 %4 %5 %6 %7 %8 %9
echo "Done server"
#wont work
echo "Makefile mosaic.img"
java $MAXJAVAHEAP -cp $LIB XOS.OperatingSystem -port $PORT -image $SERVERIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$MOSAICMAKEFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION" %3 %4 %5 %6 %7 %8 %9
#works
echo "Compile mosaic.img"
java $MAXJAVAHEAP -cp $LIB XOS.OperatingSystem -port $PORT -image $SERVERIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$MOSAICFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION" %3 %4 %5 %6 %7 %8 %9
echo "Done mosaic"