package XOS;

public class Message {

    // A message consists of a name, a unique id and a sequence of
    // message arguments. Messages are passed by clients via XOS.

    public static final int MAXARGS = 30;

    private StringBuffer    name    = new StringBuffer();

    public int              arity;

    public Value[]          args    = new Value[MAXARGS];

    public Message() {
        name.setLength(0);
        arity = 0;
    }

    public Message(String name, int arity) {
        this.setName(name);
        this.arity = arity;
    }
    
    public void appendNameChar(char c) {
        name.append(c);
    }

    public boolean hasName(String name) {
        if (name.length() != this.name.length())
            return false;
        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) != this.name.charAt(i))
                return false;
        return true;
    }

    public String name() {
        return name.toString();
    }
    
    public void setArity(int arity) {
        this.arity = arity;
    }

    public void setName(String name) {
        this.name.setLength(0);
        for (int i = 0; i < name.length(); i++)
            this.name.append(name.charAt(i));
    }

    public String toString() {
        String s = name + "(";
        for (int i = 0; i < arity; i++) {
            s = s + args[i];
            if (i < arity - 1)
                s = s + ",";
        }
        return s + ")";
    }

}