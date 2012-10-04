package Values;

public class Int extends Value {
   
   private int value;
   
   public Int(int value) {
      this.value = value;
   }
   
   public Int add(Int i) {
      return new Int(value + i.value);
   }
   
   public boolean equals(Object other) {
      if(other instanceof Int) {
         Int b = (Int)other;
         return value == b.value;
      } else return super.equals(other);
   }
   
   public Bool greater(Int i) {
      return new Bool(value > i.value);
   }
   
   public Bool less(Int i) {
      return new Bool(value < i.value);
   }
   
   public Int sub(Int i) {
      return new Int(value - i.value);
   }
   
   public String toString() {
      return value + "";
   }
   
   public void parse(String s) throws ValueParseException {
      try {
         value = Integer.parseInt(s);
      }
      catch (NumberFormatException x) {
         throw new ValueParseException("\""+s+"\" is not a valid integer expression.");
      }
   }
   
   public int value() {
       return value;
   }
   
}
