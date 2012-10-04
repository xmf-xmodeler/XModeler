package XOS;

import java.io.InputStream;

public abstract class XInputStream extends InputStream {
    
    public abstract boolean eof();
    
    public abstract int peek();
    
    public abstract boolean ready();

}
