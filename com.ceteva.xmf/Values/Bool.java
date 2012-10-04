package Values;

public class Bool extends Value {
   
   private boolean value;
   
   public Bool(boolean value) {
      this.value = value;
   }
   
   public Bool boolAnd(Bool b) {
      return new Bool(value && b.value);
   }
   
   public Bool not() {
      return new Bool(!value);
   }
   
   public Bool boolOr(Bool b) {
      return new Bool(value || b.value);
   }
   
   public boolean equals(Object other) {
      if(other instanceof Bool) {
         Bool b = (Bool)other;
         return value == b.value;
      } else return super.equals(other);
   }
   
   public String toString() {
      return value +"";
   }
   
   public boolean value() {
      return value;
   }
   
   public void parse(String s) throws ValueParseException {
      if (s.trim().equalsIgnoreCase("true"))
            value = true;
      else
         if (s.trim().equalsIgnoreCase("false"))
               value = false;
         else
            throw new ValueParseException("\""+s+"\" is not a valid boolean expression.");
   }
   
}
