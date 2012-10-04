package xjava;

public class XMFToJavaTypeError extends Error {
    
    private int value;
    private Class type;

    public XMFToJavaTypeError(String message,int value,Class type) {
        super(message);
        this.value = value;
        this.type = type;
    }

}
