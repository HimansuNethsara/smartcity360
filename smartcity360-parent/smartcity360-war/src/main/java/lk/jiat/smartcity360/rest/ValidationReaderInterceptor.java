package lk.jiat.smartcity360.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ValidationReaderInterceptor implements ReaderInterceptor {

    private static final Logger LOG = Logger.getLogger(ValidationReaderInterceptor.class.getName());
    private static volatile Validator validator;

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
        Object body = context.proceed();
        if (body == null || !body.getClass().getName().startsWith("lk.jiat.smartcity360.dto.")) {
            return body;
        }
        Validator v = validator();
        if (v == null) {
            return body;                                     // validation not available: services still check the rules
        }
        Set<ConstraintViolation<Object>> violations = v.validate(body);
        if (violations.isEmpty()) {
            return body;
        }
        Map<String, String> fields = new TreeMap<>();
        for (ConstraintViolation<Object> cv : violations) {
            fields.merge(cv.getPropertyPath().toString(), cv.getMessage(), (a, b) -> a + ", " + b);
        }
        StringBuilder msg = new StringBuilder();
        fields.forEach((f, m) -> msg.append(msg.length() == 0 ? "" : "; ").append(label(f)).append(' ').append(m));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("error", "validation_failed");
        out.put("message", msg.toString());
        out.put("fields", fields);
        String id = RequestLogFilter.currentId();
        if (id != null) {
            out.put("requestId", id);
        }
        throw new WebApplicationException(Response.status(400).type(MediaType.APPLICATION_JSON).entity(out).build());
    }

    private static String label(String property) {
        String spaced = property.replaceAll("([a-z0-9])([A-Z])", "$1 $2").toLowerCase();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static Validator validator() {
        if (validator == null) {
            synchronized (ValidationReaderInterceptor.class) {
                if (validator == null) {
                    try {
                        validator = Validation.buildDefaultValidatorFactory().getValidator();
                    } catch (RuntimeException | LinkageError e) {
                        LOG.log(Level.WARNING, "Bean Validation is not available; request bodies are not pre-checked", e);
                    }
                }
            }
        }
        return validator;
    }
}
