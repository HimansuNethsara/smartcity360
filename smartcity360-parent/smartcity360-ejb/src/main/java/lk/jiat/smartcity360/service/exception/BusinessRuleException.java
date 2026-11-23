package lk.jiat.smartcity360.service.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}