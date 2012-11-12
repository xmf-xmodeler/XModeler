package IO;

import java.io.*;

public class IOThread extends Thread {

	private InputStream in;

	private OutputStream out;

	private final static boolean debug = false;

	public IOThread(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	public static void debug(String s) {
		if (debug) {
			System.err.println(s);
			System.err.flush();
		}
	}

	public void run() {
		while (true) {
			try {
				IOThread.debug("About to read on " + in);
				int c = in.read();
				IOThread.debug("read '" + (char) c + "'");
				IOThread.debug("About to write on " + out);
				out.write(c);
				IOThread.debug("written. About to flush");
				out.flush();
				IOThread.debug("flushed.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}