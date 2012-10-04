package com.ceteva.client;

import XOS.Message;
import XOS.Value;

// Used to communicate messages to the GUI thread when we
// are concerned about the result, this is placed in a
// a known place and can be retrieved by a getter

public class CallDisplayRunnable extends MessageDisplayRunnable {
    
	Value value = null;
	
	public CallDisplayRunnable() {
		super();
	}
	
    public CallDisplayRunnable(Message message,Client client) {
        super(message,client);
    }
    
    public Value getValue() {
    	return value;
    }
    
    public void run() {
    	if(client.debug)
    		System.out.println("MESSAGE: "+message);
        value = client.processCall(message);
    }
}