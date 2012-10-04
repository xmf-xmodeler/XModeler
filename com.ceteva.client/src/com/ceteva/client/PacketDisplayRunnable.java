package com.ceteva.client;

import XOS.Message;
import XOS.MessagePacket;

// Used to communicate messages to the GUI thread when we
// are not concerned about the result

public class PacketDisplayRunnable implements Runnable {
    
    protected MessagePacket packet;
    protected Client client;
    
    public PacketDisplayRunnable() {}
    
    public PacketDisplayRunnable(MessagePacket packet,Client client) {
        this.packet = packet;
        this.client = client;
    }
    
    public  void setPacket(MessagePacket packet){
        this.packet = packet;
    }
    
    public  void setClient(Client client) {
    	if(client.debug)
    		System.out.println("PACKAGE: "+ packet);
        this.client = client;
    }
    
    public void run() {
    	unpackPacket(packet);
    }
    
    public void unpackPacket(MessagePacket packet) {
    	for(int i=0;i<packet.getMessageCount();i++) {
    	  Message message = packet.getMessage(i);
    	  if(message!=null)
    	    client.processMessage(message);
    	}
    }
}
