package com.ceteva.dialogs;

import java.util.Vector;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;

import XOS.Message;
import XOS.Value;

import com.ceteva.client.Client;
import com.ceteva.client.EventHandler;
import com.ceteva.dialogs.progress.model.JobManager;

class DialogsClient extends Client {
	
	private JobManager jobmanager = new JobManager();

	// The following are used to cache file and directory dialogs
	// so that they remember the last location.

	private static FileDialog openDialog = null;

	private static FileDialog saveDialog = null;

	private static DirectoryDialog dirDialog = null;

	private static String lastPath = "";

	EventHandler handler = null;

	public DialogsClient() {
		super("com.ceteva.dialogs");
	}

	public Value inputDialog(Message message) {
		String title = message.args[0].strValue();
		String question = message.args[1].strValue();
		String value = message.args[2].strValue();
		InputDialog dialog = new InputDialog(getShell(), title, question,
				value, null);
        int CANCEL = 1; // SWT.CANCEL does not seem to work here (Paul)
		if(dialog.open() != CANCEL)
		  return new Value(dialog.getValue());
		else
		  return new Value("-1");
	}

	public void messageDialog(Message message) {
		String text = message.args[1].strValue();
		MessageDialog.openInformation(getShell(), "Information", text);
	}
	
	public Value orderingDialog(Message message) {
		String text = message.args[0].strValue();
		String message_ = message.args[1].strValue();
		//Object[] objs = Arrays.asList(message.args).subList(2,message.arity).toArray();
		Object[] objs = message.args[2].values;
		String[] sources = new String[objs.length];
		for(int i=0;i<objs.length;i++)
		  sources[i] = ((Value)objs[i]).strValue();
		OrderingDialog d = new OrderingDialog(getShell(),text,message_,sources);
		if(d.open() != SWT.CANCEL)
		  return getResultArray(d.getChoice());
		return new Value("");
	}
	
	public Value orderingDialog2(Message message) {
		String title = message.args[0].strValue();
		Object[] objs = message.args[1].values;
		String[] elements = new String[objs.length];
		for(int i=0;i<objs.length;i++)
		  elements[i] = ((Value)objs[i]).strValue();
		OrderingDialog2 d = new OrderingDialog2(getShell(),title,elements);
		if(d.open() != SWT.CANCEL)
		  return getResultArray(d.getChoice());
		return new Value("");
	}

	public Value questionDialog(Message message) {
		String question = message.args[0].strValue();
		boolean reply = MessageDialog.openQuestion(getShell(), "Question",
				question);
		Value value;
		if (reply)
		  value = new Value("Yes");
		else
		  value = new Value("No");
		return value;
	}

	public void errorDialog(Message message) {
		String text = message.args[1].strValue();
		MessageDialog.openError(getShell(), "Error", text);
	}
	
	public void textAreaDialog(Message message) {
		String type = message.args[1].strValue();
		String message_ = message.args[2].strValue();
		String text = message.args[3].strValue();
		int imageType = getImageType(type);
		Image image = Display.getCurrent().getSystemImage(imageType);
		TextAreaDialog tad = new TextAreaDialog(getShell(),"Message",image,message_,text,imageType,new String[]{"OK"},0);
		tad.open();
	}
	
	public int getImageType(String type) {
	  	if(type.equals("error")) 
		  return MessageDialog.ERROR; 
		else if(type.equals("info"))
		  return MessageDialog.INFORMATION;
		else if(type.equals("question")) 
		  return MessageDialog.QUESTION; 
		else if(type.equals("warning"))
		  return MessageDialog.WARNING;
	    return MessageDialog.NONE;
	}

	public void warningDialog(Message message) {
		String text = message.args[1].strValue();
		MessageDialog.openWarning(getShell(), "Warning", text);
	}
	
	public Value colorDialog(Message message) {
        String text = message.args[0].strValue();
		int red = message.args[1].intValue;
		int green = message.args[2].intValue;
		int blue = message.args[3].intValue;
		ColorDialog dialog = new ColorDialog(getShell());
        dialog.setText(text);
        if(red > 0 && green > 0 && blue > 0)
		  dialog.setRGB(new RGB(red,green,blue));
		RGB choosen = dialog.open();
		if(choosen != null) {
		  Value[] color = new Value[3];
		  color[0] = new Value(choosen.red);
		  color[1] = new Value(choosen.green);
		  color[2] = new Value(choosen.blue);
		  return new Value(color);
		}
		else {
		  Value[] color = new Value[3];
		  color[0] = new Value(-1);
		  color[1] = new Value(-1);
		  color[2] = new Value(-1);
		  return new Value(color);
		}
	}

	public Value confirmDialog(Message message) {
		String question = message.args[0].strValue();
		boolean reply = MessageDialog.openQuestion(getShell(), "Confirm",
				question);
		Value value;
		if (reply)
			value = new Value("Yes");
		else
			value = new Value("No");
		return value;
	}

