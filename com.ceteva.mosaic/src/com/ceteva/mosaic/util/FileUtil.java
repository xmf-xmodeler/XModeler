/*
 * @(#)FileUtil.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.ceteva.mosaic.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

/**
 * This is utility class to load a file.
 *
 * @author Samuel Chen
 * @version $Revision: 1.2 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class FileUtil {

  private InputStream is;

  public String getRuntimeDirectory() {
    URL installUrl = null;
    installUrl = Platform.getInstallLocation().getURL();
    return installUrl.getFile().toString();
  }
  
  
  /**
   * @param filename the file name
   */
  public FileUtil(String filename) throws FileNotFoundException {
  	String directory = getRuntimeDirectory();
  	is = new FileInputStream(directory + "\\" + filename);
  }

  /**
   * @param is InputStream to be read.
   */
  public FileUtil(InputStream is) {
    this.is = is;
  }

  /**
   * The method loads the file (properties file) into a property names List object
   * and a Properties object.
   * Both objects have to be initialized.
   * This is used when one wants to keep the properties names in order.
   * @param names the properties name list.
   * @param prop the Properties itself.
   */
  public void read(List names, Properties prop) throws IOException {

    if (is == null) {
      throw new IOException("There is nothing to read from ...");
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    String name;
    String value;
    while ( (line=reader.readLine()) != null ) {      
      if ((line.trim()).indexOf('#') == 0 || (line.trim()).indexOf('!') == 0)
        continue;
      int index = line.indexOf('=');
      if (index > 0) {
        name = line.substring(0, index).trim();
        value = line.substring(++index).trim();
      } else {
        name = line.trim();
        value = "";
      }
      names.add(name);
      prop.setProperty(name, value);
    }

  }
  
}


