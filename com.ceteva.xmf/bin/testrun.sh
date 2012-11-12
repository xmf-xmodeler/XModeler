#! /bin/sh
clear
DIR=$1
DEBUG=$2
cd $DIR
echo `pwd`
echo "DIR is "$DIR
PORT=2100
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
MOSAICIMAGE=$XMFHOME/Images/mosaic.img
XMFFILENAME=Boot/Boot.o
STARTFILE=$DIR/TopLevel/Loop.o

echo "test"
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $SERVERIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION" $DEBUG
echo "done"
