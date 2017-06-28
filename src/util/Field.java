package util;

import type.Type;

public class Field {

    private String id;
    private Type type;

    public Field(String id, Type type) {
        this.id = id;
        this.type = type;
    }

    public String getID() {
        return id;
    }

    public Type getType() {
        return type;
    }
}
