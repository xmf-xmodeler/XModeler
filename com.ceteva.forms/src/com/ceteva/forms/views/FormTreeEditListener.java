package com.ceteva.forms.views;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.ceteva.forms.FormsPlugin;

public class FormTreeEditListener {
   
   static final TreeItem [] lastItem = new TreeItem [1];

   public static void addListener(final Tree tree,final Vector nonEditableNodes,final FormTreeWrapper formtree) {
    
	  tree.addListener (SWT.Selection, new Listener () {
	         public void handleEvent (Event e) {
	        	 e.doit = false;
	         }
	  });
	   
	  final TreeEditor editor = new TreeEditor (tree);
      tree.addListener (SWT.Selection, new Listener () {
         public void handleEvent (Event e) {
            final TreeItem item = (TreeItem) e.item;
            if (item != null && item == lastItem [0]) {
            //if (item != null) {
               final Composite composite = new Composite (tree, SWT.NONE);
               composite.setBackground (FormsPlugin.BLACK);
               getEditableText(item,formtree);
               final Text text = new Text (composite, SWT.NONE);
               formtree.setEditText(text);
               tree.forceFocus();
               composite.addListener (SWT.Resize, new Listener () {
                  public void handleEvent (Event e) {
                     Rectangle rect = composite.getClientArea ();
                     text.setBounds (rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);            	
                  }
               });
               item.addDisposeListener(new DisposeListener() {
                  public void widgetDisposed(DisposeEvent e) {
                     composite.dispose();
                  }
               });
               //if (item == lastItem [0]) {
                  Listener textListener = new Listener () {
                     public void handleEvent (final Event e) {
                        String newText;
                        switch (e.type) {
                           case SWT.FocusOut:
                              newText = text.getText();
                              composite.dispose();
                              textChanged(item,newText,formtree);
                           	break;
                           case SWT.Verify:
                              newText = text.getText();
                              String leftText = newText.substring (0, e.start);
                              String rightText = newText.substring (e.end, newText.length ());
                              GC gc = new GC (text);
                              Point size = gc.textExtent (leftText + e.text + rightText);
                              gc.dispose();
                              size = text.computeSize (size.x, SWT.DEFAULT);
                              editor.horizontalAlignment = SWT.LEFT;
                              Rectangle itemRect = item.getBounds (), rect = tree.getClientArea ();
                              editor.minimumWidth = Math.max (size.x, itemRect.width) + 2;
                              int left = itemRect.x, right = rect.x + rect.width;
                              editor.minimumWidth = Math.min (editor.minimumWidth, right - left);
                              editor.layout();
                           	break;
                           case SWT.Traverse:
                              switch (e.detail) {
                                 case SWT.TRAVERSE_RETURN:
                                    newText = text.getText();
                                    textChanged(item,newText,formtree);
                                    composite.dispose();
                                 	e.doit = false;
                                 	break;
                                 case SWT.TRAVERSE_ESCAPE:
                                    composite.dispose();
                                 	e.doit = false;
                              }
                           	break;
                        }
                     }
                  };
                  if (!nonEditableNodes.contains(item.getData())) {
                     text.addListener (SWT.FocusOut, textListener);
                     text.addListener (SWT.Traverse, textListener);
                     text.addListener (SWT.Verify, textListener);
                     editor.setEditor (composite, item);
                     text.setText (item.getText ());
                     text.selectAll ();
                     text.setFocus ();
                  }
               }
            //} 
            lastItem [0] = item;
         }
      });
   }
  
  public static void setLastItem(TreeItem item) {
     lastItem [0] = item;
  }
   
  public static String calculateIdentity(TreeItem item) {
  	 return (String)item.getData();
  }
  
  public static void textChanged(TreeItem item,String text,FormTreeWrapper formtree) {
  	item.setText(text);
  	String identity = calculateIdentity(item);
  	formtree.textChanged(identity, text);
  }

  public static void getEditableText(TreeItem item,FormTreeWrapper formtree) {
  	String identity = calculateIdentity(item);
  	formtree.getEditableText(identity);
  }

}