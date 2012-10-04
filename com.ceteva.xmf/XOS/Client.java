package XOS;

public abstract class Client {
    
    // A client is a computational resource that is registered with XOS for
    // communication with XVM. Clients are named and may be synchronous or
    // asynchronous. Data may be supplied as messages or raw bytes.
    
    public static XmfIOHandler ioHandler = new DefaultIOHandler();;
	
	protected OperatingSystem os;  // A client informs XOS when it has data ready.

    protected String        name; // The unique name for the client.
    
    public Client(OperatingSystem XOS,String name) {
        this.os = XOS;
        this.name = name;
    }
    
    public abstract boolean ready();
    
    public String toString() {
        return "Client(" + name + ")";
    }

    public static void setIOHandler(XmfIOHandler ioHandler) {
    	Client.ioHandler = ioHandler;
    }
    
    public static XmfIOHandler getIOHandler() {
    	return Client.ioHandler;
    }

}
