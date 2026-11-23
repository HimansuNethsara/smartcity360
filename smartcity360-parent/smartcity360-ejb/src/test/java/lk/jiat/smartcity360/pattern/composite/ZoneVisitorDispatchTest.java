package lk.jiat.smartcity360.pattern.composite;

import lk.jiat.smartcity360.domain.enums.LandUse;
import lk.jiat.smartcity360.domain.enums.PowerStatus;
import lk.jiat.smartcity360.domain.enums.ZoneType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneVisitorDispatchTest {

    @Mock
    ZoneVisitor visitor;

    private static ZoneRow row(long id, ZoneType type) {
        return new ZoneRow(id, null, type, "Z" + id, "Zone " + id, LandUse.MIXED, PowerStatus.ON);
    }

    @Test
    @DisplayName("A leaf calls visit(ZoneLeaf), a composite calls visit(ZoneComposite)")
    void dispatch() {
        ZoneLeaf leaf = new ZoneLeaf(row(1, ZoneType.BUILDING), null);
        ZoneComposite composite = new ZoneComposite(row(2, ZoneType.STREET));

        leaf.accept(visitor);
        composite.accept(visitor);

        verify(visitor).visit(leaf);
        verify(visitor).visit(composite);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    @DisplayName("TraversingVisitor reports parents before children, with increasing depth")
    void traversalOrderAndDepth() {
        ZoneComposite street = new ZoneComposite(row(1, ZoneType.STREET));
        ZoneLeaf b1 = new ZoneLeaf(row(2, ZoneType.BUILDING), null);
        ZoneLeaf b2 = new ZoneLeaf(row(3, ZoneType.BUILDING), null);
        street.add(b1);
        street.add(b2);

        TraversingVisitor spy = spy(new TraversingVisitor() {
            @Override
            protected void onNode(ZoneComponent node, int depth) {
            }
        });
        street.accept(spy);

        InOrder order = inOrder(spy);
        order.verify(spy).onNode(street, 0);
        order.verify(spy).onNode(b1, 1);
        order.verify(spy).onNode(b2, 1);
    }
}
