package com.ceteva.console;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

import com.ceteva.console.views.ConsoleView;

public class ConsoleClient extends Thread {

	ConsoleView view = null;
	BufferedReader in;
	PrintStream out;
	StringBuffer queuedInput = new StringBuffer();

	public ConsoleClient(InputStream in, OutputStream out) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = new PrintStream(new BufferedOutputStream(out));
	}

	public void run() {
		char[] buffer = new char[1000];
		while (true) {
			try {
				int size = in.read(buffer);
				if (size > 0)
					sendInput(new String(buffer).substring(0, size));
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

	public void debug(String message) {

		System.err.println(java.lang.Thread.currentThread() + ": " + message);
		System.err.flush();

	}

	public boolean tryConnecting() {
		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IWorkbenchPage page = consolePlugin.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			debug("Active workbench was not found");
			return false;
		}
		view = (ConsoleView) page.findView("com.ceteva.console.view");
		if (view == null) {
			debug("Console View was not found with id = com.ceteva.console.view");
			return false;
		}
		view.setOutput(out);
		view.processInput(queuedInput.toString());
		return true;
	}

	public void sendInput(final String input) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				if (view != null)
					view.processInput(input);
				else if (tryConnecting())
					view.processInput(input);
				else
					queueInput(input);
			}
		});
	}

	public void queueInput(String input) {
		queuedInput.append(input);
	}
}