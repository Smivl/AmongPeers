package Game.Task;

public enum TaskType {
    UPLOAD_DATA("Upload Data"),
    WIRING("Fix Wiring"),
    SUBMIT_SCAN("Submit Medbay Scan"),
    EMPTY_CHUTE("Empty Chutes"),
    EMPTY_GARBAGE("Empty Garbage"),
    CLEAN_O2_FILTER("Clean O2 Filter");

    private final String displayName;

    TaskType(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
