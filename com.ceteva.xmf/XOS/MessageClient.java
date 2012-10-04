package XOS;


public class MessageClient extends Client {
    
    // A message client allows synchronous communication between XVM and arbitrary
    // Java via XOS.
    
    private MessageQueue messages = new MessageQueue();
    
    private MessageHandler handler;
    
    public MessageClient(OperatingSystem XOS,String name,MessageHandler handler) {
        super(XOS,name);
        this.handler = handler;
    }
    
    public Value call(Message message) {
        Value value = null;
		try {
			value = handler.callMessage(message);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return value;
    }
    
    public MessageQueue messages() {
        return messages;
    }
    
    public synchronized void queueMessage(Message message) {
        
        // Some external Java code has supplied a message for XVM. Queue
        // up the message and notify any threads that are currently waiting
        // on the client.
        
        messages.insert(message);
        notifyAll();
    }
    
    public Message readMessage() {
        if(messages.isEmpty())
            return null;
        else return messages.next();
    }
    
    public synchronized boolean ready() {
        return !messages.isEmpty();
    }
    
    public void sendMessage(Message message) {
      try {
        handler.sendMessage(message);
	  } catch (RuntimeException e) {
		e.printStackTrace();
	  }
    }
    
    public void sendPacket(MessagePacket packet) {
      try {
        handler.sendPacket(packet);
      } catch (RuntimeException e) {
    	e.printStackTrace();  
      }
    }

}
 