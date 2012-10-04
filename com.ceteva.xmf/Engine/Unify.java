package Engine;

public class Unify {

public static int unify(Machine machine,int Var,int pattern,int value,int env) {
        
        // Returns an updated environment or null after unifying the pattern and
        // the value.
        
        pattern = deref(machine,pattern,env);
        value = deref(machine,value,env);
        if(machine.equalValues(pattern,value))
            return env;
        else {
            if(machine.type(pattern) == Var)
                return machine.mkCons(machine.mkCons(pattern,value),env);
            else {
                if(machine.type(value) == Var)
                    return machine.mkCons(machine.mkCons(value,pattern),env);
                else {
                    if(Machine.isCons(pattern) && Machine.isCons(value)) {
                        env = unify(machine,Var,machine.consHead(pattern),machine.consHead(value),env);
                        if(env == Machine.undefinedValue)
                            return env;
                        else return unify(machine,Var,machine.consTail(pattern),machine.consTail(value),env);
                    } else {
                        int ptype = machine.type(pattern);
                        int vtype = machine.type(value);
                        if(Machine.isObj(pattern) && Machine.isObj(value) && machine.inheritsFrom(vtype,ptype)){
                                    int slots = machine.objAttributes(pattern);
                            while((slots != Machine.nilValue) && (env != Machine.undefinedValue)) {
                                int slot = machine.consHead(slots);
                                slots = machine.consTail(slots);
                                int name = machine.consHead(slot);
                                int value1 = machine.consTail(slot);
                                int value2 = machine.objAttValue(value,name);
                                env = unify(machine,Var,value1,value2,env);
                            }
                            return env;
                        } else {
                            if(Machine.isSet(pattern))
                                return unify(machine,Var,machine.asSeq(pattern),value,env);
                            else {
                                if(Machine.isSet(value))
                                    return unify(machine,Var,pattern,machine.asSeq(value),env);
                                else return Machine.undefinedValue;
                            }
                            
                        }
                    }
                        
                }
            }
        }
    }    public static int deref(Machine machine, int value, int env) {
        int binds = assoc(machine, value, env);
        while (binds != Machine.undefinedValue) {
            value = machine.consTail(machine.consHead(binds));
            binds = assoc(machine, value, env);
        }
        return value;
    }

    public static int assoc(Machine machine, int value, int env) {
        if (env == Machine.nilValue)
            return Machine.undefinedValue;
        else {
            int cell = machine.consHead(env);
            int key = machine.consHead(cell);
            if (key == value)
                return env;
            else
                return assoc(machine, value, machine.consTail(env));
        }
    }
}
