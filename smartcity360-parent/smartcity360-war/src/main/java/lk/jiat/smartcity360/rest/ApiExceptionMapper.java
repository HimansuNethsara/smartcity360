package lk.jiat.smartcity360.rest;

import jakarta.ejb.EJBException;
import jakarta.json.bind.JsonbException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lk.jiat.smartcity360.service.exception.BusinessRuleException;
import lk.jiat.smartcity360.service.exception.ResourceNotFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable throwable) {
        Throwable t = throwable;
        while (t instanceof EJBException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof WebApplicationException wae) {
            return wae.getResponse();                       // already shaped (401, 403, 400 from validation ...)
        }
        if (t instanceof ResourceNotFoundException) {
            return body(404, "not_found", t.getMessage(), Level.FINE, null);
        }
        if (t instanceof BusinessRuleException) {
            return body(422, "rule_violation", t.getMessage(), Level.FINE, null);
        }
        if (t instanceof ConstraintViolationException) {
            return body(400, "validation_failed", "The request has invalid values.", Level.FINE, null);
        }
        for (Throwable c = throwable; c != null; c = c.getCause()) {
            if (c instanceof JsonbException || c instanceof jakarta.json.stream.JsonParsingException) {
                return body(400, "bad_request",
                        "The request body is not valid JSON, or a value has the wrong type or is not one of the allowed options.",
                        Level.FINE, null);
            }
            if (c instanceof jakarta.persistence.OptimisticLockException) {
                return body(409, "conflict",
                        "Someone else changed this record at the same moment. Reload and try again.", Level.FINE, null);
            }
        }
        return body(500, "server_error", "Unexpected server error. Quote the reference below to support.",
                Level.SEVERE, throwable);
    }

    private static Response body(int status, String code, String message, Level level, Throwable cause) {
        String id = RequestLogFilter.currentId();
        if (cause != null) {
            LOG.log(level, "[" + id + "] Unhandled error in REST call", cause);
        } else {
            LOG.log(level, () -> "[" + id + "] " + code + ": " + message);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("error", code);
        out.put("message", message == null ? "" : message);
        if (id != null) {
            out.put("requestId", id);
        }
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(out).build();
    }
}
