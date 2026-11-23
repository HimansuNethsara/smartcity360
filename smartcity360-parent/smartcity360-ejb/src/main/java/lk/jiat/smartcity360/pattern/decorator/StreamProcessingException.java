package lk.jiat.smartcity360.pattern.decorator;

public class StreamProcessingException extends RuntimeException {

    public StreamProcessingException(String message) {
        super(message);
    }

    public StreamProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
