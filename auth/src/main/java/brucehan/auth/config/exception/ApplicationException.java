package brucehan.auth.config.exception;

public class ApplicationException extends RuntimeException {
    private final ApplicationExceptionType type;

    public ApplicationException(ApplicationExceptionType type) {
        this.type = type;
    }

    public ApplicationException(Throwable cause, ApplicationExceptionType type) {
        super(cause);
        this.type = type;
    }

    public ApplicationExceptionType type() {
        return type;
    }
}
