package XOS;

import Engine.Thread;

public class StreamMonitor extends java.lang.Thread {

    // Monitors an input stream that is currently causing a thread to block.
    // If input becomes available on the stream then the thread is scheduled.

    private XInputStream    in;

    private Thread          thread;

    private OperatingSystem XOS;

    public StreamMonitor(XInputStream in, Thread thread, OperatingSystem XOS) {
        this.in = in;
        this.thread = thread;
        this.XOS = XOS;
    }

    public XInputStream in() {
        return in;
    }

    public synchronized void run() {
        while (true) {
            XOS.debug("Input monitor " + this + " about to peek at " + in);
            int c = in.peek();
            XOS.debug("Input monitor " + this + " peeked '" + (char) c + "'");

            if (thread.state() != Thread.DEAD) {
                if (thread.state() != Thread.BLOCK_READ) {
                    System.out.println("ERROR in monitor " + this + " state has changed to: " + thread.state());
                    System.exit(0);
                }

                synchronized (XOS) {
                    XOS.schedule(thread);
                    XOS.debug("Input monitor scheduled " + thread);
                    XOS.notifyAll();
                }
            }
            try {

                // Enter a waiting state that is re-awoken by XOS setting the
                // thread for this input stream.

                thread = null;
                notifyAll();
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
                e.printStackTrace();
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
            return "InputMonitor(" + in + "INACTIVE)";
        else
            return "InputMonitor(" + in + "," + thread + ",ACTIVE)";
    }

}