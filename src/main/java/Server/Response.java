package Server;

// Request responses
public enum Response {
    ACCEPTED(true, ""),
    SUCCESS(true, ""),
    FAILURE(false, "Failed to connect"),
    ERROR(false, "Failed to connect"),
    CONFLICT(false, "Name was already taken, please choose another one"),
    PERMISSION_DENIED(false ,"Permission denied"), FULL_ROOM(false, "This room is full");

    final boolean succesful;
    final String errorMessage;

    Response(boolean successful, String errorMessage){
        this.succesful = successful;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isSuccesful() {
        return succesful;
    }
}
