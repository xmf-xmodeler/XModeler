package com.ceteva.forms.views;

import java.util.ArrayList;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import com.ceteva.forms.FormsPlugin;

class Scanner extends RuleBasedScanner {

  class WordDector implements IWordDetector {
  	
  	private String word = "";
  	
  	public WordDector(String word) {
  	  this.word = word;
  	}
  	
  	public boolean isWordStart(char c) {
  	  if(word.length()>0)
  	    return c == word.charAt(0);
  	  return false;	
  	}
  	
  	public boolean isWordPart(char c) {
  	  for(int i=0;i<word.length();i++) {
  	  	if(c == word.charAt(i))
  	  	  return true;	
  	  }
  	  return false;
  	}
  }

  private IRule[] a = new IRule[1];
  private ArrayList rules = new ArrayList();
  
  private static IToken redToken = new Token(new TextAttribute(FormsPlugin.RED));
  private static IToken greenToken = new Token(new TextAttribute(FormsPlugin.GREEN));
  private static IToken blueToken = new Token(new TextAttribute(FormsPlugin.BLUE));
  private static IToken defaultToken = new Token(new TextAttribute(FormsPlugin.BLACK));
  
  public Scanner() {
	setDefaultReturnToken(defaultToken);
    addRule("//","green"); // for some reason it needs at least one rule!
  }
  
  public void addRule(String word,String color) {
	if(word.length()>0) {
	  IToken token = getToken(color);
	  EvaluateWord rule = new EvaluateWord(new WordDector(word),token,word);
	  rules.add(rule);
	  updateRules();
	}
  }
  
  public IToken getToken(String color) {
    if(color.equals("red"))
      return redToken;
    else if(color.equals("green"))
      return greenToken;
    return blueToken;
  }	
  
  public void updateRules() {
	setRules((IRule[])rules.toArray(a));	
  }	
  
  public void clearRules() {
    rules = new ArrayList();
	addRule("//","green"); 
    updateRules();
  }		
}
