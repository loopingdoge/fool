package util;

public class DispatchTableEntry {

    private String methodID;
    private String methodCode;

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public DispatchTableEntry(String methodID, String methodCode) {
        this.methodID = methodID;

        this.methodCode = methodCode;
    }

    public String getMethodID() {
        return methodID;
    }

    public String getMethodCode() {
        return methodCode;
    }
}
