package Values;

public class Null extends Value {
	
    // We will need to place null into Java representations of XML values. Since
    // we can do things with null in XOCL but cannot treat Java null as a proper
    // object, define a representation for null:
    
	public static Null nullValue = new Null();
	
	public Null() {}
	
	public String toString() {
		return "null";
	}
}