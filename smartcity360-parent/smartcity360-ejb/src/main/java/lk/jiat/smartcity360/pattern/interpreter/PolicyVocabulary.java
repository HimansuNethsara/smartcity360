package lk.jiat.smartcity360.pattern.interpreter;

import lk.jiat.smartcity360.domain.enums.LandUse;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class PolicyVocabulary {

    public enum VariableType { NUMBER, TEXT }

    private final Map<String, VariableType> types = new LinkedHashMap<>();
    private final Map<String, Set<String>> allowedTexts = new LinkedHashMap<>();

    private PolicyVocabulary() {
    }

    public static PolicyVocabulary standard() {
        PolicyVocabulary v = new PolicyVocabulary();
        v.types.put("noise", VariableType.NUMBER);
        v.types.put("temperature", VariableType.NUMBER);
        v.types.put("air_quality", VariableType.NUMBER);
        v.types.put("hour", VariableType.NUMBER);
        v.types.put("zone", VariableType.TEXT);
        Set<String> uses = new LinkedHashSet<>();
        Arrays.stream(LandUse.values()).forEach(u -> uses.add(u.name()));
        v.allowedTexts.put("zone", uses);
        return v;
    }

    public VariableType typeOf(String name) {
        return types.get(name);
    }

    public Set<String> allowedTexts(String name) {
        return allowedTexts.get(name);
    }

    public Set<String> names() {
        return types.keySet();
    }
}
