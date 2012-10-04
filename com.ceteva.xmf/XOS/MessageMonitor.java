package XOS;

import Engine.Thread;

public class MessageMonitor extends java.lang.Thread {

    // Monitors a message queue that is currently causing a thread to block.
    // If input becomes available on the queue then the thread is scheduled.

    private MessageClient    client;

    private Thread          thread;

    private OperatingSystem XOS;

    public MessageMonitor(MessageClient client, Thread thread, OperatingSystem XOS) {
        this.client = client;
        this.thread = thread;
        this.XOS = XOS;
    }

    public MessageClient client() {
        return client;
    }

    public synchronized void run() {
        while (true) {
            XOS.debug("Message monitor " + this + " about to peek at " + client);
            synchronized (client) {
                if (client.messages().isEmpty())
                    try {
                        client.wait();
                    } catch (InterruptedException e1) {
                        System.out.println(e1);
                    }
            }
            if (thread.state() != Thread.BLOCK_READ) {
                System.out.println("ERROR in monitor state has changed!");
                System.exit(0);
            }
            synchronized (XOS) {
                XOS.schedule(thread);
                XOS.debug("Input monitor scheduled " + thread);
                XOS.notifyAll();
            }
            try {

                // Enter a waiting state that is re-awoken by XOS setting the
                // thread for this input stream.

                thread = null;
                notifyAll();
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public synchronized void setThread(Thread thread) {
        this.thread = thread;
        notifyAll();
    }

    public Thread thread() {
        return thread;
    }

    public String toString() {
        if (thread == null)
            return "MessageMonitor(" + client + "INACTIVE)";
        else
            return "MessageMonitor(" + client + "," + thread + ",ACTIVE)";
    }

}