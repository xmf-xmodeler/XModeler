package com.ceteva.mosaic;

import java.io.PrintWriter;

import org.eclipse.ui.about.ISystemSummarySection;

public class SystemInfo implements ISystemSummarySection {

	public void write(PrintWriter writer) {
	  writer.write("XMF-Mosaic");	
	}
}
