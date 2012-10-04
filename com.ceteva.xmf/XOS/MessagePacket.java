package XOS;

import java.util.Vector;

public class MessagePacket {
	
	// A message package consists of a number of Messages
	
	private Vector messages = new Vector();
	private int messageCount = 0;
	
	public MessagePacket(int messageCount) {
	  this.messageCount = messageCount;	
	}
	
	public void addMessage(int position,Message message) {
	  messages.insertElementAt(message,position);	
	}
	
	public void reset() {
	  messages.clear();	
	}
	
	public Message getMessage(int position) {
	  return (Message)messages.elementAt(position);	
	}
	
	public int getMessageCount() {	
	  return messageCount;	
	}
	
	public void setMessageCount(int messageCount) {
	  this.messageCount = messageCount;	
	}

}
