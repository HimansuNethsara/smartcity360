package lk.jiat.smartcity360.pattern.memento;

public final class PolicyDocument {

    private String dslText;

    public PolicyDocument(String dslText) {
        this.dslText = dslText;
    }

    public void edit(String newText) {
        this.dslText = newText;
    }

    public PolicyMemento save(int versionNo) {
        return new PolicyMemento(versionNo, dslText);
    }

    public void restore(PolicyMemento memento) {
        this.dslText = memento.dslText();
    }

    public String getDslText() {
        return dslText;
    }
}
