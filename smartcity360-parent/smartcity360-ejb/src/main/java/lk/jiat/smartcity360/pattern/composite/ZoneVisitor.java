package lk.jiat.smartcity360.pattern.composite;

public interface ZoneVisitor {

    void visit(ZoneComposite composite);

    void visit(ZoneLeaf leaf);
}