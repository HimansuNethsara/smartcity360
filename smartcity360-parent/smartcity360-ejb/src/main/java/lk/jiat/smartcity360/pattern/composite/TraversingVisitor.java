package lk.jiat.smartcity360.pattern.composite;

public abstract class TraversingVisitor implements ZoneVisitor {

    private int depth;

    protected abstract void onNode(ZoneComponent node, int depth);

    @Override
    public void visit(ZoneComposite composite) {
        onNode(composite, depth);
        depth++;
        for (ZoneComponent child : composite.getChildren()) {
            child.accept(this);
        }
        depth--;
    }

    @Override
    public void visit(ZoneLeaf leaf) {
        onNode(leaf, depth);
    }
}