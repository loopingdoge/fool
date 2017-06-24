package symbol_table;

import type.ClassType;

import java.util.ArrayList;
import java.util.HashMap;

public class DispatchTable {

    private HashMap<String, ArrayList<Integer>> table = new HashMap<>();

    public DispatchTable addClass(ClassType classType) {
        this.table.put(classType.getClassID(), new ArrayList<>());
        return this;
    }


}
