package Engine;

import java.util.Vector;

public class ArgParser {

    // This class provides static methods that deal with parsing and deconstructing
    // command line arguments.

    public static String argName(String argSpec) {

        // Returns the argument name portion of the supplied arg spec.

        int separator = argSpec.indexOf(':');
        if (separator == -1) {
            System.out.println("An arg should take the form <NAME>:<INT> " + argSpec);
            System.exit(1);
        }
        return argSpec.substring(0, separator);
    }

    public static int argParams(String argSpec) {

        // Returns the number of parameters required by the supplied argument
        // specification.

        int separator = argSpec.indexOf(':');
        if (separator == -1) {
            System.out.println("An arg should take the form <NAME>:<INT> " + argSpec);
            System.exit(1);
        }
        return Integer.parseInt(argSpec.substring(separator + 1, argSpec.length()));
    }

    public static String argSpec(String name, String[] argSpecs) {

        // Returns the specification of the named argument in the supplied
        // command line argument specs or null of no specification exists.

        for (int i = 0; i < argSpecs.length; i++)
            if (name.equals(argName(argSpecs[i])))
                return argSpecs[i];
        return null;
    }

    public static boolean isArg(String name, String[] argSpecs) {
        return argSpec(name, argSpecs) != null;
    }

    public static String[] parseArgs(String[] argSpecs, String[] args) {

        // Set up the command line arguments in the appropriate string arrays.
        // Not all of the arguments may be consumed by the arg specs. 

        Vector argsv = new Vector();
        int index = 0;
        while (index < args.length) {
            int params = 0;
            String arg = args[index];
            if (isArg(arg, argSpecs)) {
                params = argParams(argSpec(arg, argSpecs));
                argsv.addElement(args[index++]);
                for (int i = 0; i < params; i++)
                    argsv.addElement(args[index++]);
            } else index++;
        }
        String[] newArgs = new String[argsv.size()];
        argsv.copyInto(newArgs);
        return newArgs;
    }

}