package util;

public class DispatchTableEntry {

    private String methodID;
    private String methodLabel;

    public DispatchTableEntry(String methodID, String methodLabel) {
        this.methodID = methodID;
        this.methodLabel = methodLabel;
    }

    public String getMethodID() {
        return methodID;
    }

    public String getMethodLabel() {
        return methodLabel;
    }
}
