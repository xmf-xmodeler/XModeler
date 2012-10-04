package Engine;

public class Brand {
    
    public static void main(String[]args) {
        if(args.length < 1)
            usage();
        else brand(args);
    }
    
    public static void brand(String[] args) {
        Machine machine = new Machine(null);
        String imageFile = args[0];
        machine.load(imageFile);
        for(int i = 1;i < args.length; i=i+2) {
            String property = args[i];
            String value = args[i+1];
            machine.header().setProperty(property,value);
        }
        machine.save(imageFile);
    }
    
    public static void usage() {
        System.out.println("usage: Brand <IMAGEFILE> <PROPERTY> <VALUE> ... <PROPERTY> <VALUE>");
    }

}
