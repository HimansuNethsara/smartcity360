package lk.jiat.smartcity360.pattern.memento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PolicyCaretaker {

    private final List<PolicyMemento> mementos = new ArrayList<>();

    public static PolicyCaretaker of(List<PolicyMemento> existing) {
        PolicyCaretaker c = new PolicyCaretaker();
        existing.stream().sorted(Comparator.comparingInt(PolicyMemento::versionNo)).forEach(c.mementos::add);
        return c;
    }

    public void add(PolicyMemento memento) {
        if (!mementos.isEmpty() && memento.versionNo() <= latest().versionNo()) {
            throw new IllegalArgumentException("Version " + memento.versionNo() + " is not newer than the latest");
        }
        mementos.add(memento);
    }

    public Optional<PolicyMemento> get(int versionNo) {
        return mementos.stream().filter(m -> m.versionNo() == versionNo).findFirst();
    }

    public PolicyMemento latest() {
        return mementos.isEmpty() ? null : mementos.get(mementos.size() - 1);
    }

    public int nextVersionNo() {
        return mementos.isEmpty() ? 1 : latest().versionNo() + 1;
    }

    public boolean isEmpty() {
        return mementos.isEmpty();
    }

    public List<PolicyMemento> versions() {
        return Collections.unmodifiableList(mementos);
    }
}
