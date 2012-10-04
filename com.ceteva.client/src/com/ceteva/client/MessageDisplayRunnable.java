package com.ceteva.client;

import XOS.Message;

// Used to communicate messages to the GUI thread when we
// are not concerned about the result

public class MessageDisplayRunnable implements Runnable {
    
    protected Message message;
    protected Client client;
    
    public MessageDisplayRunnable() {}
    
    public MessageDisplayRunnable(Message message,Client client) {
        this.message = message;
        this.client = client;
    }
    
    public  void setMessage(Message message){
        this.message = message;
    }
    
    public  void setClient(Client client) {
    	if(client.debug)
    		System.out.println("MESSAGE: "+message);
        this.client = client;
    }
    
    public void run() {
    	client.processMessage(message);
    }
}
