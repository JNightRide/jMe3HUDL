package sun.e.g.jme3hudl;

import com.jme3.scene.Node;
import e.g.jme3hudl.Jme3HudlConstants;
import e.g.jme3hudl.LayoutStyle;

public class DefaultLayoutStyle extends LayoutStyle {
    private static final DefaultLayoutStyle INSTANCE =
            new DefaultLayoutStyle();

    public static LayoutStyle getInstance() {
        return INSTANCE;
    }

    @Override
    public int getPreferredGap(Node component1, Node component2, ComponentPlacement type, int position, Node parent) {
        if (component1 == null || component2 == null || type == null) {
            throw new NullPointerException();
        }

        checkPosition(position);

        if (type == ComponentPlacement.INDENT &&
                (position == Jme3HudlConstants.EAST ||
                 position == Jme3HudlConstants.WEST)) {
            int indent = getIndent(component1, position);
            if (indent > 0) {
                return indent;
            }
        }
        return (type == ComponentPlacement.UNRELATED) ? 12 : 6;
    }

    @Override
    public int getContainerGap(Node component, int position, Node parent) {
        if (component == null) {
            throw new NullPointerException();
        }
        checkPosition(position);
        return 6;
    }
    
    protected int getIndent(Node c, int position) {
        return 0;
    }
    
    private void checkPosition(int position) {
        if (position != Jme3HudlConstants.NORTH &&
                position != Jme3HudlConstants.SOUTH &&
                position != Jme3HudlConstants.WEST &&
                position != Jme3HudlConstants.EAST) {
            throw new IllegalArgumentException();
        }
    }
}
