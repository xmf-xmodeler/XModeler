package com.ceteva.console.views;

import java.util.Vector;

public class History extends Vector {
	
  static final long serialVersionUID = 0;

  public static int historySize = 10;
  public int commandPointer = 0;

  public void add(String input) {
    
    // if the new command is the same as the one just pushed
    // ignore it
      
    if(!(size()>0 && input.equals(elementAt(size()-1)) && input.equals("")))  {
      
      // if we've reached the maximum size for the history
      // get rid of the old history  
        
      if(size()+1 >= historySize) {
        while(size()+1 != historySize)
          removeElementAt(0);
      }
      
      // add the element to the history
      
      addElement(input);
      // printState(); 
    }
    resetCommandPointer();
    
  }
  
  public String getPrevious() {
      
    // if there is a previous command  
      
    if(size()>0) {
      if(commandPointer-1 < 0) {
        commandPointer = size()-1;
        // printState();
        return (String)elementAt(commandPointer);
      }
      else {   
        commandPointer = commandPointer-1;
        // printState();
        return (String)elementAt(commandPointer);
      }
    }
    return "";
  }
  
  public String getNext() {
      
    // if there is a next command
    
    if(size()>0) {  
      if(commandPointer+1 >= size()) {  
        commandPointer = 0;
        // printState();
        return (String)elementAt(commandPointer);
      }
      else {   
        commandPointer = commandPointer+1;
        // printState();
        return (String)elementAt(commandPointer);
      }
    }
  	return "";
  }
  
  public void printState() {
    for(int i=0;i<size();i++) {
      String command = i + " : " + (String)elementAt(i);
      if(i == commandPointer)
        command = command + "<<<";     
      System.out.println(command);
    }
  }
  
  public void resetCommandPointer() {
    commandPointer = size();  
  }
  
  public void setSize(int size) {
  	historySize = size;
  }
}