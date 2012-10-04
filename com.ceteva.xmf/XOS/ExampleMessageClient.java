package XOS;


public class ExampleMessageClient extends java.lang.Thread implements MessageHandler {
    
    EventHandler handler;
    
    public ExampleMessageClient() {
        start();
    }
    
    public void registerEventHandler(EventHandler handler) {
        this.handler = handler;
    }
    
    public void sendMessage(Message message) {
        //System.out.println("sendMessage: " + message);
    }
    
    public void sendPacket(MessagePacket packet) {
        //System.out.println("sendPacket: " + packet);
    }
    
    public Value callMessage(Message message) {
        System.out.println("callMessage: " + message);
        return null;
    }
    
    public void run() {
        for(int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Message message = new Message("This is a reply",0);
            handler.raiseEvent("xxx",message);
        }
    }

}
