package lk.jiat.smartcity360.service.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}