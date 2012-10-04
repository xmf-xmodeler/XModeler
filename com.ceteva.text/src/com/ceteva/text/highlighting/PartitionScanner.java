package com.ceteva.text.highlighting;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class PartitionScanner extends RuleBasedPartitionScanner {
	
  private IPredicateRule[] a = new IPredicateRule[1];
  private ArrayList rules;
 
  public PartitionScanner() {
  	super();
  	clearRules();
  }
  
  public void addRule(String id,String start,String end) {
  	if(start.length()>0 && end.length()>0) {
  	  IToken token = new Token(id);
  	  MultiLineRule rule = new MultiLineRule(start,end,token,(char)0,true);
  	  rules.add(rule);
  	  updateRules();
  	}
  }
  
  public void updateRules() {
	setPredicateRules((IPredicateRule[])rules.toArray(a));	
  }
  
  public void clearRules() {
    rules = new ArrayList();
    // addRule(PartitionTypes.MULTI_LINE,"/*","*/");
  }
}