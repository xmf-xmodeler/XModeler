#! /bin/sh
clear
DIR=$1
cd $DIR
echo `pwd`
echo "DIR is "$DIR
PORT=2100
XMFHOME=$DIR
LIB=$XMFHOME
EVALUATOR=$XMFHOME/Images/eval.img
MOSAICIMG=$XMFHOME/Images/mosaic.img
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
XMFFILENAME=Boot/Boot.o

first(){
#works
echo "Makefile XOS"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $EVALUATORIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$XOSFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
echo "Done XOS"
}
second(){
#works
echo "xmf.img, derived from makexmf.bat"
java -cp $LIB XOS.OperatingSystem -port $PORT -initFile $XMFHOME/$XMFFILENAME -heapSize $HEAPSIZE -arg home:$XMFHOME
echo "Done xmf"
}
third(){
#works
echo "eval.img"
java -cp $LIB XOS.OperatingSystem -port $PORT -image $XMFIMAGE -heapSize $HEAPSIZE -arg user:"$USERNAME" -arg home:$XMFHOME -arg license:license.lic -arg filename:$EVALFILENAME
echo "Done eval"
}
fourth(){
#works
echo "server.img, derived from makexmfs.bat"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $EVALUATORIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$SERVERFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
echo "Done server"
}
fifth(){
#wont work
echo "Makefile mosaic.img"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $SERVERIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$MOSAICMAKEFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
echo "Done makefile mosaic"
}
sixth(){
#works
echo "Compile mosaic.img"
java $MAXJAVAHEAP -cp $LIB XOS.OperatingSystem -port $PORT -image $SERVERIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$MOSAICFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
echo "Done mosaic"
}
first
second
first
second
third
first
fourth
fifth
sixth