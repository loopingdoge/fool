package util;

import exception.RedeclaredClassException;
import exception.UndeclaredClassException;
import type.ClassType;

import java.util.HashMap;

public class CodegenUtils {

    public static HashMap<String, ClassType> classTable = new HashMap<String, ClassType>();
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

    public static void addClassEntry( String classID, ClassType classT ) throws RedeclaredClassException {
        if( classTable.get( classID ) != null ) throw new RedeclaredClassException( classID );
        classTable.put( classID, classT );
    }

    public static ClassType getClassEntry( String classID ) throws UndeclaredClassException {
        ClassType classT = classTable.get( classID );
        if( classT == null ) throw new UndeclaredClassException( classID );
        return classTable.get( classID );
    }

    public static void reset() {
        label = 0;
        functionsLabelCount = 0;
        functionsCode = "";
        classTable = new HashMap<>();
    }

}