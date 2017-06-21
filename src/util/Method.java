package util;

import type.FunType;

public class Method {

    private String id;
    private FunType type;

    public Method(String id, FunType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public FunType getType() {
        return type;
    }
}
