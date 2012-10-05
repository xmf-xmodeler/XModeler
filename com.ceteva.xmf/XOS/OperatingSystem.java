package XOS;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import Engine.ArgParser;
import Engine.Machine;
import Engine.Thread;

public final class OperatingSystem implements EventHandler {

    // XOS is the XMF operating system. It is responsible for managing XMF
    // clients, input and output channels and for scheduling XMF threads.
    // The operating system is (Java) multi-threaded. A thread monitors
    // requests for connections via sockets. Multiple threads monitor input
    // streams and ensure that the XVM is woken up when all threads are
    // blocking on input.

    // Clients are maintained in the following table.
    // The keys are the ids of the clients. Each client must have
    // a unique identifier.

    private Vector            clients               = new Vector();

    // Threads may block waiting for a client to connect. A connection
    // blocking thread is removed from the scheduled threads queue and
    // added to the XOS connection monitor. When the named client
    // connects, the thread is re-scheduled.

    private Vector            clientThreads         = new Vector();

    // XOS is always listening for clients trying to connect. This service
    // is provided by a connection monitor. The port used for client
    // connections is passed to XOS as a command line argument.

    private ConnectionMonitor connectionMonitor;

    // Set the following flag to true for debug output...

    public boolean            debug                 = false;

    // The input channels for XVM are allocated and maintained by XOS.
    // At the XVM level channels are referenced via their index into
    // the following vector. Each element in this vector is a Java
    // InputStream. It is important that the input channels all
    // support a mechanism for asking whether input is available or not
    // without blocking. When XVM performs a READ on an input channel
    // it will yield the current thread (placing the thread in blocking
    // mode) if there is nothing available on the underlying Java input
    // stream. All input streams have their own Java thread that performs
    // a blocking read (actually a peek) that informs XOS when the
    // input stream is ready to produce some input. XOS restarts XVM
    // threads blocking on a read for a Java stream that becomes ready.

    private Vector            inputChannels         = new Vector();

    // When a thread blocks on input, a new input monitor is created and
    // added to the collection inputMonitors. The monitor waits for input
    // to become available on the input stream and then schedules the
    // associated thread. XOS knows about the currently active monitors
    // in order to service ready requests on the input stream without
    // blocking.

    private Vector            inputMonitors         = new Vector();

    // XOS is supplied with the names of internal clients via the -internal
    // command line argument. The value of the argument should be the
    // qualified name of the class relative to CLASSPATH.

    private Vector            internalClientNames   = new Vector();

    // XOS is supplied with the names of message clients via the -message
    // command line argument. The value of the argument should be the
    // qualified name of the class relative to CLASSPATH.

    private Vector            messageClientNames    = new Vector();

    // Message monitors are used to detect incoming messages for message
    // based clients.

    private Vector            messageMonitors       = new Vector();

    // A pool of free messages that can be used by clients and XVM.

    private Stack             messagePool           = new Stack();
    
    // A pool of free packages for messages that can be used by clients and XVM.
    
    private Stack			  packetPool			= new Stack();

    // The output channels for XVM are allocated and maintained by XOS.
    // The channels are represented in the machine as their index into
    // the following table. The elements of the table are Java output
    // streams.

    private Vector            outputChannels        = new Vector();

    // XOS listens on a supplied port for external client connections.
    // The port number is supplied as a command line argument.

    private int               port                  = -1;

    // XVM schedules threads by placing them on the scheduledThreads
    // queue in XOS. XOS runs a thread that removes the next scheduled
    // thread and runs it (servicing any outstanding requests, e.g.
    // for input).

    private ThreadQueue       scheduledThreads      = new ThreadQueue();

    // A pool of free values used in messages.

    private Stack             valuePool             = new Stack();

    // Message clients may be started by XOS or may attach themselves to XOS.
    // Any message clients that attach themselves to XOS are added to this
    // vector of client names. If this is non empty during startup then
    // XOS initialisation waits for all message clients to attach before
    // proceeding.

    private Vector            waitingMessageClients = new Vector();

    // XOS contains a single XVM that is used to run each of the
    // threads.

    private Machine           XVM                   = new Machine(this);

    // The following string array is populated with the command line
    // arguments appropriate for XOS. Population is performed in parseArgs.

    private String[]          XOSargs;

    // The following two string arrays specify the command line arguments
    // appropriate for XOS and XVM. Each element is the name of the arg
    // and a definition of the parameters.

    private String[]          XOSargSpecs           = {
            "-port:1", "-debug:0", "-internal:1", "-message:1" };
    
    private UserPassword auth;

    public synchronized void addInputMonitor(StreamMonitor inputMonitor) {
        inputMonitors.add(inputMonitor);
    }

    public synchronized void addMessageMonitor(MessageMonitor inputMonitor) {
        messageMonitors.add(inputMonitor);
    }

    public Message allocMessage() {
        return allocMessage("", 0);
    }

    public synchronized Message allocMessage(String name, int arity) {

        // Called when a message is required.

        if (messagePool.isEmpty())
            return new Message(name, arity);
        else {
            Message message = (Message) messagePool.pop();
            message.setName(name);
            message.arity = arity;
            return message;
        }
    }
    
    public synchronized MessagePacket allocPacket(int messageCount) {
    	if(packetPool.isEmpty())
    	  return new MessagePacket(messageCount);
    	else {
    	  MessagePacket packet = (MessagePacket)packetPool.pop();
    	  packet.reset();
    	  packet.setMessageCount(messageCount);
    	  return packet;
    	}
    }

    public synchronized Value allocValue() {
        if (valuePool.isEmpty())
            return new Value();
        else {
            Value value = (Value) valuePool.pop();
            value.reset();
            return value;
        }
    }

