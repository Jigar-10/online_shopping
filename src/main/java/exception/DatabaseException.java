package exception;

public class DatabaseException extends RuntimeException {
    private String sqlState;
    private int errorCode;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message, String sqlState, int errorCode, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
