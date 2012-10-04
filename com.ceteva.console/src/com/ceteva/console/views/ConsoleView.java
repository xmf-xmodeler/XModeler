package com.ceteva.console.views;

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ceteva.console.ConsolePlugin;
import com.ceteva.console.preferences.IPreferenceConstants;
import com.ceteva.consoleInterface.EscapeHandler;

public class ConsoleView extends ViewPart implements IPropertyChangeListener, IPartListener2 {

    StyledText    text            = null;
    Document      document        = null;
    History       history         = new History();
    int           inputStart      = 0;
    Font          textFont        = null;
    Color         backgroundColor = null;
    Color         foregroundColor = null;
    int           waterMark       = 1000;
    PrintStream   out             = null;
    static EscapeHandler escape   = null;
    
    // Used to synchronise the adding of text to the document with the flushing
    // of waterline
    
    private Object overflowLock = new Object();
    private FlushWaterline waterlineJob;
    
    private class FlushWaterline extends Job {
    	
    	StyledTextContent styledTextContent;

    	FlushWaterline() {
    		super("Console Flush Waterline");
    		styledTextContent = text.getContent();
    	}
    	
		protected IStatus run(IProgressMonitor monitor) {
		    synchronized (overflowLock) {
				Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (text != null) {
                            int charCount = styledTextContent.getCharCount();
                            if (charCount > waterMark) {
                              int difference = charCount - waterMark;
                              if(difference > 0) {
                            	styledTextContent.replaceTextRange(0, difference, "");
                                inputStart = inputStart - difference;
                                goToEnd();
                              }
                            }
                        }  
				    }
				});
		    }
			return Status.OK_STATUS;
		}
    }
    

 	public ConsoleView() {
	    registerAsListener();
	}
    
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        text = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL);
        text.setWordWrap(true);
        addVerifyListener(text);
        
        // add the job to reduce the text size as it flows over
        // the waterline
        
		waterlineJob = new FlushWaterline();
		waterlineJob.setSystem(true);
		waterlineJob.setPriority(Job.INTERACTIVE);
        
        // get preference details and monitor for future changes
        
        registerWithPreferences();
        getPreferences();
    }

    public void appendText(String string) {
    	synchronized (overflowLock) {
          if (text != null)
        	ConsolePlugin.writeToFile(string);
            text.append(string);
    	}
    }

    public void getPreferences() {
        if (textFont != null)
            textFont.dispose();
        if (backgroundColor != null)
            backgroundColor.dispose();
        if (foregroundColor != null)
            backgroundColor.dispose();
        IPreferenceStore ipreferences = ConsolePlugin.getDefault().getPreferenceStore();
        RGB fontColour = PreferenceConverter.getColor(ipreferences, IPreferenceConstants.CONSOLE_FONT_COLOUR);
        RGB backgroundColour = PreferenceConverter.getColor(ipreferences, IPreferenceConstants.CONSOLE_BACKGROUND);
        FontData font = PreferenceConverter.getFontData(ipreferences, IPreferenceConstants.CONSOLE_FONT);
        text.setBackground(backgroundColor = new Color(Display.getDefault(), backgroundColour));
        text.setForeground(foregroundColor = new Color(Display.getDefault(), fontColour));
        text.setFont(textFont = new Font(Display.getCurrent(), font));
        this.waterMark = ipreferences.getInt(IPreferenceConstants.LINE_LIMIT);
        history.setSize(ipreferences.getInt(IPreferenceConstants.COMMAND_HISTORY_LIMIT));
        
    }

    public String pushToHistory(StyledText text) {
        StyledTextContent content = text.getContent();
        String command = content.getTextRange(inputStart, content.getCharCount() - inputStart - 1);
        history.add(command);
        return command;
    }

    public String recallFromHistoryForward() {
        return history.getPrevious();
    }

    public String recallFromHistoryBackward() {
        return history.getNext();
    }

    public void addCommand(StyledText text, String command) {
        int length = text.getCharCount() - inputStart;
        text.replaceTextRange(inputStart, length, command);
        goToEnd();
    }

    public void addVerifyListener(final StyledText text) {
        text.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                int start = e.start;
                int end = e.end;
                if (start < inputStart || end < inputStart) {
                    goToEnd();
                    appendText(e.text);
                    goToEnd();
                    e.doit = false;
                } else
                    e.doit = true;
            }
        });
        text.addVerifyKeyListener(new VerifyKeyListener() {
            public void verifyKey(VerifyEvent e) {
                if (e.keyCode == SWT.ESC) {
                    if(escape != null) {
                      escape.interrupt();
                    }
                    e.doit = false;
                } else if (e.keyCode == SWT.ARROW_UP) {
                    String command = recallFromHistoryForward();
                    if (command != "")
                        addCommand(text, command);
                    e.doit = false;
                } else if (e.keyCode == SWT.ARROW_DOWN) {
                    String command = recallFromHistoryBackward();
                    if (command != "")
                        addCommand(text, command);
                    e.doit = false;
                } else if (e.keyCode == SWT.CR) {
                    goToEnd();
                    appendText("\n");
                    // waterlineJob.schedule();
                    goToEnd();
                    e.doit = false;
                    String output = pushToHistory(text);
                    if (out != null) {
              		  out.print(output + "\r");
              		  out.flush();
                    }
                    goToEnd();
                    inputStart = text.getContent().getCharCount();
                }
            }
        });
    }

    public void registerWithPreferences() {
        IPreferenceStore preference = ConsolePlugin.getDefault().getPreferenceStore();
        preference.addPropertyChangeListener(this);
    }

    public void setOutput(PrintStream out) {
        this.out = out;
    }

    public void processInput(String input) {
    	appendText(input);
    	waterlineJob.schedule(250);
  		goToEnd();
  		inputStart = text.getContent().getCharCount();
    }

    public void propertyChange(PropertyChangeEvent event) {
        getPreferences();
    }

    public void setFocus() {
        goToEnd();
        text.setFocus();
    }

    public void goToEnd() {
    	int end = text.getCharCount();
        text.setSelection(end,end);
    }

    public void dispose() {
        super.dispose();
        textFont.dispose();
        backgroundColor.dispose();
        foregroundColor.dispose();
    }
    
    public static void setEscapeHandler(EscapeHandler handler) {
        escape = handler;
    }

 	public void registerAsListener() {
 	  IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 	  page.addPartListener(this);
 	}

   public void partActivated(IWorkbenchPartReference partRef) {
   }

   public void partBroughtToTop(IWorkbenchPartReference partRef) {
   }

   public void partClosed(IWorkbenchPartReference partRef) {
   }

   public void partDeactivated(IWorkbenchPartReference partRef) {
   }

   public void partOpened(IWorkbenchPartReference partRef) {
   }

   public void partHidden(IWorkbenchPartReference partRef) {
   }

   public void partVisible(IWorkbenchPartReference partRef) {
   }

   public void partInputChanged(IWorkbenchPartReference partRef) {
   }

}