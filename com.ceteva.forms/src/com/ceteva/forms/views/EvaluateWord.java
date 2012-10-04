package com.ceteva.forms.views;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

public class EvaluateWord implements IRule {

	private String fWord;
	private final IToken fToken;
	private final IWordDetector fDetector;
	private StringBuffer fBuffer= new StringBuffer();

	public EvaluateWord(IWordDetector detector, IToken token, String word) {
		fDetector = detector;
		fToken = token;
		fWord = word;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		scanner.unread();
	 	int c = scanner.read();
	 	if(!isChar(c)) {
	 	  int count = 1;
		  c = scanner.read();
		  if (fDetector.isWordStart((char) c)) {
		    fBuffer.setLength(0);
		    do {
              fBuffer.append((char) c);
			  c = scanner.read();
			  ++count;
			} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char)c));
		    scanner.unread();
		    if(fBuffer.toString().equals(fWord) && !isChar(c)) 
		  	  return fToken;
		    unwind(scanner);
		    return Token.UNDEFINED;
		  }
		  scanner.unread();
		  return Token.UNDEFINED;
	 	}
	 	return Token.UNDEFINED;
	}
	
	protected void unwind(ICharacterScanner scanner) {
		for (int i= fBuffer.length() - 1; i >= 0; i--)
			scanner.unread();
	}
	
	public boolean isChar(int c) {
		if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
		  return true;
		return false;
	}
	
}
