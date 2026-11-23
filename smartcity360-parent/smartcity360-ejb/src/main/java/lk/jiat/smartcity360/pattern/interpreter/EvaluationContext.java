package lk.jiat.smartcity360.pattern.interpreter;

import java.util.HashMap;
import java.util.Map;

public final class EvaluationContext {

    private final Map<String, Object> values = new HashMap<>();

    public EvaluationContext put(String name, Object value) {
        if (value != null) {
            values.put(name.toLowerCase(), value);
        }
        return this;
    }

    public Object get(String name) {
        Object v = values.get(name);
        if (v == null) {
            throw new MissingValueException(name);
        }
        return v;
    }

    public Map<String, Object> asMap() {
        return Map.copyOf(values);
    }
}
