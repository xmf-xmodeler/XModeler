package Values;

public class Str extends Value {
    
    private String value;
    
    public Str(String value) {
        this.value = value;
    }
    
    public Str add(Str other) {
        return new Str(value + other.value);
    }
    
    public Str add(String s) {
        return new Str(value + s);
    }
    
    public boolean equals(Object other) {
        if(other instanceof Str) {
            Str s = (Str)other;
            return value.equals(s.value);
        } else return super.equals(other);
    }
    
    public void println() {
        System.out.println(value);
    }
    
    public Value splitBy(Value s,Value start,Value end) {
        // Ignore the start and end for now....
        String[] split = this.toString().split(s.toString());
        return new Values(split);
    }
    
    public String toString() {
        //return "\"" + value + "\"";
        return value;
    }

    public void parse(String s) {
       value = s;
    }

}
