package XOS;

public class Value implements XData {

    public int           type     = UNKNOWN;

    private StringBuffer strValue = new StringBuffer();

    public int           intValue;

    public boolean       boolValue;

    public float         floatValue;

    public Value[]       values;
    
    public Value() {
        strValue.setLength(0);
        type = UNKNOWN;
    }

    public Value(int i) {
        this.type = INT;
        this.intValue = i;
    }

    public Value(boolean b) {
        type = BOOL;
        boolValue = b;
    }

    public Value(String s) {
        type = STRING;
        setStrValue(s);
    }

    public Value(float f) {
        type = FLOAT;
        floatValue = f;
    }

    public Value(Value[] values) {
        type = VECTOR;
        this.values = values;
    }
    
    public void appendChar(char c) {
        strValue.append(c);
    }

    public boolean hasStrValue(String s) {
        if (s.length() != strValue.length())
            return false;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != strValue.charAt(i))
                return false;
        return true;
    }
    
    public void reset() {
        type = UNKNOWN;
        strValue.setLength(0);
    }

    public void setBoolValue(boolean b) {
        type = BOOL;
        boolValue = b;
    }

    public void setFloatValue(float f) {
        type = FLOAT;
        floatValue = f;
    }

    public void setIntValue(int i) {
        type = INT;
        intValue = i;
    }

    public void setStrValue(String s) {
        type = STRING;
        strValue.setLength(0);
        for (int i = 0; i < s.length(); i++)
            strValue.append(s.charAt(i));
    }

    public void setValues(Value[] values) {
        type = VECTOR;
        this.values = values;
    }
    
    public String strValue() {
        //System.out.println("StrValue(" + strValue + ")");
        return strValue.toString();
    }

    public String toString() {
        String s = "Value(";
        switch (type) {
        case INT:
            s = s + intValue + ")";
            break;
        case STRING:
            s = s + strValue + ")";
            break;
        case FLOAT:
            s = s + floatValue + ")";
            break;
        case BOOL:
            s = s + boolValue + ")";
            break;
        case VECTOR:
            for (int i = 0; i < values.length; i++) {
                s = s + values[i];
                if (i + 1 < values.length)
                    s = s + ",";
            }
            break;
        default:
            System.out.println("Unknown message arg type: " + type);
        }
        return s;
    }
}