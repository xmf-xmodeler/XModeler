package com.ceteva.text.highlighting;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

import com.ceteva.text.TextPlugin;

public class ScannerTokens {
	
  private static IToken redToken = new Token(new TextAttribute(TextPlugin.RED));
  private static IToken greenToken = new Token(new TextAttribute(TextPlugin.GREEN));
  private static IToken blueToken = new Token(new TextAttribute(TextPlugin.BLUE));
  private static IToken defaultToken = new Token(new TextAttribute(TextPlugin.BLACK));

  public static IToken getToken(String color) {
    if(color.equals("red"))
      return redToken;
    else if(color.equals("green"))
      return greenToken;
    return blueToken;
  }
  
  public static Color getColour(String color) {
  	if(color.equals("red"))
      return TextPlugin.RED;
    else if(color.equals("green"))
      return TextPlugin.GREEN;
    return TextPlugin.BLUE;
  }
  
  public static IToken getDefaultToken() {
  	return defaultToken;
  }
}