#This file creates the various images
#! /bin/sh
clear
DIR=$1
cd $DIR
echo `pwd`
echo "DIR is "$DIR
PORT=2100
XMFHOME=$DIR
LIB=$XMFHOME

HEAPSIZE=10000
STACKSIZE=50
FREEHEAP=20
MAXJAVAHEAP=-Xmx150m
MAXJAVASTACKSIZE=-Xss2m
VERSION=2.0
XOSFILENAME=Boot/Makefile.o
TOOLFILENAME=Boot/Toolmaker.o
EVALFILENAME=Boot/BootEval.o
XMFIMAGE=$XMFHOME/Images/xmf.img
SERVERIMAGE=$XMFHOME/Images/server.img
MOSAICFILENAME=Boot/Mosaic/Boot.o
TOOLIMAGEFILENAME=Boot/Mosaic/BootToolCompiler.o
EVALUATORIMAGE=$XMFHOME/Images/eval.img
MOSAICIMG=$XMFHOME/Images/mosaic.img
SERVERFILENAME=Boot/Server/Boot.o
MOSAICMAKEFILENAME=Boot/Makefile.o
XMFFILENAME=Boot/Boot.o
TOOLSFILENAME=Tools/Manifest.o
COMPILERFILE=Boot/BootCompiler.o
TOOLIMAGE=$XMFHOME/Images/toolcompiler.img

buildImage(){ #1 parameter
#works
echo "Building Image"
java -cp $LIB XOS.OperatingSystem -port $PORT -initFile $1 -heapSize $HEAPSIZE -arg home:$XMFHOME
echo "Done building image"
}

first(){
#works
echo "Makefile XOS"
compileFileWithImg $EVALUATORIMAGE $XOSFILENAME
echo "Done XOS"
}

toolMaker(){
#works
echo "Makefile XOS"
compileFileWithImg $TOOLIMAGE $TOOLFILENAME
echo "Done XOS"
}
second(){
#works
echo "xmf.img, derived from makexmf.bat"
    buildImage $XMFHOME/$XMFFILENAME
echo "Done xmf"
}
third(){
#works
echo "eval.img"
java -cp $LIB XOS.OperatingSystem -port $PORT -image $XMFIMAGE -heapSize $HEAPSIZE -arg user:"$USERNAME" -arg home:$XMFHOME -arg license:license.lic -arg filename:$EVALFILENAME
echo "Done eval"
}
compileFileWithImg(){ #2 parameters
java $MAXJAVAHEAP $MAXJAVASTACKSIZE -cp $LIB XOS.OperatingSystem -port $PORT -image $1 -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$2 -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
}

fourth(){ 
#works
echo "server.img, derived from makexmfs.bat"
    compileFileWithImg $EVALUATORIMAGE $SERVERFILENAME
echo "Done server"
}
fifth(){ 
#wont work
echo "Makefile mosaic.img"
    compileFileWithImg $TOOLIMAGE $MOSAICMAKEFILENAME
echo "Done makefile mosaic"
}
toolMakerImage(){
echo "Makefile mosaic.img"
    compileFileWithImg $SERVERIMAGE $TOOLIMAGEFILENAME
echo "Done makefile mosaic"
}
sixth(){
#works
echo "Compile mosaic.img"
java $MAXJAVAHEAP -cp $LIB XOS.OperatingSystem -port $PORT -image $TOOLIMAGE -heapSize $HEAPSIZE -freeHeap $FREEHEAP -stackSize $STACKSIZE -arg filename:$MOSAICFILENAME -arg user:"$USERNAME" -arg home:"$XMFHOME" -arg license:license.lic -arg projects:"$XMFPROJECTS" -arg doc:"$XMFDOC" -arg version:"$VERSION"
echo "Done mosaic"
}
buildImage $XMFHOME/$COMPILERFILE
second
first
third
fourth
fifth
toolMakerImage
fifth
toolMaker
sixth

