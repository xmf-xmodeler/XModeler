package Engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

public class Header implements Serializable {
    
    // Each image has a header that describes
    // image properties...
    
    private Date creationDate = new Date();
    
    private Hashtable properties = new Hashtable();
    
    public Date creationDate() {
        return creationDate;
    }
    
    public boolean hasProperty(String property) {
        return properties.containsKey(property);
    }
    
    public String propertyValue(String property) {
        if(hasProperty(property))
            return (String)properties.get(property);
        else return null;
    }
    
    public void setProperty(String property,String value) {
        properties.put(property,value);
    }
    
    public String toString() {
        return "Header(" + creationDate + "," + properties + ")";
    }
}