	public Value fileDialog(Message message) {
		String saveOpen = message.args[0].strValue();
		String path = message.args[1].strValue();
		String extension = message.args[2].strValue();
        String initName = message.args[3].strValue();
		FileDialog fd = getFileDialog(saveOpen,path,extension,initName);
		String response = fd.open();
		lastPath = fd.getFilterPath();
		Value value;
		if(response != null) 
			value = new Value(response.replace('\\','/'));
	    else
	    	value = new Value("");
		return value;
	}
	
	public Value fontDialog(Message message) {
		String def = message.args[0].strValue();
		FontDialog fd = new FontDialog(getShell());
		if(!def.equals("")) {
		  FontData def_fd = new FontData(def);
		  FontData[] defaults = { def_fd };
		  fd.setFontList(defaults);
		}
		FontData data = fd.open();
		String datas = "";
		if(data!=null)
		  datas = data.toString();
		return new Value(datas);
	}

	public FileDialog getFileDialog(String style,String path,String extension,String initName) {
		if (style.equals("open")) {
			if (openDialog == null) {
				openDialog = new FileDialog(getShell(), SWT.OPEN);
                openDialog.setFileName(initName);
				if (!openDialog.getFilterPath().equals(path))
					openDialog.setFilterPath(path);
			}
			openDialog.setFilterExtensions(new String[] { extension });
            openDialog.setFileName(initName);
            if (!lastPath.equals(""))
            	openDialog.setFilterPath(lastPath);
			return openDialog;
		}
		if (saveDialog == null) {
			saveDialog = new FileDialog(getShell(), SWT.SAVE);
            saveDialog.setFileName(initName);
		    if (!saveDialog.getFilterPath().equals(path))
				saveDialog.setFilterPath(path);
		}
		saveDialog.setFilterExtensions(new String[] { extension });
        saveDialog.setFileName(initName);
        if (!lastPath.equals(""))
        	saveDialog.setFilterPath(lastPath);
		return saveDialog;
	}

	public Value directoryDialog(Message message) {
		String path = message.args[0].strValue();
		if (dirDialog == null) {
			dirDialog = new DirectoryDialog(getShell());
			if (!dirDialog.getFilterPath().equals(path))
			  dirDialog.setFilterPath(path);
		}
        if (!lastPath.equals(""))
        	dirDialog.setFilterPath(lastPath);
		dirDialog.setMessage("Please choose a folder from below:");
		String response = dirDialog.open();
		lastPath = dirDialog.getFilterPath();
		Value value;
		if(response != null) 
			value = new Value(response = response.replace('\\','/'));
		else
			value = new Value("");
		return value;
	}

	public Value selectionDialog(Message message) {
		boolean multi = message.args[0].boolValue;
		String title = message.args[1].strValue();
		String message_ = message.args[2].strValue();
		Object[] options = message.args[3].values;
		if(multi)
		  return SelectionDialog.openMultiSelectionDialog(title,message_,options,handler);
		else
		  return SelectionDialog.openSelectionDialog(title,message_,options,handler);
	}

	public Value getResultArray(Object[] strings) {
		Value[] values = new Value[strings.length];
		for(int i=0;i<strings.length;i++)
		  values[i] = new Value((String)strings[i]);
		return new Value(values);
	}

	public void raiseEvent(String identity, String value) {
		Message m = handler.newMessage("dialogReply",2);
		Value v1 = new Value(identity);
		Value v2 = new Value(value);
		m.args[0] = v1;
		m.args[0] = v2;
		handler.raiseEvent(m);
	}

	public Shell getShell() {
		return Display.getCurrent().getActiveShell();
	}
	
	public Value processCall(Message message) {
		if (message.hasName("newColorDialog") && message.arity == 4)
			return colorDialog(message);
		else if (message.hasName("newQuestionDialog") && message.arity == 1)
			return questionDialog(message);
		else if (message.hasName("newConfirmDialog") && message.arity == 1)
			return confirmDialog(message);
		// else if (message.hasName("newMultioptionDialog") && message.arity > 4)
		//	return multioptionDialog(message);
		else if (message.hasName("newFileDialog") && message.arity == 4)
			return fileDialog(message);
		else if (message.hasName("newFontDialog") && message.arity == 1)
			return fontDialog(message);
		else if (message.hasName("newDirectoryDialog") && message.arity == 1)
			return directoryDialog(message);
		else if (message.hasName("newSelectionDialog") && message.arity == 4)
			return selectionDialog(message);
		else if (message.hasName("newInputDialog") && message.arity == 3)
			return inputDialog(message);
		else if (message.hasName("newOrderingDialog") && message.arity == 3)
			return orderingDialog(message);
		else if (message.hasName("newOrderingDialog2") && message.arity == 2)
			return orderingDialog2(message);
		else if (message.hasName("newTreeDialog"))
			return simpleTreeDialog(message);
		else if (message.hasName("newMultiSelectionTreeDialog"))
			return multiTreeDialog(message);
		return null;
	}

