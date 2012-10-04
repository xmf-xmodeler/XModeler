package com.ceteva.oleBridge;

import java.util.Enumeration;
import java.util.Hashtable;

import XOS.Value;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class Component {
	
	// call, invoke, get, getProperty, setProperty, put 
	
	private static Hashtable idDispatch = new Hashtable();
	
	public static void newTopLevel(String id,String target) {
		ActiveXComponent ax = new ActiveXComponent(target);
		store(id,ax.getObject());
		Dispatch.put(ax,"Visible",true);
	}
	
	public static void call(String targetId,String call,Value[] args) throws DispatchException {
		if(idDispatch.containsKey(targetId)) {
		  Object[] vars = valuesToVariants(args);
		  Dispatch target = (Dispatch)idDispatch.get(targetId);
		  if(vars.length == 0)
		    Dispatch.call(target,call);
		  if(vars.length == 1)
		    Dispatch.call(target,call,vars[0]);
		  if(vars.length == 2)
			Dispatch.call(target,call,vars[0],vars[1]);
		  if(vars.length == 3)
			Dispatch.call(target,call,vars[0],vars[1],vars[2]);
		  if(vars.length == 4)
			Dispatch.call(target,call,vars[0],vars[1],vars[3],vars[4]);
		}
		else {
		  throw new DispatchException("No such dispatch: " + targetId);	
		}
	}
	
	public static void callAndStore(String targetId,String storeId,String call,Value[] args) throws DispatchException {
		if(idDispatch.containsKey(targetId)) {
	      Object[] vars = valuesToVariants(args);
	      Dispatch target = (Dispatch)idDispatch.get(targetId);
	      Dispatch d = null;
	      if(vars.length == 0)
	    	d = Dispatch.call(target,call).toDispatch();
	      if(vars.length == 1)
	    	d = Dispatch.call(target,call,vars[0]).toDispatch();
	      if(vars.length == 2)
		    d = Dispatch.call(target,call,vars[0],vars[1]).toDispatch();
	      if(vars.length == 3)
			d = Dispatch.call(target,call,vars[0],vars[1],vars[2]).toDispatch();
	      if(vars.length == 4)
		    d = Dispatch.call(target,call,vars[0],vars[1],vars[3],vars[4]).toDispatch();
	      store(storeId,d);
	    }
	    else {
	      throw new DispatchException("No such dispatch: " + targetId);	
	    }
	}
	
	public static Value get(String targetId,String property) throws DispatchException {
		if(idDispatch.containsKey(targetId)) {
		  Object o = idDispatch.get(targetId);
		  Variant v = Dispatch.get(o,property);
		  return variantToValue(v);
		}
		throw new DispatchException("No such dispatch: " + targetId);
	}
	
	public static void getObject(String targetId,String property,String id) throws DispatchException {
		if(idDispatch.containsKey(targetId)) {
		  Object o = idDispatch.get(targetId);
		  store(id,Dispatch.get(o,property).getDispatch());
		}
		else {
		  String available = "";
		  Enumeration e = idDispatch.keys();
		  while(e.hasMoreElements()) {
		    available = available + e.nextElement() + " ";	
		  }
		  throw new DispatchException("No such dispatch: " + targetId + " available: " + available);
		}
	}
	
	public static void set(String targetId,String property,Value value) throws DispatchException {
		if(idDispatch.containsKey(targetId)) {
		  Object o = idDispatch.get(targetId);
		  switch(value.type) {
		    case Value.STRING:
		      Dispatch.put(o,property,value.strValue()); break;
		    case Value.INT:
		      Dispatch.put(o,property,new Variant(value.intValue)); break;
		    case Value.BOOL:
		      Dispatch.put(o,property,new Variant(value.boolValue)); break;
		  }
		}
		else {
		  throw new DispatchException("No such dispatch: " + targetId);	
		}
	}
	
	public static void setToDispatch(String targetId,String property,String id) throws DispatchException  {
	    if(idDispatch.containsKey(targetId) && idDispatch.containsKey(id)) {
	      Object o = idDispatch.get(targetId);
	      Object v = idDispatch.get(id);
	      Dispatch.put(o,property,v);	
	    }
		else {
		  throw new DispatchException("No such dispatch: " + targetId);	
		}
	}
	
	public static void store(String storeid,Object value) {
	    idDispatch.put(storeid,value);	
	}
	
	public static Value variantToValue(Variant v) {
		if(v.getvt() == Variant.VariantString)
		  return new Value(v.getString());
		else if(v.getvt() == Variant.VariantInt) 
		  return new Value(v.getInt());  
		else if(v.getvt() == Variant.VariantBoolean)	
		  return new Value(v.getBoolean());
		return null;
	}
	
	public static Variant valueToVariant(Value v) {
		if(v.type == Value.STRING)
		  return new Variant(v.strValue());
		else if(v.type == Value.INT)
		  return new Variant(v.intValue);
		else if(v.type == Value.BOOL)
		  return new Variant(v.boolValue);
		return null;
	}
	
	public static Object[] valuesToVariants(Value[] values) {
		Object[] objects = new Object[values.length];
		for(int i=0;i<values.length;i++){
		  Variant v = valueToVariant(values[i]);
		  objects[i] = v;
		}
		return objects;
	}

}