    public synchronized Value allocValue(boolean b) {

        // Called when a value is required.

        if (valuePool.isEmpty())
            return new Value(b);
        else {
            Value value = (Value) valuePool.pop();
            value.setBoolValue(b);
            return value;
        }
    }

    public synchronized Value allocValue(int i) {

        // Called when a value is required.

        if (valuePool.isEmpty())
            return new Value(i);
        else {
            Value value = (Value) valuePool.pop();
            value.setIntValue(i);
            return value;
        }
    }

    public synchronized Value allocValue(float f) {

        // Called when a value is required.

        if (valuePool.isEmpty())
            return new Value(f);
        else {
            Value value = (Value) valuePool.pop();
            value.setFloatValue(f);
            return value;
        }
    }

    public synchronized Value allocValue(String s) {

        // Called when a value is required.

        if (valuePool.isEmpty())
            return new Value(s);
        else {
            Value value = (Value) valuePool.pop();
            value.setStrValue(s);
            return value;
        }
    }

    public synchronized Value allocValue(Value[] values) {

        // Called when a value is required.

        if (valuePool.isEmpty())
            return new Value(values);
        else {
            Value value = (Value) valuePool.pop();
            value.setValues(values);
            return value;
        }
    }

    public StreamClient asyncClient(String name) {

        // Returns the asynchronous client with the given name or null.

        Client client = client(name);
        if (client instanceof StreamClient && client != null)
            return (StreamClient) client;
        else
            return null;

    }

