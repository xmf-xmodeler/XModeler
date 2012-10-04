package Mosaic;

import XOS.OperatingSystem;

import com.ceteva.consoleInterface.EscapeHandler;

class InterruptHandler implements EscapeHandler {

    OperatingSystem XOS;
    
    public InterruptHandler(OperatingSystem XOS) {
        this.XOS = XOS;
    } 
    
    public void interrupt() {
       XOS.interrupt();
    }
    
}