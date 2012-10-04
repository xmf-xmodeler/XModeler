package Engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xjava.AlienObject;
import xjava.XJ;
import Database.Database;
import XML.XMLReader;

public class ForeignFuns implements Value, Instr, Errors {

    // Foreign functions are those written in java and which can be
    // applied to a machine state.

    public static void addToTable(Machine machine, int table, ForeignFun fun) {
        int symbol = machine.mkSymbol(fun.name());
        int value = machine.newForeignFun(fun);
        machine.symbolSetValue(symbol, value);
        machine.hashTablePut(table, symbol, value);
    }

    public static void builtinForeignFuns(Machine machine, int table) {
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "gc", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_addAtt", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arrayDaemons", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arrayDaemonsActive", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arrayLength", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arrayRef", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arraySet", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arraySetDaemons", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_arraySetDaemonsActive", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_asSeq", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_asSet", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_asString", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_available", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_backtrace", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferAsString", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferIncrement", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferDaemons", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferDaemonsActive", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferSetAsString", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferSetDaemons", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferSetDaemonsActive", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferSetSize", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferSize", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_bufferStorage", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_call", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_callcc", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_ceiling", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_client_connect", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_clientInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_clientOutputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_close", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_closeAll", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_closeZipInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetCode", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxCodeSize", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxConstants", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxInstrAt", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxLocals", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxName", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxOperandsAt", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetConstants", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetInstrAt", 4));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetResourceName", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxResourceName", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetName", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSetSource", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxSource", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeBoxToFun", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_codeSet", 4));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_copy", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_copyFile", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_cos", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonId", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonType", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSlot", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonAction", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonPersistent", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonTraced", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonTarget", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetId", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetType", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetSlot", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetAction", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetPersistent", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetTraced", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonSetTarget", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemons", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonsOff", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_daemonsOn", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_date", 1));

        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbAutoCommit", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbClose", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbCommit", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbConnect", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbLoadDriver", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbUpdate", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbQuery", 2));

        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbQueryLookup", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbQueryNext", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbQueryPrevious", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dbQueryClose", 1));

        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_deleteFile", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_die", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_dirContents", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_div", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_eof", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_exec", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_execRedo", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_execUndo", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_exit", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_extendHeap", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_floor", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_flush", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_letVar", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_loadbin", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_lsh", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fileReadOnly", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fileExists", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fileInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fileOutputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fileSize", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forName", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forName2", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_fork", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefs", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefPath", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefValue", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefListeners", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefSetPath", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefSetValue", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_forwardRefSetListeners", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funArity", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funArgs", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSig", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funCodeBox", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funGlobals", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funIsVarArgs", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funDoc", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funDynamics", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funName", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funOwner", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funProperties", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSelf", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetCodeBox", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetDoc", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetSig", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetArgs", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetArity", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetDynamics", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetGlobals", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetIsVarArgs", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetName", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetOwner", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetProperties", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetSelf", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetSupers", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSupers", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funTraced", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_funSetTraced", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_getForeignSlot", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_getLastModified", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_getNonReferencedElements", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_getSlotValue", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_getVar", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_graphLayout", 7));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_gzipInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_gzipOutputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_hasForeignSlot", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_hasSlot", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_hasVar", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_hashCode", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_heapSize", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_import", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_imports", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_indexOf", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_invoke", 4));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_isDir", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_isOlder", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_lastIndexOf", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_load", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_load3_tmp", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_local", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_lowerCase", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_needsGC", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_memory", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mk24bit", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkArray", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkBasicTokenChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkBuffer", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkCode", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkCodeBox", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkDataInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkDataOutputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkDaemon", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkDir", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkFloat", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkFun", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkString", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkTable", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkObj", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mkSymbol", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_mod", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_newListeners", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_newObj", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_nextToken", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objDaemonsActive", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objSetDaemonsActive", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objIsSaveAsLookup", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objSetSaveAsLookup", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objIsNotVMNew", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objSetNotVMNew", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objHotLoad", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objSetHotLoad", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_objSlots", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_of", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_operationCodeBox", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_operatorPrecedenceList", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_peek", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_pow", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_readString", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_readVector", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_readXML", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_rebindStdin", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_rebindStdout", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_redoStackSize", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_removeAtt", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_renameFile", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_resetSaveLoad", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_resetToInitialState", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_random", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_round", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_rsh", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_save", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_save2", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_save3_tmp", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_saxInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_sendForeignInstance", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setDaemons", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setOf", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setCharAt", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setConstructorArgs", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setDefaultGetMOP", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setDefaultSetMOP", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setDefaultSendMOP", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setFileLastModified", 5));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setForeignSlot", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setForwardRefs", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setSlotValue", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setTypes", 26));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_setUndoSize", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_size", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_sin", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_slotNames", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_sortNamedElements", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_sqrt", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_stats", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_stackFrames", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_startUndoContext", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_endUndoContext", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_stringInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_subString", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_symbolName", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_symbolSetName", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_symbolValue", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_symbolSetValue", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tableKeys", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tableRemove", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tableHasKey", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tableHasValue", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tag", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tempFile", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tempDir", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_thread", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_threadId", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_threadKill", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_threadState", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_threadNext", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_time", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_timeAdd", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_timeDifference", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_tokenChannelTextTo", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_traceFrames", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_traceInstrs", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_traceInstrsToFile", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_usedHeap", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_undoStackSize", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_unify", 4));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_URLInputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_value", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_valueToString", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_wake", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_write", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_writeCommand", -1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_writePacket", 3));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_yield", 0));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_zipNewEntry", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_zipInputChannel", 2));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "Kernel_zipOutputChannel", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "print", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "restoreMachineState", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "saveMachineState", 1));
        addToTable(machine, table, new ForeignFun("Engine.ForeignFuns", "resetOperatorTable", 0));
    }

    public static void Kernel_dbAutoCommit(Machine machine) {
        Database.autoCommit(machine);
    }

    public static void Kernel_dbClose(Machine machine) {
        Database.close(machine);
    }

    public static void Kernel_dbCommit(Machine machine) {
        Database.autoCommit(machine);
    }

    public static void Kernel_dbConnect(Machine machine) {
        Database.connect(machine);
    }

    public static void Kernel_dbLoadDriver(Machine machine) {
        Database.loadDriver(machine);
    }

    public static void Kernel_dbUpdate(Machine machine) {
        Database.update(machine);
    }

    public static void Kernel_dbQuery(Machine machine) {
        Database.query(machine);
    }

    public static void Kernel_dbQueryLookup(Machine machine) {
        Database.queryResultLookup(machine);
    }

    public static void Kernel_dbQueryNext(Machine machine) {
        Database.queryResultNext(machine);
    }

    public static void Kernel_dbQueryPrevious(Machine machine) {
        Database.queryResultPrevious(machine);
    }

    public static void Kernel_dbQueryClose(Machine machine) {
        Database.queryClose(machine);
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                deleteFile(new File(file, children[i]));
            }
        }
        file.delete();
    }

    public static void copySingleFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            String[] children = srcDir.list();
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copySingleFile(srcDir, dstDir);
        }
    }

    public static void error(int id, Machine machine, String message) {
        machine.popFrame(); // this probably shouldn't happen every time (Paul)
        machine.error(id, message);
    }

    public static void Kernel_callcc(Machine machine) {
        // Create a new continution.
        machine.callcc(machine.frameLocal(0));
    }

    public static void print(Machine machine) {
        // Print the argument. Return the value.
        int value = machine.frameLocal(0);
        System.out.print(machine.valueToString(value));
        System.out.flush();
        machine.pushStack(value);
        machine.popFrame();
    }

    public static void gc(Machine machine) {
        // Cause the machine to collect garbage.
        machine.gc();
        machine.popStack();
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void saveMachineState(Machine machine) {
        // Save the current state of the machine.
        int fileName = machine.frameLocal(0);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
        machine.save(machine.basicStringToString(fileName));
    }

    public static void restoreMachineState(Machine machine) {
        // Restore the saved state of a machine.
        int fileName = machine.frameLocal(0);
        machine.load(machine.basicStringToString(fileName));
    }

    public static void resetOperatorTable(Machine machine) {
        machine.resetOperatorTable();
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_asSeq(Machine machine) {
        // Transform a collection to a sequence.
        machine.pushStack(machine.asSeq(machine.frameLocal(0)));
        machine.popFrame();
    }

    public static void Kernel_asSet(Machine machine) {
        // Transform a collection to a set.
        machine.pushStack(machine.asSet(machine.frameLocal(0)));
        machine.popFrame();
    }

    public static void Kernel_addAtt(Machine machine) {
        // addField(obj,att). Add the given att to the object.
        // The object is modified in place. The attribute name
        // must be either a string or a symbol. If it is a string
        // then the string is interned.
        int obj = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        int value = machine.frameLocal(2);
        machine.objAddAttribute(obj, name, value);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_arrayDaemons(Machine machine) {
        int array = machine.frameLocal(0);
        machine.pushStack(machine.arrayDaemons(array));
        machine.popFrame();
    }

    public static void Kernel_arrayDaemonsActive(Machine machine) {
        int array = machine.frameLocal(0);
        machine.pushStack(machine.arrayDaemonsActive(array));
        machine.popFrame();
    }

    public static void Kernel_arrayLength(Machine machine) {
        int array = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(machine.arrayLength(array)));
        machine.popFrame();
    }

    public static void Kernel_arrayRef(Machine machine) {
        int array = machine.frameLocal(0);
        int index = machine.frameLocal(1);
        machine.pushStack(machine.arrayRef(array, Machine.value(index)));
        machine.popFrame();
    }

    public static void Kernel_arraySet(Machine machine) {
        int array = machine.frameLocal(0);
        int index = machine.frameLocal(1);
        int value = machine.frameLocal(2);
        machine.arraySet(array, Machine.value(index), value);
        machine.pushStack(array);
        machine.popFrame();
    }

    public static void Kernel_arraySetDaemons(Machine machine) {
        int array = machine.frameLocal(0);
        int daemons = machine.frameLocal(1);
        machine.arraySetDaemons(array, daemons);
        machine.pushStack(array);
        machine.popFrame();
    }

    public static void Kernel_arraySetDaemonsActive(Machine machine) {
        int array = machine.frameLocal(0);
        int daemonsActive = machine.frameLocal(1);
        machine.arraySetDaemons(array, daemonsActive);
        machine.pushStack(array);
        machine.popFrame();
    }

    public static void Kernel_asString(Machine machine) {
        int value = machine.frameLocal(0);
        int string = -1;
        switch (Machine.tag(value)) {
        case INT:
            string = machine.mkString(1);
            machine.stringSet(string, 0, Machine.byte1(value));
            break;
        case CONS:
            string = machine.asString(value);
            break;
        case ARRAY:
            string = machine.asString(value);
            break;
        case BUFFER:
            string = machine.asString(value);
            break;
        default:
            string = machine.mkString(0);
        }
        machine.pushStack(string);
        machine.popFrame();
    }

    public static void Kernel_available(Machine machine) {
        int inputChannel = machine.frameLocal(0);
        machine.pushStack(machine.available(inputChannel));
        machine.popFrame();
    }

    public static void Kernel_backtrace(Machine machine) {
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
        machine.printBacktrace();
    }

    public static void Kernel_bufferAsString(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferAsString(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferDaemons(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferDaemons(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferDaemonsActive(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferDaemonsActive(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferIncrement(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferIncrement(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferSize(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferSize(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferStorage(Machine machine) {
        int buffer = machine.frameLocal(0);
        machine.pushStack(machine.bufferStorage(buffer));
        machine.popFrame();
    }

    public static void Kernel_bufferSetAsString(Machine machine) {
        int buffer = machine.frameLocal(0);
        int asString = machine.frameLocal(1);
        machine.bufferSetAsString(buffer, asString);
        machine.pushStack(buffer);
        machine.popFrame();
    }

    public static void Kernel_bufferSetDaemons(Machine machine) {
        int buffer = machine.frameLocal(0);
        int daemons = machine.frameLocal(1);
        machine.bufferSetDaemons(buffer, daemons);
        machine.pushStack(buffer);
        machine.popFrame();
    }

    public static void Kernel_bufferSetDaemonsActive(Machine machine) {
        int buffer = machine.frameLocal(0);
        int active = machine.frameLocal(1);
        machine.bufferSetDaemonsActive(buffer, active);
        machine.pushStack(buffer);
        machine.popFrame();
    }

    public static void Kernel_bufferSetSize(Machine machine) {
        int buffer = machine.frameLocal(0);
        int size = machine.frameLocal(1);
        machine.bufferSetSize(buffer, size);
        machine.pushStack(buffer);
        machine.popFrame();
    }

    public static void Kernel_call(Machine machine) {
        int output = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.pushStack(machine.call(output, value));
        machine.popFrame();
    }

    public static void Kernel_ceiling(Machine machine) {
        int f = machine.frameLocal(0);
        if (machine.isFloat(f)) {
            machine.pushStack(Machine.mkInt((int) Math.ceil(Double.parseDouble(machine.valueToString(f)))));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_ceiling expects a float " + machine.valueToString(f));
    }

    public static void Kernel_client_connect(Machine machine) {
        // String addr = machine.valueToString(machine.frameLocal(0));
        // int port = machine.frameLocal(1);
        System.out.println("Kernel_clientConnect() not implemented");
        System.exit(0);
        machine.popFrame();
    }

    public static void Kernel_clientInputChannel(Machine machine) {
        int name = machine.frameLocal(0);
        int in = machine.clientInputChannel(name);
        if (in != -1)
            machine.pushStack(in);
        else
            error(ERROR, machine, "Cannot find input channel for " + machine.valueToString(name));
        machine.popFrame();
    }

    public static void Kernel_clientOutputChannel(Machine machine) {
        int name = machine.frameLocal(0);
        int out = machine.clientOutputChannel(name);
        if (out != -1)
            machine.pushStack(out);
        else
            error(ERROR, machine, "Cannot find output channel for " + machine.valueToString(name));
        machine.popFrame();
    }

    public static void Kernel_close(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.close(channel);
        machine.pushStack(channel);
        machine.popFrame();
    }

    public static void Kernel_closeAll(Machine machine) {
        machine.closeAll();
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_closeZipInputChannel(Machine machine) {
        int fileName = machine.frameLocal(0);
        machine.closeZipInputChannel(fileName);
        machine.pushStack(fileName);
        machine.popFrame();
    }

    public static void Kernel_codeBoxConstants(Machine machine) {
        int codeBox = machine.frameLocal(0);
        machine.pushStack(machine.codeBoxConstants(codeBox));
        machine.popFrame();
    }

    public static void Kernel_codeBoxCodeSize(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int code = machine.codeBoxInstrs(codeBox);
        machine.pushStack(Machine.mkInt(machine.codeLength(code)));
        machine.popFrame();
    }

    public static void Kernel_codeBoxInstrAt(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int index = Machine.value(machine.frameLocal(1));
        int code = machine.codeBoxInstrs(codeBox);
        machine.pushStack(Machine.mkInt(Machine.tag(machine.codeRef(code, index))));
        machine.popFrame();
    }

    public static void Kernel_codeBoxOperandsAt(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int index = Machine.value(machine.frameLocal(1));
        int code = machine.codeBoxInstrs(codeBox);
        machine.pushStack(Machine.mkInt(Machine.value(machine.codeRef(code, index))));
        machine.popFrame();
    }

    public static void Kernel_codeBoxLocals(Machine machine) {
        int codeBox = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(machine.codeBoxLocals(codeBox)));
        machine.popFrame();
    }

    public static void Kernel_codeBoxName(Machine machine) {
        int codeBox = machine.frameLocal(0);
        machine.pushStack(machine.codeBoxName(codeBox));
        machine.popFrame();
    }

    public static void Kernel_codeBoxResourceName(Machine machine) {
        int codeBox = machine.frameLocal(0);
        machine.pushStack(machine.codeBoxResourceName(codeBox));
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetName(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        if (!machine.isSymbol(name))
            name = machine.mkSymbol(name);
        machine.codeBoxSetName(codeBox, name);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetConstants(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int constants = machine.frameLocal(1);
        machine.codeBoxSetConstants(codeBox, constants);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetCode(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int code = machine.frameLocal(1);
        machine.codeBoxSetInstrs(codeBox, code);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetInstrAt(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int index = Machine.value(machine.frameLocal(1));
        int tag = Machine.value(machine.frameLocal(2));
        int operands = Machine.value(machine.frameLocal(3));
        int code = machine.codeBoxInstrs(codeBox);
        int instr = Machine.mkImmediate(tag, Machine.value(operands));
        machine.codeSet(code, index, instr);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetResourceName(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int resourceName = machine.frameLocal(1);
        machine.codeBoxSetResourceName(codeBox, resourceName);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSetSource(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int source = machine.frameLocal(1);
        machine.codeBoxSetSource(codeBox, source);
        machine.pushStack(codeBox);
        machine.popFrame();
    }

    public static void Kernel_codeBoxSource(Machine machine) {
        int codeBox = machine.frameLocal(0);
        machine.pushStack(machine.codeBoxSource(codeBox));
        machine.popFrame();
    }

    public static void Kernel_codeSet(Machine machine) {
        int array = machine.frameLocal(0);
        int index = machine.frameLocal(1);
        int tag = machine.frameLocal(2);
        int operands = machine.frameLocal(3);
        int instr = Machine.mkImmediate(Machine.value(tag), Machine.value(operands));
        machine.codeSet(array, Machine.value(index), instr);
        machine.pushStack(array);
        machine.popFrame();
    }

    public static void Kernel_codeBoxToFun(Machine machine) {
        int codeBox = machine.frameLocal(0);
        int arity = machine.frameLocal(1);
        int dynamics = machine.frameLocal(2);
        machine.pushStack(machine.codeBoxToFun(codeBox, Machine.value(arity), dynamics));
        machine.popFrame();
    }

    public static void Kernel_cos(Machine machine) {
        int angle = machine.frameLocal(0);
        if (machine.isFloat(angle)) {
            float f = Float.parseFloat(machine.valueToString(angle));
            double radians = (Math.PI / 180) * f;
            machine.pushStack(machine.mkFloat(Math.cos(radians)));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_cos expects a float " + machine.valueToString(angle));
    }

    public static void Kernel_copy(Machine machine) {
        int value = machine.frameLocal(0);
        machine.pushStack(machine.copy(value));
        machine.popFrame();
    }

    public static void Kernel_copyFile(Machine machine) {
        int source = machine.frameLocal(0);
        int target = machine.frameLocal(1);
        File sourcef = new File(machine.valueToString(source));
        File targetf = new File(machine.valueToString(target));
        if (sourcef.exists()) {
            try {
                if (sourcef.isDirectory()) {
                    copyDirectory(sourcef, targetf);
                    machine.pushStack(Machine.trueValue);
                } else {
                    copySingleFile(sourcef, targetf);
                    machine.pushStack(Machine.trueValue);
                }
            } catch (IOException iox) {
                machine.pushStack(Machine.falseValue);
            }
        } else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_moveFile(Machine machine) {
        int name = machine.frameLocal(0);
        File file = new File(machine.valueToString(name));
        if (file.exists()) {
            deleteFile(file);
            machine.pushStack(Machine.trueValue);
        } else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_daemonId(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonId(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonType(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonType(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonSlot(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonSlot(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonAction(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonAction(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonPersistent(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonPersistent(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonTraced(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonTraced(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonTarget(Machine machine) {
        int daemon = machine.frameLocal(0);
        machine.pushStack(machine.daemonTarget(daemon));
        machine.popFrame();
    }

    public static void Kernel_daemonSetId(Machine machine) {
        int daemon = machine.frameLocal(0);
        int id = machine.frameLocal(1);
        machine.daemonSetId(daemon, id);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetType(Machine machine) {
        int daemon = machine.frameLocal(0);
        int type = machine.frameLocal(1);
        machine.daemonSetType(daemon, type);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetSlot(Machine machine) {
        int daemon = machine.frameLocal(0);
        int slot = machine.frameLocal(1);
        machine.daemonSetSlot(daemon, slot);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetAction(Machine machine) {
        int daemon = machine.frameLocal(0);
        int action = machine.frameLocal(1);
        machine.daemonSetAction(daemon, action);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetPersistent(Machine machine) {
        int daemon = machine.frameLocal(0);
        int persistent = machine.frameLocal(1);
        machine.daemonSetPersistent(daemon, persistent);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetTraced(Machine machine) {
        int daemon = machine.frameLocal(0);
        int traced = machine.frameLocal(1);
        machine.daemonSetTraced(daemon, traced);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemonSetTarget(Machine machine) {
        int daemon = machine.frameLocal(0);
        int target = machine.frameLocal(1);
        machine.daemonSetTarget(daemon, target);
        machine.pushStack(daemon);
        machine.popFrame();
    }

    public static void Kernel_daemons(Machine machine) {
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.objDaemons(obj));
        machine.popFrame();
    }

    public static void Kernel_daemonsOff(Machine machine) {
        int obj = machine.frameLocal(0);
        machine.objSetDaemonsActive(obj, Machine.falseValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_daemonsOn(Machine machine) {
        int obj = machine.frameLocal(0);
        machine.objSetDaemonsActive(obj, Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_date(Machine machine) {
        boolean verbose = machine.frameLocal(0) == Machine.trueValue;
        java.util.Date d = new java.util.Date();
        String dateString = "";
        if (verbose)
            dateString = d.toString();
        else {
            SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
            dateFormat.applyPattern("MMM dd HH:mm:ss");
            dateString = dateFormat.format(d);
        }
        machine.pushStack(machine.mkString(dateString));
        machine.popFrame();
    }

    public static void Kernel_deleteFile(Machine machine) {
        int name = machine.frameLocal(0);
        File file = new File(machine.valueToString(name));
        if (file.exists()) {
            deleteFile(file);
            machine.pushStack(Machine.trueValue);
        } else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_die(Machine machine) {
        machine.killCurrentThread();
    }

    public static void Kernel_dirContents(Machine machine) {
        int dir = machine.frameLocal(0);
        int dirFilters = machine.frameLocal(1);
        if (!Machine.isCons(dirFilters))
            error(TYPE, machine, "Kernel_dirContents expects filters to be a sequence.");
        else {
            String dirString = machine.valueToString(dir);
            String filters = "";
            while (dirFilters != Machine.nilValue) {
                int filter = machine.consHead(dirFilters);
                filters = filters + machine.valueToString(filter) + "|";
                dirFilters = machine.consTail(dirFilters);
            }
            filters = filters.substring(0, filters.length() - 1); // delete the extra bar
            File file = new File(dirString);
            final Pattern filter = Pattern.compile(filters);
            String[] contents = file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File file = new File(dir.getPath() + "/" + name);
                    if (file.isDirectory())
                        return true;
                    Matcher m = filter.matcher(name);
                    return m.matches();
                }
            });
            int result = Machine.nilValue;
            List contentsList = Arrays.asList(contents);
            for (int i = 0; i < contentsList.size(); i++) {
                String content = (String) contentsList.get(i);
                int value = machine.mkString(content);
                result = machine.mkCons(value, result);
            }
            machine.pushStack(result);
            machine.popFrame();
        }
    }

    public static void Kernel_div(Machine machine) {
        int n = Machine.value(machine.frameLocal(0));
        int m = Machine.value(machine.frameLocal(1));
        machine.pushStack(Machine.mkInt(n / m));
        machine.popFrame();
    }

    public static void Kernel_eof(Machine machine) {
        int inputChannel = machine.frameLocal(0);
        if (machine.eof(inputChannel))
            machine.pushStack(Machine.trueValue);
        else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_execRedo(Machine machine) {
        machine.undo.execRedo(machine);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_execUndo(Machine machine) {
        machine.undo.execUndo(machine);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_exec(Machine machine) {
        int program = machine.frameLocal(0);
        int args = machine.frameLocal(1);
        String[] execArgs = new String[machine.consLength(args) + 1];
        execArgs[0] = machine.valueToString(program);
        int index = 1;
        while (args != Machine.nilValue) {
            int arg = machine.consHead(args);
            execArgs[index++] = machine.valueToString(arg);
            args = machine.consTail(args);
        }
        try {
            Process p = Runtime.getRuntime().exec(execArgs);
            InputStream results = p.getInputStream();
            OutputStream commands = p.getOutputStream();
            int result = machine.mkCons(machine.mkInputChannel(results), machine.mkOutputChannel(commands));
            machine.pushStack(result);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
            error(FOREIGNFUNERR, machine, ioe.getMessage());
        }
        machine.popFrame();
    }

    public static void Kernel_exit(Machine machine) {
        // Use the following sparingly! Never returns.
        int value = machine.frameLocal(0);
        machine.exit(value);
    }

    public static void Kernel_extendHeap(Machine machine) {
        int words = machine.frameLocal(0);
        boolean done = machine.extendHeap(Machine.value(words));
        machine.pushStack(done ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_flush(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.flush(channel);
        machine.pushStack(channel);
        machine.popFrame();
    }

    public static void Kernel_floor(Machine machine) {
        int f = machine.frameLocal(0);
        if (machine.isFloat(f)) {
            machine.pushStack(Machine.mkInt((int) Math.floor(Double.parseDouble(machine.valueToString(f)))));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_floor expects a float " + machine.valueToString(f));
    }

    public static void Kernel_fork(Machine machine) {
        int value = machine.frameLocal(0);
        int fun = machine.frameLocal(1);
        if (machine.funArity(fun) == 0) {
            // Push a dummy value for now....
            machine.pushStack(Machine.undefinedValue);
            machine.popFrame();
            int thread = machine.fork(value, fun);
            // Pop the dummy value...
            machine.popStack();
            machine.pushStack(thread);
        } else
            error(ARGCOUNT, machine, "Fork expects a 0-arity function.");
    }

    public static void Kernel_forwardRefs(Machine machine) {
        machine.pushStack(machine.forwardRefs());
        machine.popFrame();
    }

    public static void Kernel_forName(Machine machine) {
        int name = machine.frameLocal(0);
        int paths = machine.frameLocal(1);
        String[] strings = new String[machine.consLength(paths)];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = machine.valueToString(machine.consHead(paths));
            paths = machine.consTail(paths);
        }
        int object = XJ.forName(machine, strings, machine.valueToString(name));
        if (object == -1)
            machine.pushStack(Machine.undefinedValue);
        else
            machine.pushStack(object);
        machine.popFrame();
    }

    public static void Kernel_forName2(Machine machine) {
        String name = machine.valueToString(machine.frameLocal(0));
        String path = machine.valueToString(machine.frameLocal(1));
        try {
            int object = AlienObject.forName(machine, path, name);
            machine.pushStack(object);
        } catch (ClassNotFoundException cnfe) {
            machine.pushStack(Machine.undefinedValue);
        }
        machine.popFrame();
    }

    public static void Kernel_fileReadOnly(Machine machine) {
        int string = machine.frameLocal(0);
        String fileName = machine.valueToString(string);
        File file = new File(fileName);
        if (file.exists()) {
            if (!file.canWrite())
                machine.pushStack(Machine.trueValue);
            else
                machine.pushStack(Machine.falseValue);
        } else {
            machine.pushStack(Machine.falseValue);
        }
        machine.popFrame();
    }

    public static void Kernel_fileExists(Machine machine) {
        int string = machine.frameLocal(0);
        String fileName = machine.valueToString(string);
        File file = new File(fileName);
        machine.pushStack(file.exists() ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_fileSize(Machine machine) {
        int string = machine.frameLocal(0);
        String fileName = machine.valueToString(string);
        File file = new File(fileName);
        machine.pushStack(file.exists() ? Machine.mkInt((int) file.length()) : Machine.mkInt(-1));
        machine.popFrame();
    }

    public static void Kernel_forwardRefPath(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        machine.pushStack(machine.forwardRefPath(forwardRef));
        machine.popFrame();
    }

    public static void Kernel_forwardRefSetPath(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        int path = machine.frameLocal(1);
        machine.forwardRefSetPath(forwardRef, path);
        machine.pushStack(forwardRef);
        machine.popFrame();
    }

    public static void Kernel_forwardRefValue(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        machine.pushStack(machine.forwardRefValue(forwardRef));
        machine.popFrame();
    }

    public static void Kernel_forwardRefSetValue(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.forwardRefSetValue(forwardRef, value);
        machine.pushStack(forwardRef);
        machine.popFrame();
    }

    public static void Kernel_forwardRefListeners(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        machine.pushStack(machine.forwardRefListeners(forwardRef));
        machine.popFrame();
    }

    public static void Kernel_forwardRefSetListeners(Machine machine) {
        int forwardRef = machine.frameLocal(0);
        int listeners = machine.frameLocal(1);
        machine.forwardRefSetListeners(forwardRef, listeners);
        machine.pushStack(forwardRef);
        machine.popFrame();
    }

    public static void Kernel_fileInputChannel(Machine machine) {
        int string = machine.frameLocal(0);
        String fileName = machine.valueToString(string);
        machine.pushStack(machine.mkFileInputChannel(fileName));
        machine.popFrame();
    }

    public static void Kernel_fileOutputChannel(Machine machine) {
        int string = machine.frameLocal(0);
        String fileName = machine.valueToString(string);
        machine.pushStack(machine.mkFileOutputChannel(fileName));
        machine.popFrame();
    }

    public static void Kernel_funArity(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(machine.funArity(fun)));
        machine.popFrame();
    }

    public static void Kernel_funSig(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funSig(fun));
        machine.popFrame();
    }

    public static void Kernel_funArgs(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funSig(fun));
        machine.popFrame();
    }

    public static void Kernel_funCodeBox(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funCode(fun));
        machine.popFrame();
    }

    public static void Kernel_funDoc(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funDocumentation(fun));
        machine.popFrame();
    }

    public static void Kernel_funDynamics(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funDynamics(fun));
        machine.popFrame();
    }

    public static void Kernel_funGlobals(Machine machine) {
        int fun = machine.frameLocal(0);
        int globals = machine.funGlobals(fun);
        machine.pushStack(globals);
        machine.popFrame();
    }

    public static void Kernel_funIsVarArgs(Machine machine) {
        int fun = machine.frameLocal(0);
        int isVarArgs = machine.funIsVarArgs(fun);
        machine.pushStack(isVarArgs);
        machine.popFrame();
    }

    public static void Kernel_funName(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funName(fun));
        machine.popFrame();
    }

    public static void Kernel_funOwner(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funOwner(fun));
        machine.popFrame();
    }

    public static void Kernel_funProperties(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funProperties(fun));
        machine.popFrame();
    }

    public static void Kernel_funSelf(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funSelf(fun));
        machine.popFrame();
    }

    public static void Kernel_funSetArity(Machine machine) {
        int fun = machine.frameLocal(0);
        int arity = machine.frameLocal(1);
        machine.funSetArity(fun, arity);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetSig(Machine machine) {
        int fun = machine.frameLocal(0);
        int args = machine.frameLocal(1);
        machine.funSetSig(fun, args);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetArgs(Machine machine) {
        int fun = machine.frameLocal(0);
        int args = machine.frameLocal(1);
        machine.funSetSig(fun, args);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetCodeBox(Machine machine) {
        int fun = machine.frameLocal(0);
        int codeBox = machine.frameLocal(1);
        machine.funSetCode(fun, codeBox);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetDynamics(Machine machine) {
        int fun = machine.frameLocal(0);
        int dynamics = machine.frameLocal(1);
        machine.funSetDynamics(fun, dynamics);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetName(Machine machine) {
        int fun = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        machine.funSetName(fun, name);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetOwner(Machine machine) {
        int fun = machine.frameLocal(0);
        int owner = machine.frameLocal(1);
        machine.funSetOwner(fun, owner);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetProperties(Machine machine) {
        int fun = machine.frameLocal(0);
        int properties = machine.frameLocal(1);
        machine.funSetProperties(fun, properties);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetSelf(Machine machine) {
        int fun = machine.frameLocal(0);
        int self = machine.frameLocal(1);
        machine.funSetSelf(fun, self);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetSupers(Machine machine) {
        int fun = machine.frameLocal(0);
        int supers = machine.frameLocal(1);
        machine.funSetSupers(fun, supers);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetDoc(Machine machine) {
        int fun = machine.frameLocal(0);
        int doc = machine.frameLocal(1);
        machine.funSetDocumentation(fun, doc);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetGlobals(Machine machine) {
        int fun = machine.frameLocal(0);
        int glob = machine.frameLocal(1);
        machine.funSetGlobals(fun, glob);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSetIsVarArgs(Machine machine) {
        int fun = machine.frameLocal(0);
        int isVarArgs = machine.frameLocal(1);
        machine.funSetIsVarArgs(fun, isVarArgs);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funTraced(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funTraced(fun));
        machine.popFrame();
    }

    public static void Kernel_funSetTraced(Machine machine) {
        int fun = machine.frameLocal(0);
        int traced = machine.frameLocal(1);
        machine.funSetTraced(fun, traced);
        machine.pushStack(fun);
        machine.popFrame();
    }

    public static void Kernel_funSupers(Machine machine) {
        int fun = machine.frameLocal(0);
        machine.pushStack(machine.funSupers(fun));
        machine.popFrame();
    }

    public static void Kernel_getForeignSlot(Machine machine) {
        int object = machine.frameLocal(0);
        int symbol = machine.frameLocal(1);
        String name = machine.valueToString(Machine.isSymbol(symbol) ? machine.symbolName(symbol) : symbol);
        int value = XJ.getSlot(machine, machine.foreignObj(Machine.value(object)), name);
        if (value == -1)
            error(MISSINGSLOT, machine, "Machine.dot: object " + machine.valueToString(object) + " has no att " + name);
        else {
            machine.pushStack(value);
            machine.popFrame();
        }
    }

    public static void Kernel_getLastModified(Machine machine) {
        int f = machine.frameLocal(0);
        String file = machine.valueToString(f);
        File jfile = new File(file);
        long time = jfile.lastModified();
        machine.pushStack(machineTime(machine, time, machine.time));
        machine.popFrame();
    }

    public static void Kernel_getNonReferencedElements(Machine machine) {
        int elements = machine.frameLocal(0);
        int nameSpaces = machine.frameLocal(1);
        if (!Machine.isCons(elements) && elements != Machine.nilValue)
            error(TYPE, machine, "Kernel_getNonReferencedElements expects a sequence of values.");
        else if (!Machine.isCons(nameSpaces) && nameSpaces != Machine.nilValue)
            error(TYPE, machine, "Kernel_getNonReferencedElements expects a sequence of name spaces.");
        else {
            elements = machine.nonReferencedElements(elements, nameSpaces);
            machine.pushStack(elements);
            machine.popFrame();
        }
    }

    public static void Kernel_getSlotValue(Machine machine) {
        int object = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        if (!machine.dotAlienObj(name, object)) {
            name = machine.isSymbol(name) ? name : machine.mkSymbol(name);
            int att = machine.objAttribute(object, name);
            if (att == -1) {
                machine.pushStack(Machine.undefinedValue);
                machine.popFrame();
                machine.popStack();
                machine.sendSlotMissing(object, name);
            } else {
                machine.pushStack(machine.attributeValue(att));
                machine.popFrame();
            }
        } else {
            machine.popFrame();
        }

    }

    public static void Kernel_getVar(Machine machine) {

        // Get the value of a dynamic variable. Throw an error
        // if there is no variable in scope.

        int name = machine.mkSymbol(machine.frameLocal(0));
        int value = machine.dynamicValue(name);
        if (value != -1) {
            machine.pushStack(value);
            machine.popFrame();
        } else
            error(UNBOUNDVAR, machine, "Kernel_getVar: no dynamic named " + machine.valueToString(name));
    }

    public static void Kernel_graphLayout(Machine machine) {
        int x1 = machine.frameLocal(0);
        int y1 = machine.frameLocal(1);
        int x2 = machine.frameLocal(2);
        int y2 = machine.frameLocal(3);
        int v = machine.frameLocal(4);
        int k = machine.frameLocal(5);
        int isGreater = machine.frameLocal(6);

        int dx1 = x1 - x2;
        int dx2 = x2 - x1;
        int dy1 = y1 - y2;
        int dy2 = y1 - y1;

        double x1r = (double) dx1 / ((double) Math.abs(dx1) + Math.abs(dy1));
        double x2r = (double) dx2 / ((double) Math.abs(dx2) + Math.abs(dy2));
        double y1r = (double) dy1 / ((double) Math.abs(dx1) + Math.abs(dy1));
        double y2r = (double) dy2 / ((double) Math.abs(dx2) + Math.abs(dy2));

        double dist = Math.sqrt((dx1 * dx1) + (dy1 * dy1));
        double delta = 3.0;

        int modx1 = (int) (x1r * delta);
        int mody1 = (int) (y1r * delta);
        int modx2 = (int) (x2r * delta);
        int mody2 = (int) (y2r * delta);

        boolean doit = false;
        if (isGreater == Machine.trueValue)
            doit = (int) dist > Machine.value(k);
        else
            doit = (int) dist < Machine.value(k);

        if (doit) {
            machine.arraySet(v, 0, Machine.mkInt(modx1));
            machine.arraySet(v, 1, Machine.mkInt(mody1));
            machine.arraySet(v, 2, Machine.mkInt(modx2));
            machine.arraySet(v, 3, Machine.mkInt(mody2));
            machine.pushStack(Machine.trueValue);
        } else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_gzipInputChannel(Machine machine) {
        int in = machine.frameLocal(0);
        machine.pushStack(machine.mkGZipInputChannel(in));
        machine.popFrame();
    }

    public static void Kernel_gzipOutputChannel(Machine machine) {
        int out = machine.frameLocal(0);
        machine.pushStack(machine.mkGZipOutputChannel(out));
        machine.popFrame();
    }

    public static void Kernel_hasSlot(Machine machine) {
        // Return true when the object has the given slot.

        int obj = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        if (Machine.isString(name))
            name = machine.mkSymbol(name);
        if ((Machine.isObj(obj) || Machine.isAlienObj(obj)) && machine.objHasAtt(obj, name))
            machine.pushStack(Machine.trueValue);
        else if (Machine.isForeignObj(obj)
                && XJ.hasSlot(machine.foreignObj(Machine.value(obj)), machine.valueToString(name)))
            machine.pushStack(Machine.trueValue);
        else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_hasForeignSlot(Machine machine) {
        int object = machine.frameLocal(0);
        int symbol = machine.frameLocal(1);
        String name = machine.valueToString(Machine.isSymbol(symbol) ? machine.symbolName(symbol) : symbol);
        machine.pushStack(XJ.hasSlot(machine.foreignObj(Machine.value(object)), name) ? Machine.trueValue
                : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_hasVar(Machine machine) {
        // Return 'true' if there is a dynamic with the given name
        // otherwise return 'false'.
        int name = machine.mkSymbol(machine.frameLocal(0));
        int dynamic = machine.dynamicValue(name);
        if (dynamic != -1)
            machine.pushStack(Machine.trueValue);
        else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_hashCode(Machine machine) {
        // Return the hash code for any element.
        int element = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(machine.hashCode(element)));
        machine.popFrame();
    }

    public static void Kernel_heapSize(Machine machine) {
        machine.pushStack(Machine.mkInt(machine.heapSize));
        machine.popFrame();
    }

    public static void Kernel_import(Machine machine) {
        int table = machine.frameLocal(0);
        machine.pushStack(table);
        machine.popFrame();
        machine.importTable(table);
    }

    public static void Kernel_imports(Machine machine) {
        machine.pushStack(machine.frameDynamics());
        machine.popFrame();
    }

    public static void Kernel_indexOf(Machine machine) {
    	int st = machine.frameLocal(0);
    	int val = machine.frameLocal(1);
    	int in = machine.frameLocal(2);
        String string = machine.valueToString(st);
        String value = machine.valueToString(val);
        int index = machine.intValue(in);
        int result = machine.mkInt(string.indexOf(value,index));
        machine.pushStack(result);
        machine.popFrame();
    }
    
    public static void Kernel_invoke(Machine machine) {

        // Get the arguments supplied to Kernel_invoke and
        // calculate the number of supplied arguments. NB
        // all var args modifications must have taken place.

        int fun = machine.frameLocal(0);
        int target = machine.frameLocal(1);
        int args = machine.frameLocal(2);
        int supers = machine.frameLocal(3);
        int arity = machine.consLength(args);

        // Fix up the stack frame ready for calling the supplied
        // function. The Kernel_invoke stack frame is discarded and
        // a new frame for 'fun' is opened.

        machine.pushStack(target);
        machine.popFrame();
        machine.popStack();
        machine.openFrame();

        // Push the arguments into the call frame ...

        while (args != Machine.nilValue) {
            machine.pushStack(machine.consHead(args));
            args = machine.consTail(args);
        }

        // The following should really be handled by Machine.
        // The factoring is not quite right, so handle it here for now...

        switch (Machine.tag(fun)) {
        case FUN:
            if (machine.funTraced(fun) != Machine.undefinedValue)
                machine.enterTracedFun(fun, machine.funArity(fun), target, supers);
            else
                machine.enterFun(fun, arity, target, supers);
            break;
        case FOREIGNFUN:
            machine.enterForeignFun(fun, arity);
            break;
        case OBJ:
            machine.enterObj(fun, arity);
            break;
        default:
            machine.error(TYPE, "Trying to apply a non-applicable value.");
        }
    }

    public static void Kernel_isDir(Machine machine) {
        String file = machine.valueToString(machine.frameLocal(0));
        File f = new File(file);
        boolean isDir = f.isDirectory();
        machine.pushStack(isDir ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_isOlder(Machine machine) {
        String oldName = machine.valueToString(machine.frameLocal(0));
        String newName = machine.valueToString(machine.frameLocal(1));
        File oldFile = new File(oldName);
        File newFile = new File(newName);
        if (oldFile.exists() && newFile.exists())
            machine.pushStack(oldFile.lastModified() < newFile.lastModified() ? Machine.trueValue : Machine.falseValue);
        else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }
    
    public static void Kernel_lastIndexOf(Machine machine) {
    	int st = machine.frameLocal(0);
    	int val = machine.frameLocal(1);
    	int in = machine.frameLocal(2);
        String string = machine.valueToString(st);
        String value = machine.valueToString(val);
        int index = machine.intValue(in);
        int result = machine.mkInt(string.lastIndexOf(value,index));
        machine.pushStack(result);
        machine.popFrame();
    }

    public static void Kernel_letVar(Machine machine) {
        // Sets the value of a dynamic variable in the current
        // stack frame. Assumes the name is supplied as a symbol.
        int name = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.pushStack(value);
        machine.popFrame();
        machine.bindDynamic(name, value);
    }

    public static void Kernel_load(Machine machine) {
        try {
            int source = machine.frameLocal(0);
            if (Machine.isString(source))
                machine.pushStack(machine.loadValue(machine.valueToString(source)));
            else if (machine.isInputChannel(source))
                machine.pushStack(machine.loadValue(machine.inputChannel(Machine.value(source))));
            else
                error(TYPE, machine, "Kernel_load: expecting a filename or an input channel: "
                        + machine.valueToString(source));
            machine.popFrame();
        } catch (MachineError machineError) {
            error(machineError.error, machine, machineError.getMessage());
        }
    }

    public static void Kernel_load3_tmp(Machine machine) {
        String file = machine.valueToString(machine.frameLocal(0));
        Serializer s = new Serializer(machine);
        machine.popFrame();
        machine.popStack();
        machine.pushStack(s.load(file));
    }

    public static void Kernel_local(Machine machine) {
        int index = machine.frameLocal(0);
        machine.popFrame();
        machine.popStack();
        machine.pushStack(machine.frameLocal(Machine.value(index)));
    }

    public static void Kernel_loadbin(Machine machine) {
        // Load a binary file. A binary file is loaded into a single
        // code box. The code box is transformed into a 0 arity function
        // which is then called.
        int source = machine.frameLocal(0);
        int fun = -1;
        if (Machine.isString(source)) {
            String name = machine.basicStringToString(source);
            fun = machine.loadBin(name);
        } else if (machine.isInputChannel(source)) {
            fun = machine.loadBin(machine.inputChannel(source));
        }
        machine.popFrame();
        machine.popStack();
        machine.openFrame();
        machine.enterFun(fun, 0);
        machine.pushStack(source);
    }

    public static void Kernel_lsh(Machine machine) {
        int i = Machine.value(machine.frameLocal(0));
        int bits = Machine.value(machine.frameLocal(1));
        machine.pushStack(Machine.mkInt(i << bits));
        machine.popFrame();
    }

    public static void Kernel_memory(Machine machine) {
        machine.memory.setTotalUsed(machine.usedHeap());
        machine.pushStack(machine.memory.data(machine));
        machine.popFrame();
    }

    public static void Kernel_lowerCase(Machine machine) {
        int string = machine.frameLocal(0);
        String str = machine.valueToString(string);
        machine.pushStack(machine.mkString(str.toLowerCase()));
        machine.popFrame();
    }

    public static void Kernel_needsGC(Machine machine) {
        machine.popFrame();
        machine.pushStack(machine.needsGC() ? Machine.trueValue : Machine.falseValue);
    }

    public static void Kernel_nextToken(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.pushStack(machine.nextToken(channel));
        machine.popFrame();
    }

    public static void Kernel_mk24bit(Machine machine) {
        int high = Machine.value(machine.frameLocal(0));
        int med = Machine.value(machine.frameLocal(1));
        int low = Machine.value(machine.frameLocal(2));
        machine.pushStack(Machine.mkInt(Machine.mkWord(0, high, med, low)));
        machine.popFrame();
    }

    public static void Kernel_mkCodeBox(Machine machine) {
        int locals = machine.frameLocal(0);
        machine.pushStack(machine.mkCodeBox(Machine.value(locals)));
        machine.popFrame();
    }

    public static void Kernel_mkArray(Machine machine) {
        int length = machine.frameLocal(0);
        machine.pushStack(machine.mkArray(Machine.value(length)));
        machine.popFrame();
    }

    public static void Kernel_mkBasicTokenChannel(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.pushStack(machine.mkTokenInputChannel(channel));
        machine.popFrame();
    }

    public static void Kernel_mkBuffer(Machine machine) {
        int increment = machine.frameLocal(0);
        machine.pushStack(machine.mkBuffer(Machine.value(increment)));
        machine.popFrame();
    }

    public static void Kernel_mkCode(Machine machine) {
        int length = machine.frameLocal(0);
        machine.pushStack(machine.mkCode(Machine.value(length)));
        machine.popFrame();
    }

    public static void Kernel_mkDataOutputChannel(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.pushStack(machine.mkDataOutputChannel(channel));
        machine.popFrame();
    }

    public static void Kernel_mkDataInputChannel(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.pushStack(machine.mkDataInputChannel(channel));
        machine.popFrame();
    }

    public static void Kernel_mkDaemon(Machine machine) {
        machine.pushStack(machine.mkDaemon());
        machine.popFrame();
    }

    public static void Kernel_mkDir(Machine machine) {
        int path = machine.frameLocal(0);
        String pathString = machine.valueToString(path);
        File file = new File(pathString);
        if (!file.exists())
            machine.pushStack(file.mkdir() ? Machine.trueValue : Machine.falseValue);
        else
            machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_mkFloat(Machine machine) {
        int prePoint = machine.frameLocal(0);
        int postPoint = machine.frameLocal(1);
        String pre = machine.valueToString(prePoint);
        String post = machine.valueToString(postPoint);
        machine.pushStack(machine.mkFloat(pre + "." + post));
        machine.popFrame();
    }

    public static void Kernel_mkFun(Machine machine) {
        machine.pushStack(machine.mkFun());
        machine.popFrame();
    }

    public static void Kernel_mkString(Machine machine) {
        int size = machine.frameLocal(0);
        machine.pushStack(machine.mkString(Machine.value(size)));
        machine.popFrame();
    }

    public static void Kernel_mkSymbol(Machine machine) {
        int name = machine.frameLocal(0);
        machine.pushStack(machine.mkSymbol(name));
        machine.popFrame();
    }

    public static void Kernel_mkTable(Machine machine) {
        int size = machine.frameLocal(0);
        machine.pushStack(machine.mkHashtable(Machine.value(size)));
        machine.popFrame();
    }

    public static void Kernel_mkObj(Machine machine) {
        // Create a new empty object.
        if (machine.needsGC())
            machine.gc();
        machine.pushStack(machine.mkObj());
        machine.popFrame();
    }

    public static void Kernel_mod(Machine machine) {
        int n = Machine.value(machine.frameLocal(0));
        int m = Machine.value(machine.frameLocal(1));
        machine.pushStack(Machine.mkInt(n % m));
        machine.popFrame();
    }

    public static void Kernel_newListeners(Machine machine) {
        machine.pushStack(machine.newListeners());
        machine.popFrame();
    }

    public static void Kernel_newObj(Machine machine) {
        int type = machine.frameLocal(0);
        machine.pushStack(machine.mkObj(type));
        machine.popFrame();
    }

    public static void Kernel_objDaemonsActive(Machine machine) {
        // Return whether the daemons of the receiver are active.
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.objDaemonsActive(obj));
        machine.popFrame();
    }

    public static void Kernel_objSetDaemonsActive(Machine machine) {
        // Set the active state of the daemons and return the obj.
        int obj = machine.frameLocal(0);
        int active = machine.frameLocal(1);
        machine.objSetDaemonsActive(obj, active);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_objIsSaveAsLookup(Machine machine) {
        // Return whether the object is a named element....
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.isSaveAsLookup(obj) ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_objSetSaveAsLookup(Machine machine) {
        // Set whether the machine thinks the object is named...
        int obj = machine.frameLocal(0);
        int isSaveAsLookup = machine.frameLocal(1);
        if (Machine.isObj(obj))
            machine.objSetSaveAsLookup(obj, isSaveAsLookup == Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_objIsNotVMNew(Machine machine) {
        // Return whether the class is instantiable in the VM....
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.isNotVMNew(obj) ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_objSetNotVMNew(Machine machine) {
        // Set whether the class is instantiable in the VM....
        int obj = machine.frameLocal(0);
        int isSaveAsLookup = machine.frameLocal(1);
        if (Machine.isObj(obj))
            machine.objSetNotVMNew(obj, isSaveAsLookup == Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_objSlots(Machine machine) {
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.objAttributes(obj));
        machine.popFrame();
    }

    public static void Kernel_objHotLoad(Machine machine) {
        // Return whether the object will run an operation when it is loaded.
        int obj = machine.frameLocal(0);
        machine.pushStack(machine.objHotLoad(obj));
        machine.popFrame();
    }

    public static void Kernel_objSetHotLoad(Machine machine) {
        // Set the hot load status of the object.
        int obj = machine.frameLocal(0);
        int hotLoad = machine.frameLocal(1);
        machine.objSetHotLoad(obj, hotLoad);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_of(Machine machine) {
        // Return the classifier of a value.
        machine.pushStack(machine.type(machine.frameLocal(0)));
        machine.popFrame();
    }

    public static void Kernel_operationCodeBox(Machine machine) {
        int op = machine.frameLocal(0);
        machine.pushStack(machine.funCode(op));
        machine.popFrame();
    }

    public static void Kernel_operatorPrecedenceList(Machine machine) {
        int classifier = machine.frameLocal(0);
        machine.pushStack(machine.operatorPrecedenceList(classifier));
        machine.popFrame();
    }

    public static void Kernel_peek(Machine machine) {
        int channel = machine.frameLocal(0);
        machine.pushStack(machine.peek(channel));
        machine.popFrame();
    }

    public static void Kernel_pow(Machine machine) {
        int n = machine.frameLocal(0);
        int m = machine.frameLocal(1);
        if (machine.isFloat(n) && machine.isFloat(m)) {
        	double nn = Double.parseDouble(machine.valueToString(n));
        	double mm = Double.parseDouble(machine.valueToString(m));
            machine.pushStack(machine.mkFloat(Math.pow(nn,mm)));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_pow expects two float arguments " + machine.valueToString(n) + "," + machine.valueToString(m));
    }
    
    public static void Kernel_readString(Machine machine) {
        int inputChannel = machine.frameLocal(0);
        machine.pushStack(machine.readString(inputChannel));
        machine.popFrame();
    }

    public static void Kernel_readVector(Machine machine) {
        int inputChannel = machine.frameLocal(0);
        int vector = machine.frameLocal(1);
        machine.pushStack(machine.readVector(inputChannel, vector));
        machine.popFrame();
    }

    public static void Kernel_readXML(Machine machine) {
        machine.gc();
        int source = machine.frameLocal(0);
        if (Machine.isString(source))
            machine.pushStack(XMLReader.parse(machine.valueToString(source), machine));
        else if (machine.isInputChannel(source)) {
            machine.pushStack(XMLReader.parse(machine.inputChannel(Machine.value(source)), machine));
        } else
            error(TYPE, machine, "Kernel_realXML expects a string or an input channel: "
                    + machine.valueToString(source));
        machine.popFrame();
    }

    public static void Kernel_rebindStdin(Machine machine) {
        int in = machine.frameLocal(0);
        machine.rebindStdin(in);
        machine.pushStack(in);
        machine.popFrame();
    }

    public static void Kernel_rebindStdout(Machine machine) {
        int out = machine.frameLocal(0);
        machine.rebindStdout(out);
        machine.pushStack(out);
        machine.popFrame();
    }

    public static void Kernel_redoStackSize(Machine machine) {
        machine.pushStack(Machine.mkInt(machine.undo.redoStackSize()));
        machine.popFrame();
    }

    public static void Kernel_removeAtt(Machine machine) {
        int obj = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        machine.objRemoveAttribute(obj, name);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_renameFile(Machine machine) {
        int newName = machine.frameLocal(0);
        int oldName = machine.frameLocal(1);
        File newFile = new File(machine.valueToString(newName));
        File oldFile = new File(machine.valueToString(oldName));
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
            machine.pushStack(Machine.trueValue);
        } else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_resetSaveLoad(Machine machine) {
        machine.resetSaveLoad();
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_resetToInitialState(Machine machine) {
        int in = machine.frameLocal(0);
        machine.resetToInitialState(in);
        machine.pushStack(in);
        machine.popFrame();
    }

    public static void Kernel_random(Machine machine) {
        Random r = new Random();
        machine.pushStack(machine.mkFloat(r.nextFloat()));
        machine.popFrame();
    }

    public static void Kernel_round(Machine machine) {
        int f = machine.frameLocal(0);
        if (machine.isFloat(f)) {
            machine.pushStack(Machine.mkInt(Math.round(Float.parseFloat(machine.valueToString(f)))));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_round expects a float " + machine.valueToString(f));
    }

    public static void Kernel_rsh(Machine machine) {
        int i = Machine.value(machine.frameLocal(0));
        int bits = Machine.value(machine.frameLocal(1));
        machine.pushStack(Machine.mkInt(i >> bits));
        machine.popFrame();
    }

    public static void Kernel_save(Machine machine) {
        int value = machine.frameLocal(0);
        int sink = machine.frameLocal(1);
        if (Machine.isString(sink))
            machine.save(value, machine.valueToString(sink));
        else if (machine.isOutputChannel(sink))
            machine.save(value, machine.outputChannel(Machine.value(sink)));
        else
            error(TYPE, machine, "Machine_save expects a filename or an output channel: " + machine.valueToString(sink));
        machine.pushStack(value);
        machine.popFrame();
    }

    public static void Kernel_save2(Machine machine) {
        int value = machine.frameLocal(0);
        int sink = machine.frameLocal(1);
        int nameSpaces = machine.frameLocal(2);
        if (!Machine.isCons(nameSpaces) && nameSpaces != Machine.nilValue)
            error(TYPE, machine, "Machine_save2 expects a sequence of name spaces.");
        else {
            if (Machine.isString(sink)) {
                String result = machine.save(value, machine.valueToString(sink), nameSpaces);
                if (result == null) {
                    machine.pushStack(value);
                    machine.popFrame();
                } else error(SAVEERR,machine,result);
            } else if (machine.isOutputChannel(sink)) {
                String result = machine.save(value, machine.outputChannel(Machine.value(sink)), nameSpaces);
                if (result == null) {
                    machine.pushStack(value);
                    machine.popFrame();
                } else error(SAVEERR,machine,result);
            } else
                error(TYPE, machine, "Machine_save expects a filename or an output channel: "
                        + machine.valueToString(sink));
        }
    }

    public static void Kernel_save3_tmp(Machine machine) {
        int value = machine.frameLocal(0);
        String file = machine.valueToString(machine.frameLocal(1));
        int nameSpaces = machine.frameLocal(2);
        Serializer s = new Serializer(machine);
        s.setNameSpaces(nameSpaces);
        s.save(value, file);
        machine.pushStack(value);
        machine.popFrame();
    }

    public static void Kernel_saxInputChannel(Machine machine) {
        int in = machine.frameLocal(0);
        machine.pushStack(machine.mkSAXInputChannel(in));
        machine.popFrame();
    }

    public static void Kernel_sendForeignInstance(Machine machine) {
        Object target = machine.foreignObj(Machine.value(machine.frameLocal(0)));
        int messageValue = machine.frameLocal(1);
        String message = "<undefined>";
        if (machine.isString(messageValue))
            message = machine.valueToString(messageValue);
        if (machine.isSymbol(messageValue))
            message = machine.valueToString(machine.symbolName(messageValue));
        int args = machine.frameLocal(2);
        int value = XJ.send(machine, target, message, args);
        if (value == -1)
            error(TYPE, machine, XJ.error);
        else {
            machine.pushStack(value);
            machine.popFrame();
        }
    }

    public static void Kernel_setCharAt(Machine machine) {
        int string = machine.frameLocal(0);
        int index = machine.frameLocal(1);
        int value = machine.frameLocal(2);
        machine.stringSet(string, Machine.value(index), Machine.value(value));
        machine.pushStack(string);
        machine.popFrame();
    }

    public static void Kernel_setConstructorArgs(Machine machine) {
        int klass = machine.frameLocal(0);
        int args = machine.frameLocal(1);
        machine.setConstructorArgs(klass, args);
        machine.pushStack(klass);
        machine.popFrame();
    }

    public static void Kernel_setDefaultGetMOP(Machine machine) {
        int obj = machine.frameLocal(0);
        int bool = machine.frameLocal(1);
        machine.objSetDefaultGetMOP(obj, bool == Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_setDefaultSetMOP(Machine machine) {
        int obj = machine.frameLocal(0);
        int bool = machine.frameLocal(1);
        machine.objSetDefaultSetMOP(obj, bool == Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_setDefaultSendMOP(Machine machine) {
        int obj = machine.frameLocal(0);
        int bool = machine.frameLocal(1);
        machine.objSetDefaultSendMOP(obj, bool == Machine.trueValue);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_setFileLastModified(Machine machine) {
        File file = new File(machine.valueToString(machine.frameLocal(0)));
        int hours = Machine.value(machine.frameLocal(1));
        int mins = Machine.value(machine.frameLocal(2));
        int secs = Machine.value(machine.frameLocal(3));
        int millis = Machine.value(machine.frameLocal(4));
        long time = machine.time + (hours * 60 * 60 * 1000) + (mins * 60 * 1000) + (secs * 1000) + millis;
        if (file.exists())
            machine.pushStack(Machine.mkBool(file.setLastModified(time)));
        else
            machine.pushStack(Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_setDaemons(Machine machine) {
        int obj = machine.frameLocal(0);
        int daemons = machine.frameLocal(1);
        machine.objSetDaemons(obj, daemons);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_setForeignSlot(Machine machine) {
        int object = machine.frameLocal(0);
        int symbol = machine.frameLocal(1);
        String name = machine.valueToString(Machine.isSymbol(symbol) ? machine.symbolName(symbol) : symbol);
        int value = machine.frameLocal(2);
        int success = XJ.setSlot(machine, machine.foreignObj(Machine.value(object)), name, value);
        if (success == -1)
            error(MISSINGSLOT, machine, "Machine.dot: object " + machine.valueToString(object) + " has no att " + name);
        else {
            machine.pushStack(value);
            machine.popFrame();
        }
    }

    public static void Kernel_setForwardRefs(Machine machine) {
        int refs = machine.frameLocal(0);
        machine.setForwardRefs(refs);
        machine.pushStack(refs);
        machine.popFrame();
    }

    public static void Kernel_setGlobal(Machine machine) {
        // Part of the implementation of letrec. Sets a global in a function.
        int fun = machine.frameLocal(0);
        int index = machine.frameLocal(1);
        int value = machine.frameLocal(2);
        if (Machine.isFun(fun))
            if (Machine.isInt(index)) {
                int globals = machine.funGlobals(fun);
                int array = machine.globalsArray(globals);
                machine.arraySet(array, Machine.value(index), value);
                machine.pushStack(value);
                machine.popFrame();
            } else
                error(TYPE, machine, "$setglobal: expecting an int index: " + machine.valueToString(index));
        else
            error(TYPE, machine, "$setglobal: expecting a fun: " + machine.valueToString(fun));
    }

    public static void Kernel_setOf(Machine machine) {
        // Set the classifier of an object.
        int obj = machine.frameLocal(0);
        int classifier = machine.frameLocal(1);
        machine.objSetType(obj, classifier);
        machine.pushStack(obj);
        machine.popFrame();
    }

    public static void Kernel_setSlotValue(Machine machine) {
        int object = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        int value = machine.frameLocal(2);
        machine.pushStack(Machine.undefinedValue);
        machine.popFrame();
        machine.popStack();
        if (Machine.isObj(object))
            machine.setObjSlot(object, machine.isSymbol(name) ? name : machine.mkSymbol(name), value);
        else
            error(TYPE, machine, "Kernel_setSlotValue, non-object: " + machine.valueToString(object));
    }

    public static void Kernel_setUndoSize(Machine machine) {
        int size = machine.frameLocal(0);
        machine.undo.setUndoSize(Machine.value(size));
        machine.pushStack(size);
        machine.popFrame();
    }

    public static void Kernel_setTypes(Machine machine) {
        // Set the basic data types in the machine.
        machine.theTypeBoolean = machine.frameLocal(0);
        machine.theClassBind = machine.frameLocal(1);
        machine.theClassBuffer = machine.frameLocal(2);
        machine.theClassCodeBox = machine.frameLocal(3);
        machine.theClassCompiledOperation = machine.frameLocal(4);
        machine.theClassElement = machine.frameLocal(5);
        machine.theClassForeignObject = machine.frameLocal(6);
        machine.theClassForeignOperation = machine.frameLocal(7);
        machine.theClassForwardRef = machine.frameLocal(8);
        machine.theClassException = machine.frameLocal(9);
        machine.theClassClass = machine.frameLocal(10);
        machine.theClassPackage = machine.frameLocal(11);
        machine.theClassDataType = machine.frameLocal(12);
        machine.theClassDaemon = machine.frameLocal(13);
        machine.theTypeInteger = machine.frameLocal(14);
        machine.theTypeFloat = machine.frameLocal(15);
        machine.theTypeString = machine.frameLocal(16);
        machine.theTypeSeqOfElement = machine.frameLocal(17);
        machine.theTypeSetOfElement = machine.frameLocal(18);
        machine.theTypeSeq = machine.frameLocal(19);
        machine.theTypeSet = machine.frameLocal(20);
        machine.theClassSymbol = machine.frameLocal(21);
        machine.theClassTable = machine.frameLocal(22);
        machine.theClassThread = machine.frameLocal(23);
        machine.theTypeNull = machine.frameLocal(24);
        machine.theClassVector = machine.frameLocal(25);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_sin(Machine machine) {
        int angle = machine.frameLocal(0);
        if (machine.isFloat(angle)) {
            float f = Float.parseFloat(machine.valueToString(angle));
            double radians = (Math.PI / 180) * f;
            machine.pushStack(machine.mkFloat(Math.sin(radians)));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_sin expects a float " + machine.valueToString(angle));
    }

    public static void Kernel_size(Machine machine) {
        int value = machine.frameLocal(0);
        switch (Machine.tag(value)) {
        case STRING:
            machine.pushStack(Machine.mkInt(machine.stringLength(value)));
            machine.popFrame();
            break;
        default:
            System.out.println("Kernel_size: " + machine.valueToString(value));
            machine.pushStack(value);
            machine.popFrame();
        }
    }

    public static void Kernel_slotNames(Machine machine) {
        int object = machine.frameLocal(0);
        int names = Machine.nilValue;
        if (Machine.isObj(object)) {
            int attributes = machine.objAttributes(object);
            while (attributes != Machine.nilValue) {
                int attribute = machine.consHead(attributes);
                attributes = machine.consTail(attributes);
                int symbol = machine.attributeName(attribute);
                names = machine.mkCons(machine.symbolName(symbol), names);
            }
        } else if (Machine.isForeignObj(object))
            names = XJ.slotNames(machine, machine.foreignObj(Machine.value(object)));
        machine.pushStack(machine.mkSet(names));
        machine.popFrame();
    }

    public static void Kernel_sortNamedElements(Machine machine) {
        int elements = machine.frameLocal(0);
        boolean casep = machine.frameLocal(1) == Machine.trueValue;
        machine.pushStack(machine.sortNamedElements(elements, casep));
        machine.popFrame();
    }

    public static void Kernel_sqrt(Machine machine) {
        int f = machine.frameLocal(0);
        if (machine.isFloat(f)) {
            machine.pushStack(machine.floatSqrt(f));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_sqrt expects a float " + machine.valueToString(f));
    }

    public static void Kernel_stackFrames(Machine machine) {
        machine.popFrame();
        machine.pushStack(machine.stackFrames());
    }

    public static void Kernel_startUndoContext(Machine machine) {
        machine.popFrame();
        machine.undo.startContext();
        machine.pushStack(Machine.trueValue);
    }

    public static void Kernel_endUndoContext(Machine machine) {
        machine.popFrame();
        machine.undo.endContext();
        machine.pushStack(Machine.trueValue);
    }

    public static void Kernel_stats(Machine machine) {
        machine.printStats(System.out);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_stringInputChannel(Machine machine) {
        int string = machine.frameLocal(0);
        machine.pushStack(machine.mkStringInputChannel(string));
        machine.popFrame();
    }
    
    public static void Kernel_subString(Machine machine) {
    	String string = machine.valueToString(machine.frameLocal(0));
    	int startIn = machine.intValue(machine.frameLocal(1));
    	int endIn = machine.intValue(machine.frameLocal(2));
    	String value = string.substring(startIn,endIn);
    	machine.pushStack(machine.mkString(value));
    	machine.popFrame();
    }

    public static void Kernel_symbolName(Machine machine) {
        int symbol = machine.frameLocal(0);
        if (machine.isSymbol(symbol)) {
            machine.pushStack(machine.symbolName(symbol));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_symbolName expects a symbol: " + machine.valueToString(symbol));
    }

    public static void Kernel_symbolSetName(Machine machine) {
        int symbol = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        if (machine.isSymbol(symbol) && Machine.isString(name)) {
            machine.symbolSetName(symbol, name);
            machine.pushStack(symbol);
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_symbolSetName expects a symbol and a string: " + machine.valueToString(symbol)
                    + " " + machine.valueToString(name));
    }

    public static void Kernel_symbolValue(Machine machine) {
        int symbol = machine.frameLocal(0);
        if (machine.isSymbol(symbol)) {
            machine.pushStack(machine.symbolValue(symbol));
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_symbolValue expects a symbol: " + machine.valueToString(symbol));
    }

    public static void Kernel_symbolSetValue(Machine machine) {
        int symbol = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        if (machine.isSymbol(symbol)) {
            machine.symbolSetValue(symbol, value);
            machine.pushStack(symbol);
            machine.popFrame();
        } else
            error(TYPE, machine, "Kernel_symbolSetValue expects a symbol: " + machine.valueToString(symbol));
    }

    public static void Kernel_tableRemove(Machine machine) {
        int table = machine.frameLocal(0);
        int key = machine.frameLocal(1);
        machine.pushStack(machine.hashTableRemove(table, key));
        machine.popFrame();
    }

    public static void Kernel_tableKeys(Machine machine) {
        int table = machine.frameLocal(0);
        machine.pushStack(machine.hashTableKeys(table));
        machine.popFrame();
    }

    public static void Kernel_tableHasKey(Machine machine) {
        int table = machine.frameLocal(0);
        int key = machine.frameLocal(1);
        machine.pushStack(machine.hashTableHasKey(table, key) ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_tableHasValue(Machine machine) {
        int table = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.pushStack(machine.hashTableHasValue(table, value) ? Machine.trueValue : Machine.falseValue);
        machine.popFrame();
    }

    public static void Kernel_tag(Machine machine) {
        int value = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(Machine.tag(value)));
        machine.popFrame();
    }

    public static void Kernel_tempFile(Machine machine) {
        String prefix = machine.valueToString(machine.frameLocal(0));
        String suffix = machine.valueToString(machine.frameLocal(1));
        int dir = machine.frameLocal(2);
        File file = (dir == Machine.undefinedValue) ? null : new File(machine.valueToString(dir));
        try {
            File tempFile = File.createTempFile(prefix, suffix, file);
            machine.pushStack(machine.mkString(tempFile.getAbsolutePath()));
        } catch (IOException ioe) {
            machine.pushStack(Machine.undefinedValue);
        }
        machine.popFrame();
    }

    public static void Kernel_tempDir(Machine machine) {
        String prefix = machine.valueToString(machine.frameLocal(0));
        int dir = machine.frameLocal(1);
        File file = (dir == Machine.undefinedValue) ? null : new File(machine.valueToString(dir));
        try {
            File tempFile = File.createTempFile(prefix, "", file);
            tempFile.delete();
            tempFile.mkdir();
            machine.pushStack(machine.mkString(tempFile.getAbsolutePath()));
        } catch (IOException ioe) {
            machine.pushStack(Machine.undefinedValue);
        }
        machine.popFrame();
    }

    public static void Kernel_thread(Machine machine) {
        machine.pushStack(machine.thread());
        machine.popFrame();
    }

    public static void Kernel_threadId(Machine machine) {
        int thread = machine.frameLocal(0);
        Thread t = machine.getThread(thread);
        if (t != null)
            machine.pushStack(Machine.mkInt(t.id()));
        else
            machine.pushStack(Machine.mkInt(-1));
        machine.popFrame();
    }

    public static void Kernel_threadKill(Machine machine) {

        // Not sure whether or not we need to test for being a
        // current thread or not. Works for the current thread.
        // Others may need to push a return and pop the frame.

        int thread = machine.frameLocal(0);
        machine.kill(thread);
    }

    public static void Kernel_threadNext(Machine machine) {
        int thread = machine.frameLocal(0);
        Thread t = machine.getThread(thread);
        if (t != null)
            machine.pushStack(Machine.mkThread(t.next().id()));
        else
            machine.pushStack(thread);
        machine.popFrame();
    }

    public static void Kernel_threadState(Machine machine) {
        int thread = machine.frameLocal(0);
        Thread t = machine.getThread(thread);
        if (t != null)
            machine.pushStack(Machine.mkInt(t.state()));
        else
            machine.pushStack(Machine.mkInt(-1));
        machine.popFrame();
    }

    public static int machineTime(Machine machine, long now, long startTime) {
        long time = now - startTime;
        int hours = (int) (time / (60 * 60 * 1000F));
        int mins = (int) ((time - (hours * 60 * 60 * 1000F)) / (60 * 1000F));
        int secs = (int) ((time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F))) / 1000F);
        int millis = (int) (time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F) + (secs * 1000F)));
        return machineTime(machine, hours, mins, secs, millis);
    }

    public static int machineTime(Machine machine, int hours, int mins, int secs, int millis) {
        int machineTime = Machine.nilValue;
        machineTime = machine.mkCons(Machine.mkInt(millis), machineTime);
        machineTime = machine.mkCons(Machine.mkInt(secs), machineTime);
        machineTime = machine.mkCons(Machine.mkInt(mins), machineTime);
        machineTime = machine.mkCons(Machine.mkInt(hours), machineTime);
        return machineTime;
    }

    public static long javaTime(Machine machine, int machineTime) {
        int hours = Machine.value(machine.at(machineTime, 0));
        int mins = Machine.value(machine.at(machineTime, 1));
        int secs = Machine.value(machine.at(machineTime, 2));
        int millis = Machine.value(machine.at(machineTime, 3));
        return (long) ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F) + (secs * 1000F) + millis);
    }

    public static final void Kernel_time(Machine machine) {
        machine.pushStack(machineTime(machine, System.currentTimeMillis(), machine.time));
        machine.popFrame();
    }

    public static void Kernel_timeDifference(Machine machine) {
        int time1 = machine.frameLocal(0);
        int time2 = machine.frameLocal(1);
        long millis1 = javaTime(machine, time1);
        long millis2 = javaTime(machine, time2);
        long time = millis1 - millis2;
        int hours = (int) (time / (60 * 60 * 1000F));
        int mins = (int) ((time - (hours * 60 * 60 * 1000F)) / (60 * 1000F));
        int secs = (int) ((time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F))) / 1000F);
        int millis = (int) (time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F) + (secs * 1000F)));
        machine.pushStack(machineTime(machine, hours, mins, secs, millis));
        machine.popFrame();
    }

    public static void Kernel_timeAdd(Machine machine) {
        int time1 = machine.frameLocal(0);
        int time2 = machine.frameLocal(1);
        long millis1 = javaTime(machine, time1);
        long millis2 = javaTime(machine, time2);
        long time = millis1 + millis2;
        int hours = (int) (time / (60 * 60 * 1000F));
        int mins = (int) ((time - (hours * 60 * 60 * 1000F)) / (60 * 1000F));
        int secs = (int) ((time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F))) / 1000F);
        int millis = (int) (time - ((hours * 60 * 60 * 1000F) + (mins * 60 * 1000F) + (secs * 1000F)));
        machine.pushStack(machineTime(machine, hours, mins, secs, millis));
        machine.popFrame();
    }

    public static void Kernel_tokenChannelTextTo(Machine machine) {
        int channel = machine.frameLocal(0);
        int position = machine.frameLocal(1);
        machine.pushStack(machine.textTo(channel, position));
        machine.popFrame();
    }

    public static void Kernel_traceFrames(Machine machine) {
        int returnValue = machine.frameLocal(0);
        if (returnValue == Machine.trueValue)
            machine.traceFrames = true;
        else
            machine.traceFrames = false;
        machine.pushStack(returnValue);
        machine.popFrame();
    }

    public static void Kernel_traceInstrs(Machine machine) {
        int bool = machine.frameLocal(0);
        machine.traceInstrs(bool == Machine.trueValue);
        machine.pushStack(bool);
        machine.popFrame();
    }

    public static void Kernel_traceInstrsToFile(Machine machine) {
        int file = machine.frameLocal(0);
        String fileString = machine.valueToString(file);
        machine.setDebugOutputFile(fileString);
        machine.traceInstrs(true);
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
    }

    public static void Kernel_undoStackSize(Machine machine) {
        machine.pushStack(Machine.mkInt(machine.undo.undoStackSize()));
        machine.popFrame();
    }

    public static void Kernel_unify(Machine machine) {
        int Var = machine.frameLocal(0);
        int v1 = machine.frameLocal(1);
        int v2 = machine.frameLocal(2);
        int env = machine.frameLocal(3);
        machine.pushStack(Unify.unify(machine, Var, v1, v2, env));
        machine.popFrame();
    }

    public static void Kernel_URLInputChannel(Machine machine) {
        int string = machine.frameLocal(0);
        String name = machine.valueToString(string);
        machine.pushStack(machine.mkURLInputChannel(name));
        machine.popFrame();
    }

    public static void Kernel_usedHeap(Machine machine) {
        machine.pushStack(Machine.mkInt(machine.usedHeap()));
        machine.popFrame();
    }

    public static void Kernel_value(Machine machine) {
        int value = machine.frameLocal(0);
        machine.pushStack(Machine.mkInt(Machine.value(value)));
        machine.popFrame();
    }

    public static void Kernel_valueToString(Machine machine) {
        int value = machine.frameLocal(0);
        machine.pushStack(machine.mkString(machine.valueToString(value)));
        machine.popFrame();
    }

    public static void Kernel_wake(Machine machine) {
        int thread = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.wake(thread, value);
        machine.pushStack(thread);
        machine.popFrame();
    }

    public static void Kernel_write(Machine machine) {
        int output = machine.frameLocal(0);
        int value = machine.frameLocal(1);
        machine.pushStack(output);
        machine.popFrame();
        machine.write(output, value);
    }

    public static void Kernel_writeCommand(Machine machine) {
        int client = machine.frameLocal(0);
        int message = machine.frameLocal(1);
        int arity = machine.frameLocal(2);
        machine.pushStack(machine.writeCommand(client, message, arity, false));
        machine.popFrame();
    }

    public static void Kernel_writePacket(Machine machine) {
        int client = machine.frameLocal(0);
        int packet = machine.frameLocal(1);
        int size = machine.frameLocal(2);
        machine.pushStack(machine.writePacket(client, packet, size));
        machine.popFrame();
    }

    public static void Kernel_yield(Machine machine) {
        machine.pushStack(Machine.trueValue);
        machine.popFrame();
        machine.yield();
    }

    public static void Kernel_zipNewEntry(Machine machine) {
        int out = machine.frameLocal(0);
        int name = machine.frameLocal(1);
        machine.zipNewEntry(out, name);
        machine.pushStack(out);
        machine.popFrame();
    }

    public static void Kernel_zipInputChannel(Machine machine) {
        int fileName = machine.frameLocal(0);
        int entryName = machine.frameLocal(1);
        machine.pushStack(machine.mkZipInputChannel(fileName, entryName));
        machine.popFrame();
    }

    public static void Kernel_zipOutputChannel(Machine machine) {
        int out = machine.frameLocal(0);
        machine.pushStack(machine.mkZipOutputChannel(out));
        machine.popFrame();
    }

}