    public int available(int channelIndex) {

        // Returns the number of bytes available on the next read
        // for the input channel. Returns -1 if an error occurs.

        debug("Available(" + channelIndex + ")");
        XInputStream in = inputChannel(channelIndex);
        int available = -1;
        try {
            if (in != null)
                available = in.available();
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
        return available;
    }

    public void blockOnAccept(String name) {

        // The current thread is about to yield. It changes state to
        // block on the connection of the named client. When the named
        // client connects the thread will be restarted.

        XVM.currentThread().blockOnAccept(name);
    }

    public void blockOnRead(int index) {

        // The current thread is about to yield. It changes state to
        // block on read and remembers the input channel. The thread
        // will be restarted by XOS if input becomes available.

        debug("Thread " + XVM.currentThread() + " is blocking because " + inputChannel(index) + " is not ready");

        XVM.currentThread().blockOnRead(index);
    }

    public void blockOnRead(String name) {

        // The name should be a message client.

        debug("Thread " + XVM.currentThread() + " is blocking because " + name + " is not ready");

        XVM.currentThread().blockOnRead(name);
    }

    public Value call(String name, Message message) {
        MessageClient client = messageClient(name);
        if (client != null)
            return client.call(message);
        else
            throw new Error("Unknown client " + name);
    }

    public int charCount(int index) {
        TChannel in = tokenInputChannel(index);
        return in.charCount();
    }

    public Client client(StringBuffer name) {

        // Returns the client with the supplied name or null if
        // no client exists with the name.

        for (int i = 0; i < clients.size(); i++) {
            Client client = (Client) clients.elementAt(i);
            if (client.name.length() == name.length()) {
                boolean match = true;
                for (int j = 0; j < name.length() && match; j++) {
                    if (client.name.charAt(j) != name.charAt(j))
                        match = false;
                }
                if (match)
                    return client;
            }
        }
        return null;
    }

    public Client client(String name) {

        // Returns the client with the supplied name or null if
        // no client exists with the name.

        for (int i = 0; i < clients.size(); i++) {
            Client client = (Client) clients.elementAt(i);
            if (client.name.equals(name))
                return client;
        }
        return null;
    }

    public String clientSpecClassName(String clientSpec) {

        // Returns the argument name portion of the supplied client spec.

        String[] strings = clientSpec.split(":");
        if (strings.length < 1)
            throw new Error("A client spec should take the form <CLASS>:<CLIENT>:<OPTARGS> " + clientSpec);
        else
            return strings[0];
    }

    public String clientSpecClientName(String clientSpec) {

        // Returns the name used for the instance of an internal client class.

        String[] strings = clientSpec.split(":");
        if (strings.length < 2)
            throw new Error("A client spec should take the form <CLASS>:<CLIENT>:<OPTARGS> " + clientSpec);
        else
            return strings[1];
    }

    public String[] clientSpecClientArgs(String clientSpec) {

        // Returns an array of arguments supplied with the client spec.

        String[] strings = clientSpec.split(":");
        if (strings.length < 2)
            throw new Error("A client spec should take the form <CLASS>:<CLIENT>:<OPTARGS> " + clientSpec);
        else {
            String[] args = new String[strings.length - 2];
            for (int i = 2; i < strings.length; i++)
                args[i - 2] = strings[i];
            return args;
        }
    }

    public void closeAll() {

        // Closes all open channels prior to shutting down.

        for (int i = 0; i < inputChannels.size(); i++) {
            InputStream in = (InputStream) inputChannels.elementAt(i);
            try {
                debug("Close " + in);
                in.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        for (int i = 0; i < outputChannels.size(); i++) {
            OutputStream out = (OutputStream) outputChannels.elementAt(i);
            try {
                debug("Close " + out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        if (connectionMonitor != null)
            connectionMonitor.close();
    }

    public void closeInputChannel(int index) {
        XInputStream in = inputChannel(index);
        if (in != null)
            try {
                in.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal input channel index: " + index);
    }

    public void closeOutputChannel(int index) {
        OutputStream out = outputChannel(index);
        if (out != null)
            try {
                out.close();
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        else
            throw new Error("Illegal output channel index: " + index);
    }

    public void createNewMessageClient(String className, String clientName) {
        try {
            Class clientClass = Class.forName(className);
            Object object = clientClass.newInstance();
            if (object instanceof MessageHandler) {
                MessageHandler handler = (MessageHandler) object;
                newMessageClient(clientName, handler);
                handler.registerEventHandler(this);
            } else {
                System.out.println("Clients must be instances of XOS.MessageHandler: " + className);
                System.exit(0);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot find class named " + className);
            System.exit(0);
        } catch (InstantiationException ie) {
            System.out.println("Cannot instantiate " + className);
            System.exit(0);
        } catch (IllegalAccessException iae) {
            System.out.println(iae);
            System.exit(0);
        } catch (SecurityException e) {
            System.out.println(e);
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public DChannel dataInputStream(int index) {

        // Return the requested data input stream at the supplied index.
        // If no data output stream exists then return null.

        InputStream in = inputChannel(index);
        if (in instanceof DChannel)
            return (DChannel) in;
        else
            return null;
    }

    public DataOutputStream dataOutputStream(int index) {

        // Return the requested data output stream at the supplied index.
        // If no data output stream exists then return null.

        OutputStream out = outputChannel(index);
        if (out instanceof DataOutputStream)
            return (DataOutputStream) out;
        else
            return null;
    }

    public void debug(String message) {
        if (debug) {
            System.err.println(java.lang.Thread.currentThread() + ": " + message);
            System.err.flush();
        }
    }

    public synchronized void deleteInputMonitor(StreamMonitor inputMonitor) {
        inputMonitors.removeElement(inputMonitor);
    }

    public boolean eof(int channelIndex) {

        // Returns true if the end of file has been reached on the
        // input channel. Note that EOF must have been encountered
        // by performing a read that returns -1.

        debug("eof(" + channelIndex + ")");
        XInputStream in = inputChannel(channelIndex);
        return in.eof();
    }

    public void flush(int index) {
        debug("flush(" + index + ")");
        OutputStream out = outputChannel(index);
        try {
            out.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public synchronized void freeMessage(Message message) {

        // Called when we can release a message for future use...

        for (int i = 0; i < message.arity; i++)
            freeValue(message.args[i]);
        messagePool.push(message);
    }
    
    public synchronized void freePacket(MessagePacket packet) {
        
    	// free the contained messages
    	
    	for (int i = 0; i < packet.getMessageCount(); i++) {
    	  freeMessage(packet.getMessage(i));  	
    	}
    	packetPool.push(packet);
    }

    public synchronized void freeValue(Value value) {

        // Called when we can free up a value for future use...

        if (value.type == XData.VECTOR)
            for (int i = 0; i < value.values.length; i++)
                freeValue(value.values[i]);
        valuePool.push(value);
    }

    public void imageLoaded() {

        // Called from XVM after an image has been loaded. XOS might need
        // to be reset or initialised with some data.

        Thread thread = XVM.currentThread();
        do {
            if (thread.state() == Thread.BLOCK_READ) {
                // Create an imput monitor for the thread....
                rescheduleBlockRead(thread);
            }
            thread = thread.next();
        } while (thread != XVM.currentThread());
    }
    
    public void init(String[] args, List<String> extensions){
    	
    }
    
    public void init(String[] args) {
        parseArgs(args);
        System.out.println("[ Starting XOS ]");
        parseXOSargs();
        initStandardChannels();
        initConnectionMonitor(port);
        initInternalClients();
        initMessageClients();
        initXVM(args);
        if(XVM.checkoutLicense()) 
          run();
        else
          System.exit(0);
    }

    public void initConnectionMonitor(int port) {

        // Start up a connection monitor for clients trying to connect to XMF.
        // If no port has been specified then do not start the monitor. The initial
        // value for port is -1 and will not have changed if no port is specified.

        if (port > 0) {
            connectionMonitor = new ConnectionMonitor(this, port);
            connectionMonitor.start();
        }
    }

    public void initInternalClients() {

        // Internal clients are specified using -internal command line args.
        // When XOS starts up, each internal client is loaded, instantiated
        // and initialised before starting the client as a Java thread.
        // To instantiate the client it must have a 0-arity constructor.
        // To run the client it must be an instance of java.lang.Thread.
        // Each client is specified as an arg -internal <CLASNAME>:<CLIENTNAME>.

        for (int i = 0; i < internalClientNames.size(); i++) {
            String clientSpec = (String) internalClientNames.elementAt(i);
            String className = clientSpecClassName(clientSpec);
            String clientName = clientSpecClientName(clientSpec);
            initInternalClient(className, clientName);
        }
    }

    public void initInternalClient(String className, String clientName) {
        try {
            Class clientClass = Class.forName(className);
            Constructor constructor = clientClass.getDeclaredConstructor(new Class[] {
                    InputStream.class, OutputStream.class });
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream();
            Object client = constructor.newInstance(new Object[] {
                    in, out });
            if (client instanceof java.lang.Thread) {
                newInternalClient(clientName, in, out);
                java.lang.Thread thread = (java.lang.Thread) client;
                debug("Starting " + clientName);
                thread.start();
            } else {
                System.out.println("Clients must be instances of java.lang.Thread: " + className);
                System.exit(0);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot find class named " + className);
            System.exit(0);
        } catch (InstantiationException ie) {
            System.out.println("Cannot instantiate " + className);
            System.exit(0);
        } catch (IllegalAccessException iae) {
            System.out.println(iae);
            System.exit(0);
        } catch (SecurityException e) {
            System.out.println(e);
            System.exit(0);
        } catch (NoSuchMethodException e) {
            System.out.println(e + ": Internal clients must implement a 2-place constructor(InputStream,OUtputStream)");
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
            System.exit(0);
        } catch (InvocationTargetException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public synchronized void initMessageClients() {

        // Internal clients are specified using -message command line args.
        // When XOS starts up, each message client is loaded, instantiated
        // and initialised. To instantiate the client it must have a 0-arity
        // constructor. To initialise the client it must implement MessageHandler.

        for (int i = 0; i < messageClientNames.size(); i++) {
            String clientSpec = (String) messageClientNames.elementAt(i);
            String className = clientSpecClassName(clientSpec);
            String clientName = clientSpecClientName(clientSpec);
            String[] args = clientSpecClientArgs(clientSpec);
            initMessageClient(className, clientName, args);
        }

        if (!waitingMessageClients.isEmpty())
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
    }

    public void initMessageClient(String className, String clientName, String[] args) {
        if (args.length == 0 || args[0].equals("create"))
            createNewMessageClient(className, clientName);
        else if (args[0].equals("wait")) {
            if (client(clientName) == null)
                waitForMessageClient(className, clientName);
        } else
            throw new Error("Illegal argument supplied to message client: " + args[0]);
    }

    public void initStandardChannels() {

        // Standard input (stdin) and standard output (stdout) are
        // always the input and output channels at the XVM level with
        // indices 0.

        XChannel stdin = new XChannel(System.in);
        newInputChannel(stdin);
        newOutputChannel(System.out);
    }

    public void initXVM(String[] args) {
        XVM.init(args);
        schedule(XVM.currentThread());
    }

    public XInputStream inputChannel(int index) {

        // Return the input channel at the given index or null
        // if the index is out of range.

        if (index >= 0 && index < inputChannels.size())
            return (XInputStream) inputChannels.elementAt(index);
        else
            return null;
    }

    public int inputChannel(String name) {

        // Maps a client name to the channel index of the client's
        // input channel. Returns -1 if no client exists with the
        // given name.

        StreamClient client = asyncClient(name);
        if (client != null)
            return inputIndex(client.in());
        else
            return -1;
    }

    public int inputIndex(XInputStream in) {

        // Maps an input stream to an index that is suitable for use
        // by XVM. Returns -1 if no input stream exists.

        return inputChannels.indexOf(in);
    }

    public void interrupt() {
        XVM.setInterruptFlag();
    }

    public boolean isConnected(String name) {

        // Returns true when there is a client with the supplied name.

        return client(name) != null;
    }

    public boolean isTokenChannel(int index) {
        return tokenInputChannel(index) != null;
    }

    public boolean isDataInputChannel(int index) {
        return dataInputStream(index) != null;
    }

    public boolean isDataOutputChannel(int index) {
        return dataOutputStream(index) != null;
    }

    public boolean isMessageClient(String name) {
        return messageClient(name) != null;
    }

    public int lineCount(int index) {
        TChannel in = tokenInputChannel(index);
        return in.lineCount();
    }

    public static void main(String[] args) {
        OperatingSystem XOS = new OperatingSystem();
        XOS.init(args);
    }

    public MessageClient messageClient(String name) {
        Client client = client(name);
        if (client instanceof MessageClient && client != null)
            return (MessageClient) client;
        else
            return null;
    }

    public MessageClient messageClient(StringBuffer name) {
        Client client = client(name);
        if (client instanceof MessageClient && client != null)
            return (MessageClient) client;
        else
            return null;
    }

    public int newDataInputChannel(int index) {

        // Creates and returns a new input channel that decodes
        // data written using the XData protocol.

        XInputStream in = inputChannel(index);
        if (in != null) {
            DChannel din = new DChannel(in);
            return newInputChannel(din);
        } else
            return -1;
    }

    public int newDataOutputChannel(int index) {

        // Creates and returns a new output channel that encodes data
        // using the XData protocol.

        OutputStream out = outputChannel(index);
        if (out != null) {
            DataOutputStream dout = new DataOutputStream(out);
            return newOutputChannel(dout);
        } else
            return -1;

    }

    public synchronized void newExternalClient(Socket socket) {

        // This should be called immediately on creation of the client.
        // It is reponsible for performing the naming protocol for a
        // new client. The client name is sent by the data provider. If the
        // name is unique then XOS sends ID_OK to the provider and
        // registers the client. Otherwise XOS sends ID_FAIL to the
        // provider and ignores the client.

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            String name = readExternalClientName(in);
            if (client(name) != null) {
                StreamClient client = asyncClient(name);
                client.write(ExternalClient.NAME_FAIL);
            } else {
                XChannel channel = new XChannel(in);
                ExternalClient client = new ExternalClient(name, this, channel, out);
                debug("Registering new external client named " + name);
                newInputChannel(client.in());
                newOutputChannel(client.out());
                client.write(ExternalClient.NAME_OK);
                client.flush();
                clients.addElement(client);
                scheduleClientThreads(name);
                notifyAll();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
            System.exit(0);
        }
    }

    public int newFileInputChannel(String fileName) {

        // Allocate a new input channel for the file. Return -1
        // if the file does not exist. Otherwise return the index
        // into the input channels table.

        try {
            File file = new File(fileName);
            if (file.exists()) {
                InputStream in = new FileInputStream(file);
                XChannel bin = new XChannel(in, true);
                return newInputChannel(bin);
            } else {
                return -1;
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }

    public int newFileOutputChannel(String fileName) {

        // allocate a new file output channel and return the index
        // into the channels table. Return -1 if a problem occurs.

        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            int index = newOutputChannel(fout);
            return index;
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }

    public int newGZipInputChannel(int index) {

        // Creates and returns a new input channel that deflates the
        // gzipped input provided by the supplied input channel.
        // Returns -1 if an error occurs.

        try {
            XInputStream in = inputChannel(index);
            if (in != null) {
                GZIPInputStream gin = new GZIPInputStream(in);
                XChannel xgin = new XChannel(gin, true);
                return newInputChannel(xgin);
            } else
                return -1;
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }

    public int newGZipOutputChannel(int index) {

        // Creates and returns a new output channel that compresses its
        // output and then passes it on to the supplied output channel.
        // Returns -1 if an error occurs.

        try {
            OutputStream out = outputChannel(index);
            if (out != null) {
                GZIPOutputStream gout = new GZIPOutputStream(out);
                return newOutputChannel(gout);
            } else
                return -1;
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }

    public int newInputChannel(InputStream in) {
        return newInputChannel(new XChannel(in));
    }

    public int newInputChannel(XInputStream in) {

        // Stores the supplied Java input stream and returns
        // an XVM input channel index for the stream.

        int inputChannel = inputChannels.size();
        inputChannels.addElement(in);
        return inputChannel;
    }

    public boolean newInternalClient(String name, PipedInputStream in, PipedOutputStream out) {

        // Create a new internal client that produces data on 'in' and
        // consumes data from 'out'.

        boolean newClient = false;
        if (client(name) == null)
            try {
                debug("Registering new internal client " + name);
                StreamClient client = new InternalClient(this, name, in, out);
                clients.addElement(client);
                newInputChannel(client.in());
                newOutputChannel(client.out());
                newClient = true;
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        return newClient;
    }

    public synchronized boolean newMessageClient(String name, MessageHandler handler) {

        // Creates and registers a new message client.

        debug("New Message Client " + name);

        if (client(name) == null) {
            debug("Registering message client " + name);
            handler.registerEventHandler(this);
            clients.addElement(new MessageClient(this, name, handler));
            removeWaitingMessageClient(name);
            return true;
        } else
            return false;
    }

    public int newOutputChannel(OutputStream out) {

        // Stores the supplied Java output stream and returns
        // an XVM output channel index for the stream.

        int outputChannel = outputChannels.size();
        outputChannels.addElement(out);
        return outputChannel;
    }

    public int newStringInputChannel(String string) {

        // Allocate a new string input channel and return. Note that
        // this is not the most efficient way of implementing this
        // type of input channel since the string already exists in
        // the VM heap and must be allocated as a Java string in
        // order to get this far. We don't really want to have XOS
        // have heap pointers so let it go for now...reimplement this
        // if it creates significant overhead.

        byte[] bytes = string.getBytes();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        return newInputChannel(new XChannel(bin, true));
    }

    public int newTokenChannel(TChannel in) {

        // Allocate a new index for the token input channel, save the
        // channel and return the index.

        int index = inputChannels.size();
        inputChannels.addElement(in);
        return index;
    }

    public int newTokenInputChannel(int index) {

        // Creates and returns a new token input channel (XTStream) based
        // on the supplied indexed input channel. The resulting channel can
        // be used to read bytes as normal, but also tokenizes the input
        // as required by the XOCL grammar. Returns -1 if an error occurs,
        // otherwise returns the index of the token input channel.

        XChannel in = (XChannel) inputChannel(index);
        if (in != null) {
            TChannel tin = new TChannel(in);
            return newTokenChannel(tin);
        } else
            return -1;
    }

    public int newSAXInputChannel(int index) {

        // Create and return a new XML input channel that serializes the
        // SAX events. The events are produces by a separate process that
        // reads XML from the supplied source and writes the encoded events
        // to a new output channel.

        try {
            InputStream xmlSource = inputChannel(index);
            if (xmlSource != null) {
                PipedOutputStream out = new PipedOutputStream();
                PipedInputStream in = new PipedInputStream(out);
                XMLReader reader = new XMLReader(xmlSource, out);
                reader.startProcess();
                return newInputChannel(in);
            } else
                return -1;
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }
    
    public int newURLInputChannel(String urlString) {
    	
    	final OperatingSystem os = this;
    	
    	// Set up authenticator...
    	
    	Authenticator.setDefault(new Authenticator() {

    		protected PasswordAuthentication getPasswordAuthentication() {
    			//String protocol = getRequestingProtocol();
    			String host = getRequestingHost(); // can be null;
    			InetAddress address = getRequestingSite(); // can be null;
    			//int port = getRequestingPort();
    			String prompt = getRequestingPrompt(); // realm or message, not documented that can be null
    			//String scheme = getRequestingScheme(); // not documented that can be null
    			
    			String hostString = host;
    			if (hostString == null && address != null)
    				address.getHostName();
    			if (hostString == null)
    				hostString = "";
    			
    			String promptString = prompt;
    			if (prompt == null)
    				promptString = "";
    			
    			// Get data from user password dialog...
    			try {
    				Object[] inputData = {hostString,promptString};
     				Object[] valueData = Client.getIOHandler().dialog("UserPassword",inputData);
     				if (valueData.length == 1 && valueData[0] instanceof UserPassword) {
     					UserPassword auth = (UserPassword)valueData[0];
     	    			if (auth != null)
     	    				return new PasswordAuthentication(auth.getUser(),
     	    				   auth.getPassword().toCharArray());
     	    			else
     	    				return null;
     				}
     				else
     					return null;
    			}
    			catch(XmfIOException bioe) {
    				System.out.println(bioe);
    				return null;
    			}
    		}
    	});

        // Allocate a new input channel for the URL. Return -1
        // if the URL does not exist. Otherwise return the index
        // into the input channels table.

        try {
            URL url = new URL(urlString);
            String authString = url.getUserInfo();
            if (authString != null) {
            	String[] authPair = authString.split(":");
            	this.setUserPassword(new UserPassword(authPair[0],authPair[1]));
            }
            InputStream in = url.openStream();
            XChannel bin = new XChannel(in, true);
            this.setUserPassword(null);
            return newInputChannel(bin);
        } catch (IOException ioe) {
            System.out.println(ioe);
            return -1;
        }
    }

    public int newZipOutputChannel(int index) {

        // Creates and returns a new output channel that compresses its
        // output and then passes it on to the supplied output channel.
        // Returns -1 if an error occurs.

        OutputStream out = outputChannel(index);
        if (out != null) {
            ZipOutputStream gout = new ZipOutputStream(out);
            return newOutputChannel(gout);
        } else
            return -1;
    }

    public int nextToken(int index) {

        // Reads the next token on the indexed token channel. This leaves the
        // token type and any errors in the token channel. The XOS client must
        // then use the appropriate XOS services to interrogate the channel.
        // Returns -1 if an error occurs. Returns 0 otherwise.

        debug("NextToken(" + index + ") called.");
        TChannel in = tokenInputChannel(index);
        if (in != null) {
            in.reset();
            in.nextToken();
            debug("NextToken(" + index + ") returned token = '" + in.token() + "' rawChars = '" + in.rawChars() + "'");
            return 0;
        } else
            return -1;
    }

    public OutputStream outputChannel(int index) {

        // Return the output stream at the given index or null
        // if the index is out of range.

        if (index >= 0 && index < outputChannels.size())
            return (OutputStream) outputChannels.elementAt(index);
        else
            return null;
    }

    public int outputChannel(String name) {

        // Maps a client name to the channel index of the client's
        // output channel. Returns -1 if no client exists with the
        // given name.

        StreamClient client = asyncClient(name);
        if (client != null)
            return outputIndex(client.out());
        else
            return -1;
    }

    public int outputIndex(OutputStream out) {

        // Maps an output stream to an index that is suitable for use
        // by XVM. Returns -1 if no output stream exists.

        return outputChannels.indexOf(out);
    }

    public void parseArgs(String[] args) {

        // Set up the command line arguments in the appropriate string arrays.

        XOSargs = ArgParser.parseArgs(XOSargSpecs, args);
    }

    public void parseXOSargs() {
        int index = 0;
        while (index < XOSargs.length) {
            String arg = XOSargs[index++];
            if (arg.equals("-port"))
                port = Integer.parseInt(XOSargs[index++]);
            else if (arg.equals("-debug"))
                debug = true;
            else if (arg.equals("-internal"))
                internalClientNames.addElement(XOSargs[index++]);
            else if (arg.equals("-message"))
                messageClientNames.addElement(XOSargs[index++]);
            else
                index++;
        }
    }

    public int peek(int index) {

        // Peek and the next input byte and return it. This will block if the
        // next byte is not currently available.

        debug("Peek(" + index + ")");
        XInputStream in = inputChannel(index);
        if (in != null)
            return in.peek();
        else
            throw new Error("Illegal input channel index: " + index);
    }

    public int posValue(int index) {
        TChannel in = tokenInputChannel(index);
        return in.posValue();
    }

    public void prepareThread() {

        // The current XVM thread has been selected. If it is currently active
        // then just return. Otherwise it must be pending some input. Determine
        // the type of the input, read the input and push the input on the XVM
        // stack.

        Thread thread = XVM.currentThread();
        int state = thread.state();

        switch (state) {
        case Thread.ACTIVE:
            debug("Thread " + thread + " has been selected as it is active.");
            break;
        case Thread.BLOCK_READ:
            debug("Thread " + thread + " has been selected as it is ready for input.");
            thread.setState(Thread.ACTIVE);

            // The block might be on a message client or a stream client...

            if (thread.inputChannel() == -1)
                XVM.readMessageClient(thread.client());
            else {
                int index = thread.inputChannel();
                XVM.readReadyChannel(Machine.mkInputChannel(index));
            }
            break;
        case Thread.BLOCK_ACCEPT:
            debug("Thread " + thread + " has been selected as it is ready to connect.");
            XVM.pushStack(Machine.trueValue);
            break;
        default:
            System.out.println("Unknown state for thread preparation: " + state);
            System.exit(0);
        }
    }

    public synchronized void printDebugState() {
        debug("scheduledThreads = " + scheduledThreads);
        debug("inputMonitors = " + inputMonitors);
        debug("messageMonitors = " + messageMonitors);
        debug("clientThreads = " + clientThreads);
        debug("inputChannels = " + inputChannels);
        debug("outputChannels = " + outputChannels);
        debug("clients = " + clients);
        debug("threads = " + XVM.currentThread().allToString());
        // debug("free messages = " + messagePool);
        // debug("free values = " + valuePool);
    }

    public void raiseEvent(String name, Message message) {
        MessageClient client = messageClient(name);
        if (client != null) {
            client.queueMessage(message);
        }
    }

    public String rawChars(int index) {

        // Returns the rawChars in the token channel at the given index
        // or returns null if no such token exists.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.rawChars();
        else
            return null;
    }

    public int read(int index) {

        // Read the next byte from the input channel. This will block if
        // the input is not currently available. It assumes the appropriate
        // checks have been made prior to calling this method if blocking
        // is not permitted.

        debug("Read(" + index + ")");
        XChannel in = (XChannel) inputChannel(index);
        return in.read();
    }

    public Message readMessage(String name) {

        // A message is available at this point. Get the message client
        // and pop the next message from the queue. Returns null if no
        // message is available.

        MessageClient client = messageClient(name);
        if (client != null)
            return client.readMessage();
        else
            return null;
    }

    public boolean ready(int index) {

        // Returns true when the indexed input channel is ready for input.

        debug("Ready(" + index + ")");
        XInputStream in = inputChannel(index);
        if (in != null) {
            return in.ready();
        } else
            throw new Error("XOS.ready: illegal index: " + index);
    }

    public boolean ready(String name) {

        // Supply the name of the client.

        debug("Ready(" + name + ")");
        Client client = client(name);
        if (client != null)
            return client.ready();
        else
            return false;
    }

    public String readExternalClientName(InputStream in) {

        // Reads the 0-terminated name string from the client. This must be
        // the first data sent from the client. Blocks until the name is sent.

        debug("Reading external client name");
        String name = "";
        try {
            int c = in.read();
            while (c != 0) {
                name = name + (char) c;
                c = in.read();
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(0);
        }
        return name;
    }

    public void rebindStandardInput(int index) {

        // Called when we want to change where the standard input of Java
        // comes from.

        System.setIn(inputChannel(index));
    }

    public void rebindStandardOutput(int index) {

        // Called when we want to change where the standard output of Java
        // goes to.

        System.setOut(new PrintStream(outputChannel(index), true));
    }

    public synchronized void removeWaitingMessageClient(String name) {

        // Called when a message client is created. This can occur externally to
        // XOS and XOS may be waiting for the client to connect before starting up.
        // If this is the case then wake up the XOS initialization thread.

        if (!waitingMessageClients.isEmpty()) {
            waitingMessageClients.remove(name);
            if (waitingMessageClients.isEmpty())
                notifyAll();
        }
    }

    public void resetToInitialState(int index) {

        // Used to reset a token channel.

        TChannel tin = tokenInputChannel(index);
        tin.resetToInitialState();
    }

    public void rescheduleBlockRead(Thread thread) {
        int inch = thread.inputChannel();
        String client = thread.client();
        if (inch != -1) {
            XInputStream in = inputChannel(thread.inputChannel());
            if (in != null)
                startInputMonitor(in, thread);
        } else
            startMessageMonitor(client, thread);
    }

    public synchronized void rescheduleThread(Thread thread) {

        // The thread has just completed. The state of the thread tells us
        // how to reschedule the thread. If it is active then the thread is
        // added to the end of the scheduled threads queue. If the thread is
        // blocking then the thread is monitored.

        switch (thread.state()) {
        case Thread.ACTIVE:
            debug("Thread " + thread + " is being rescheduled.");
            XVM.saveCurrentThread();
            schedule(thread);
            break;
        case Thread.BLOCK_READ:
            debug("Thread " + thread + " is blocking on input.");
            XVM.saveCurrentThread();
            rescheduleBlockRead(thread);
            break;
        case Thread.BLOCK_ACCEPT:
            debug("Thread " + thread + " is blocking on connection.");
            XVM.saveCurrentThread();
            clientThreads.add(thread);
            break;
        case Thread.SLEEPING:
            debug("Thread " + thread + " is sleeping.");
            XVM.saveCurrentThread();
            break;
        default:
            System.out.println("Thread " + thread + " unknown state: " + thread.state());
        }

        debug("Returning from reschedule thread");
    }

    public void run() {

        // The main XOS loop. If there are no XVM threads left then no
        // processing can take place and we shut down XOS. Otherwise,
        // if there are no scheduled threads then we wait for a
        // monitor thread to notify XOS. Otherwise, run the next
        // scheduled thread.

        try {
            while (!XVM.terminated()) {
                printDebugState();
                waitForMonitor();
                if (!scheduledThreads.isEmpty())
                    runReadyThread();
            }
            debug("XVM has terminated normally.");
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            shutDown(0);
        }
    }

    public synchronized void runReadyThread() {

        // Remove the next scheduled thread, update the machine, prepare the
        // thread by providing any information that the thread was blocking on
        // and then run the thread.

        Thread thread = scheduledThreads.next();
        debug("RunReadyThread(" + thread + ")");
        XVM.setThread(thread);
        prepareThread();
        runThread();
        notifyAll();
    }

    public void runThread() {

        // Perform the selected thread, remove the thread if it becomes
        // terminated and shut the system down if all threads become terminated.

        debug("Loading " + XVM.currentThread() + " onto the machine and starting run...");

        XVM.perform();

        debug("Thread " + XVM.currentThread() + " halted.");

        // If the thread has terminated then remove it...

        if (XVM.terminatedThread()) {
            debug("Killing " + XVM.currentThread());
            XVM.killCurrentThread();
        } else
            rescheduleThread(XVM.currentThread());
    }

    public synchronized void schedule(Thread thread) {
        debug("Scheduling thread " + thread);
        scheduledThreads.insert(thread);
    }

    public synchronized void scheduleClientThreads(String name) {

        // A client with the supplied name has just connected. Any threads
        // in clientThreads that are currently blocking on this client can
        // be re-scheduled.

        debug("scheduleClientThreads(" + name + ")");
        Vector threads = (Vector) clientThreads.clone();
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = (Thread) threads.elementAt(i);
            if (thread.client().equals(name)) {
                schedule(thread);
                clientThreads.removeElement(thread);
            }
        }
        notifyAll();
    }

    public void shutDown(int result) {

        // Called when XOS has no further processing to do.

        System.out.println("[ Terminating XOS ]");

        closeAll();

        System.exit(result);

    }

    public void startInputMonitor(XInputStream in, Thread thread) {

        // A read on the input stream is blocking. Start an input monitor
        // by creating one or finding an existing one. The monitor will
        // schedule the thread i=when some input is detected.

        debug("StartInputMonitor(" + in + "," + thread + ")");
        StreamMonitor monitor = null;
        for (int i = 0; i < inputMonitors.size() && monitor == null; i++) {
            StreamMonitor m = (StreamMonitor) inputMonitors.elementAt(i);
            if (m.in() == in)
                monitor = m;
        }
        if (monitor == null) {
            monitor = new StreamMonitor(in, thread, this);
            addInputMonitor(monitor);
            monitor.start();
        } else
            monitor.setThread(thread);
    }

    public void startMessageMonitor(MessageClient client, Thread thread) {

        // A read on the client has blocked. Start a message monitor that
        // will schedule the thread when a message becomes available.

        debug("StartMessageMonitor(" + client + "," + thread + ")");
        MessageMonitor monitor = null;
        for (int i = 0; i < messageMonitors.size() && monitor == null; i++) {
            MessageMonitor m = (MessageMonitor) messageMonitors.elementAt(i);
            if (m.client() == client)
                monitor = m;
        }
        if (monitor == null) {
            monitor = new MessageMonitor(client, thread, this);
            addMessageMonitor(monitor);
            monitor.start();
        } else
            monitor.setThread(thread);
    }

    public void startMessageMonitor(String name, Thread thread) {
        MessageClient client = messageClient(name);
        if (client != null)
            startMessageMonitor(client, thread);
        else
            throw new Error("Cannot find message client for " + name);
    }

    public void startString(int index, int length) {

        // A client wants to write a string as a header and some following
        // chars. This writes the header.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.startString(length);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal index for data output stream: " + index);
    }

    public String textTo(int index, int position) {

        // Returns a string being the text consumed by the indexed token channel
        // up to the supplied position. If an error occurs then returns null.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.textTo(position);
        else
            return null;
    }

    public boolean threadReady(Thread thread) {

        // Return true if the thread is not blocking or sleeping...

        debug("ThreadyReady( " + thread + ")");
        switch (thread.state()) {
        case Thread.ACTIVE:
            return true;
        case Thread.BLOCK_READ:
            return ready(thread.inputChannel());
        case Thread.BLOCK_ACCEPT:
            return client(thread.client()) != null;
        default:
            return false;
        }
    }

    public String token(int index) {

        // Returns the token in the token channel at the given index
        // or returns null if no such token exists.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.token();
        else
            return null;
    }

    public TChannel tokenInputChannel(int index) {

        // Returns the token input channel at the given index or null
        // if no such channel exists or the channel at the given index
        // is not a token input channel.

        TChannel in = tokenChannel(index);
        return in;
    }

    public TChannel tokenChannel(int index) {

        // Find and return the token input channel at the supplied index.
        // If no token channel exists then return null.

        if (index >= 0 && index < inputChannels.size()) {
            Object in = inputChannels.elementAt(index);
            if (in instanceof TChannel)
                return (TChannel) in;
            else
                return null;
        } else
            return null;
    }

    public boolean tokenError(int index) {

        // Returns the error in the token channel at the given index
        // or returns true if no such token exists.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.error();
        else
            return true;
    }

    public String tokenErrorMessage(int index) {

        // Returns the error message in the token channel at the given index
        // or returns null if no such token exists.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.errorMessage();
        else
            return null;
    }

    public int tokenType(int index) {

        // Returns the token typr in the token channel at the given index.
        // Returns -1 if not such token exists.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.type();
        else
            return -1;
    }

    public int tokenValue(int index) {

        // Returns the token value in the token channel at the given index.

        TChannel in = tokenInputChannel(index);
        if (in != null)
            return in.value();
        else
            return -1;
    }

    public static void usage() {
        System.out.println("java OperatingSystem PORT");
    }

    public void waitForMessageClient(String className, String clientName) {

        // Called when a command line argument wait has been supplied for
        // a message client. This defines that the message client will attach
        // itself to XOS directly and that startup should wait until all
        // such message clients have attached.

        waitingMessageClients.addElement(clientName);
        debug("Waiting for message client " + clientName);
    }

    public synchronized void waitForMonitor() {
        if (scheduledThreads.isEmpty()) {
            notifyAll();
            try {
                debug("XOS waiting for monitor notification.");
                wait();
                debug("XOS has been woken up.");
                notifyAll();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public synchronized void write(int index, int c) {

        // Write the supplied byte to the supplied output channel.

        OutputStream out = outputChannel(index);
        if (out != null)
            try {
                // debug("XOS.write('" + (char) c + "') on " + out);
                out.write(c);
                if (c == '\n')
                    out.flush();
                // debug("XOS.written('" + (char) c + "')");
            } catch (IOException ioe) {
                System.out.println(ioe);
            }
        else
            throw new Error("Illegal output channel index " + index);
    }

    public void writeBool(int index, boolean b) {

        // Write the boolean to the supplied data output stream.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.writeBool(b);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal data output channel index " + index);
    }

    public void writeByte(int index, int b) {

        // Write the byte to the supplied data output stream.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.writeByte(b);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal data output channel index " + index);
    }

    public void writeInt(int index, int i) {

        // Write the int to the supplied data output stream.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.writeInt(i);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal data output channel index " + index);
    }

    public void writeFloat(int index, float f) {

        // Write the float to the supplied data output stream.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.writeFloat(f);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal data output channel index " + index);
    }

    public void writeMessage(String name, Message message) {
        MessageClient client = messageClient(name);
        if (client != null)
            client.sendMessage(message);
        else
            throw new Error("Unknown client " + name);
    }

    public void writeString(int index, String s) {

        // Write the string to the supplied data output stream.

        DataOutputStream out = dataOutputStream(index);
        if (out != null)
            try {
                out.writeString(s);
            } catch (IOException ioe) {
                System.out.println(ioe);
                System.exit(0);
            }
        else
            throw new Error("Illegal data output channel index " + index);
    }
    
    protected UserPassword getUserPassword() {
       return auth;
    }
    
    protected void setUserPassword(UserPassword auth) {
    	this.auth = auth;
    }

}