	public boolean processMessage(Message message) {
		if (message.hasName("newMessageDialog") && message.arity == 2) {
			messageDialog(message);
			return true;
		} 
		else if (message.hasName("newErrorDialog") && message.arity == 2) {
			errorDialog(message);
			return true;
		} 
		else if (message.hasName("newWarningDialog") && message.arity == 2) {
			warningDialog(message);
			return true;
		}
		else if (message.hasName("newTextAreaDialog") && message.arity == 4) {
		    textAreaDialog(message);
		    return true;
		}
		return jobmanager.processMessage(message);
	}
	
	// a tree is represented as a pair A | B where A is the node and
	// B are the children of A
	
	public TreeElement buildTree(Value[] tree,Vector expand,Vector disable,Vector select) {
	    TreeElement root = new TreeElement(null,"Root");
	    buildTree(root,tree,expand,disable,select);
		return root;
	}
	
	public void buildTree(TreeElement parent,Value[] tree,Vector expand,Vector disable,Vector select) {		
		// A
		
		// xmf.multiSelectTreeDialog("BOB", Seq{"1","&",Seq{Seq{"2",Seq{Seq{"3",Seq{}}}}} } ,Seq{"1"},null);
		
		String name = tree[0].strValue();
		// if(stringContains(name,'*'))
		//	name = stringRemove(name,'*');
		TreeElement branch = new TreeElement(parent,name);;
	    parent.addChild(branch);
	    if (tree.length > 1) {
	    	Value[] children = null;
	    	
	    	if(tree[1].type == Value.VECTOR) {
	    		children = tree[1].values;
	    	}
	    	else {
	    		
	    	   // Extra information is encoded in the second element
	    		// - '&' indicates that the node should be disabled
	    		// - '*' indicates that the node should be expanded 
	    	
	    	   String encoding = tree[1].strValue();
	    	   if(stringContains(encoding,'&')) {
	    	     disable.addElement(branch);	   
	    	   }
	    	   if(stringContains(encoding,'*')) {
	    		 expand.addElement(branch);   
	    	   }
	    	   if(stringContains(encoding,'^')) {
		         select.addElement(branch);   
		       }
	    	   children = tree[2].values;
	    	}

			// Recursively build the tree

			for (int i = 0; i < children.length; i++) {
				buildTree(branch, children[i].values,expand,disable,select);
			}
		}
	}
	
	public Value multiTreeDialog(Message message) {
		String title = message.args[0].strValue();
		Value[] tree = message.args[1].values;
		Vector expand = new Vector();
		Vector disable = new Vector();
		Vector select = new Vector();
		TreeElement root = buildTree(tree,expand,disable,select);
		MultiSelectionTreeDialog treeDialog = new MultiSelectionTreeDialog(getShell(),new LabelProvider(),new TreeElementProvider());
		treeDialog.setTitle(title);
		treeDialog.setInput(root);
		treeDialog.create();
		treeDialog.expandTree(expand);
		treeDialog.disableNodes(disable);
		treeDialog.selectNodes(select);
		int returncode = treeDialog.open();
		if(returncode != 1) {
		  Object[] result = treeDialog.getResult();
		  if(result != null) {
		    Value[] values = new Value[result.length];
		    for(int i=0;i<result.length;i++) {
			  TreeElement te = (TreeElement)result[i];
			  Vector path = new Vector();
			  te.getPath(path);
			  Value[] value = new Value[path.size()];
				for(int z = path.size();z>0;z--) {
				  String s = (String)path.elementAt(z-1);
				  value[path.size()-z] = new Value(s);	
			   }
		       values[i] = new Value(value);
		    }
			return new Value(values);
		  }
		}
		return new Value("");
	}
	
	public void setEventHandler(EventHandler handler) {
	}
	
	public Value simpleTreeDialog(Message message) {
		String title = message.args[0].strValue();
		Value[] tree = message.args[1].values;
		Vector expand = new Vector();
		Vector disable = new Vector();
		Vector selected = new Vector();
		TreeElement root = buildTree(tree,expand,disable,selected);
		TreeDialog treeDialog = new TreeDialog(getShell(),new LabelProvider(),new TreeElementProvider());
		treeDialog.setTitle(title);
		treeDialog.setInput(root);
		treeDialog.create();
		treeDialog.expandTree(expand);
		int returncode = treeDialog.open();
		if(returncode != 1) {
		  Object[] result = treeDialog.getResult();
		  // if(result != null) {
		  if(result.length > 0) {
			TreeElement te = (TreeElement)result[0];
			Vector path = new Vector();
			te.getPath(path);
			Value[] value = new Value[path.size()];
			for(int i = path.size();i>0;i--) {
			  String s = (String)path.elementAt(i-1);
			  value[path.size()-i] = new Value(s);	
			}
			return new Value(value);
		  }
		}
		return new Value("");
	}
	
	public boolean stringContains(String string,char c) {
		for(int i=0;i<string.length();i++) {
		  char sc = string.charAt(i);
		  if(sc == c)
			return true;  
		}
		return false;
	}
	
	public String stringRemove(String string,char c) {
        String newString = "";
		for(int i=0;i<string.length();i++) {
		  char sc = string.charAt(i);
		  if(sc != c)
			newString = newString + sc;  
		}
		return newString;
	}
}