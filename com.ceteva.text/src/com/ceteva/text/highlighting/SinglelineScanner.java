package com.ceteva.text.highlighting;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;


public class SinglelineScanner extends RuleBasedScanner {

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
  private ArrayList rules;
  
  public SinglelineScanner() {
	setDefaultReturnToken(ScannerTokens.getDefaultToken());
	clearRules(); 
  }
  
  public void addRule(String word,String color) {
	if(word.length()>0) {
	  IToken token = ScannerTokens.getToken(color);
	  EvaluateWord rule = new EvaluateWord(new WordDector(word),token,word);
	  rules.add(rule);
	  updateRules();
	}
  }
  
  public void updateRules() {
	setRules((IRule[])rules.toArray(a));	
  }	
  
  public void clearRules() {
    rules = new ArrayList();
    // addRule("//","green");  // for some reason it needs at least one rule! 
    rules.add(new EndOfLineRule("//",ScannerTokens.getToken("red")));
    updateRules();
  }		
}
