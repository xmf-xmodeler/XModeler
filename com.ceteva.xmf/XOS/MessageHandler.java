package XOS;

public interface MessageHandler {
    
    public void sendMessage(Message message);
    
    public void sendPacket(MessagePacket packet);
    
    public Value callMessage(Message message);
    
    public void registerEventHandler(EventHandler handler);

}
