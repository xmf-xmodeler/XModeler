package Mosaic;

public class MosaicRun {
	
  public static boolean runningMosaic = false;
  
  public static void runningMosaic() {
  	runningMosaic = true;
  }
  
  public static boolean mosaicIsRunning() {
  	return runningMosaic;
  }
}