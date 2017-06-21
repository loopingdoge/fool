package util;

import symbol_table.SymbolTableEntry;
import type.ClassType;

import java.util.HashMap;

public class CodegenUtils {

    private static int label = 0;
    private static int functionsLabelCount = 0;
    private static String functionsCode = "";

    public static String freshLabel() {
        return "label" + (label++);
    }

    public static String freshFunLabel() {
        return "function" + (functionsLabelCount++);
    }

    public static void insertFunctionsCode(String c) {
        functionsCode += "\n" + c; // aggiunge una linea vuota di separazione prima di funzione
    }

    public static String getFunctionsCode() {
        return functionsCode;
    }

    public static HashMap<String, ClassType> classTable = new HashMap<String, ClassType>();

    public static void addClassEntry( String classID, ClassType classT ) {
        classTable.put( classID, classT );
    }

    public static ClassType getClassEntry( String classID ) {
        return classTable.get( classID );
    }

}