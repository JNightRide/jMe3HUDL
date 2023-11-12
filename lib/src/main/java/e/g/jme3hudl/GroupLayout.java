/* Copyright (c) 2009-2023 jMonkeyEngine.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package e.g.jme3hudl;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

import e.g.jme3hudl.LayoutStyle.ComponentPlacement;
import sun.e.g.jme3hudl.BaselineResizeBehavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Una versioón adaptada del diseño <code>javax.swing.GroupLayout</code>, tenga
 * encuenta que este objeto solo se hacerca al 99% de este diseño.
 * 
 * @author wil
 */
public class GroupLayout extends AbstractGuiComponent implements GuiLayout {

    /** Loggers de la clase <code>GroupLayout</code>. */
    private static final Logger LOG = Logger.getLogger(GroupLayout.class.getName());
    
    /*
        Utilizado en cálculos de tamaño.
    */
    private static final int MIN_SIZE = 0;
    private static final int PREF_SIZE = 1;
    private static final int MAX_SIZE = 2;

    /*
        Usado por prepare, indica que min, pref o max no se usarán.
    */
    private static final int SPECIFIC_SIZE = 3;
    private static final int UNSET = Integer.MIN_VALUE;

    /** Restricción del tamaño máximo del resorte para evitar el desbordamiento de enteros. */
    private static final float INFINITE = Integer.MAX_VALUE >> 1;
    
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL   = 1;
    
    /**
     * Indica el tamaño del componente o espacio que se debe utilizar para un
     * valor de rango particular.
     */
    public static final int DEFAULT_SIZE = -1;

    /**
     * Indica el tamaño preferido del componente o espacio que debe
     * usarse para un valor de rango particular.
     */
    public static final int PREFERRED_SIZE = -2;

    /*
        Si intentamos o no, automáticamente creara el acolchado
        preferido entre componentes.
    */
    private boolean autocreatePadding;

    /*
        Si intentamos o no automáticamente creara el relleno preferido
        entre componentes que tocan el borde del contenedor.
    */
    private boolean autocreateContainerPadding;
    
    private LayoutStyle layoutStyle;
    private Group horizontalGroup;

    /**
     * Group responsible for layout along the vertical axis.  This is NOT
     * the user specified group, use getVerticalGroup to dig that out.
     */
    private Group verticalGroup;

    // Maps from Component to ComponentInfo.  This is used for tracking
    // information specific to a Component.
    private Map<Node,ComponentInfo> componentInfos;

    // Container we're doing layout for.
    private Node host;
    private Set<Spring> tmpParallelSet;
    
    private boolean springsChanged;
    private boolean isValid;
    private boolean hasPreferredPaddingSprings;
    private boolean honorsVisibility;
    //private boolean invalidateHost;
    
    
    /**
     * Enumeración de las posibles formas en que {@code ParallelGroup} puede 
     * alinear a sus hijos.
     */
    public enum Alignment {
        
        /**
         * Indica que los elementos deben ser
         * alineado al origen. Para el eje horizontal de izquierda a
         * orientación derecha, esto significa alineado con el borde izquierdo.
         * Para el eje vertical principal significa alineado con el borde superior.
         */
        LEADING,

        /**
         * Indicates the elements should be aligned to the end of the
         * region.  For the horizontal axis with a left to right
         * orientation this means aligned to the right edge. For the
         * vertical axis trailing means aligned to the bottom edge.
         */
        TRAILING,

        /**
         * Indicates the elements should be centered in
         * the region.
         *
         * @see #createParallelGroup(Alignment)
         */
        CENTER,

        /**
         * Indicates the elements should be aligned along
         * their baseline.
         *
         * @see #createParallelGroup(Alignment)
         * @see #createBaselineGroup(boolean,boolean)
         */
        BASELINE
    }
    
    private static void checkSize(float min, float pref, float max,
            boolean isComponentSpring) {
        checkResizeType(min, isComponentSpring);
        if (!isComponentSpring && pref < 0) {
            throw new IllegalArgumentException("Pref must be >= 0");
        } else if (isComponentSpring) {
            checkResizeType(pref, true);
        }
        checkResizeType(max, isComponentSpring);
        checkLessThan(min, pref);
        checkLessThan(pref, max);
    }

    private static void checkResizeType(float type, boolean isComponentSpring) {
        if (type < 0 && ((isComponentSpring && type != DEFAULT_SIZE &&
                type != PREFERRED_SIZE) ||
                (!isComponentSpring && type != PREFERRED_SIZE))) {
            throw new IllegalArgumentException("Invalid size");
        }
    }

    private static void checkLessThan(float min, float max) {
        if (min >= 0 && max >= 0 && min > max) {
            throw new IllegalArgumentException(
                    "Following is not met: min<=pref<=max");
        }
    }
    
    public GroupLayout(Node host) {
        if (host == null) {
            throw new IllegalArgumentException("Container must be non-null");
        }
        honorsVisibility = true;
        this.host = host;
        GroupLayout.this.setHorizontalGroup(GroupLayout.this.createParallelGroup(Alignment.LEADING, true));
        GroupLayout.this.setVerticalGroup(GroupLayout.this.createParallelGroup(Alignment.LEADING, true));
        componentInfos = new HashMap<>();
        tmpParallelSet = new HashSet<>();
    }
    
    public void setHonorsVisibility(boolean honorsVisibility) {
        if (this.honorsVisibility != honorsVisibility) {
            this.honorsVisibility = honorsVisibility;
            springsChanged = true;
            isValid = false;
            invalidate();
        }
    }
    
    public boolean getHonorsVisibility() {
        return honorsVisibility;
    }
    
    public void setHonorsVisibility(Node component,
            Boolean honorsVisibility) {
        if (component == null) {
            throw new IllegalArgumentException("Component must be non-null");
        }
        getComponentInfo(component).setHonorsVisibility(honorsVisibility);
        springsChanged = true;
        isValid = false;
        invalidate();
    }
    
    public void setAutoCreateGaps(boolean autoCreatePadding) {
        if (this.autocreatePadding != autoCreatePadding) {
            this.autocreatePadding = autoCreatePadding;
            invalidate();
        }
    }
    public boolean getAutoCreateGaps() {
        return autocreatePadding;
    }

    
    public void setAutoCreateContainerGaps(boolean autoCreateContainerPadding){
        if (this.autocreateContainerPadding != autoCreateContainerPadding) {
            this.autocreateContainerPadding = autoCreateContainerPadding;
            horizontalGroup = createTopLevelGroup(getHorizontalGroup());
            verticalGroup = createTopLevelGroup(getVerticalGroup());
            invalidate();
        }
    }

    public boolean getAutoCreateContainerGaps() {
        return autocreateContainerPadding;
    }
    
    public void setHorizontalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        horizontalGroup = createTopLevelGroup(group);
        invalidate();
    }
    
    private Group getHorizontalGroup() {
        int index = 0;
        if (horizontalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)horizontalGroup.springs.get(index);
    }
    public void setVerticalGroup(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("Group must be non-null");
        }
        verticalGroup = createTopLevelGroup(group);
        invalidate();
    }

    private Group getVerticalGroup() {
        int index = 0;
        if (verticalGroup.springs.size() > 1) {
            index = 1;
        }
        return (Group)verticalGroup.springs.get(index);
    }

    private Group createTopLevelGroup(Group specifiedGroup) {
        SequentialGroup group = createSequentialGroup();
        if (getAutoCreateContainerGaps()) {
            group.addSpring(new ContainerAutoPreferredGapSpring());
            group.addGroup(specifiedGroup);
            group.addSpring(new ContainerAutoPreferredGapSpring());
        } else {
            group.addGroup(specifiedGroup);
        }
        return group;
    }
    
    public SequentialGroup createSequentialGroup() {
        return new SequentialGroup();
    }
    
    public ParallelGroup createParallelGroup() {
        return createParallelGroup(Alignment.LEADING);
    }
    
    public ParallelGroup createParallelGroup(Alignment alignment) {
        return createParallelGroup(alignment, true);
    }
    
    public ParallelGroup createParallelGroup(Alignment alignment,
            boolean resizable){
        if (alignment == null) {
            throw new IllegalArgumentException("alignment must be non null");
        }

        if (alignment == Alignment.BASELINE) {
            return new BaselineGroup(resizable);
        }
        return new ParallelGroup(alignment, resizable);
    }
    public ParallelGroup createBaselineGroup(boolean resizable,
            boolean anchorBaselineToTop) {
        return new BaselineGroup(resizable, anchorBaselineToTop);
    }
    
    public void linkSize(Node... components) {
        linkSize(Jme3HudlConstants.HORIZONTAL, components);
        linkSize(Jme3HudlConstants.VERTICAL, components);
    }
    
    public void linkSize(int axis, Node... components) {
        if (components == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        for (int counter = components.length - 1; counter >= 0; counter--) {
            Node c = components[counter];
            if (components[counter] == null) {
                throw new IllegalArgumentException(
                        "Components must be non-null");
            }
            getComponentInfo(c);
        }
        int glAxis;
        switch (axis) {
            case Jme3HudlConstants.HORIZONTAL:
                glAxis = HORIZONTAL;
                break;
            case Jme3HudlConstants.VERTICAL:
                glAxis = VERTICAL;
                break;
            default:
                throw new IllegalArgumentException("Axis must be one of " +
                        "SwingConstants.HORIZONTAL or SwingConstants.VERTICAL");
        }
        LinkInfo master = getComponentInfo(
                components[components.length - 1]).getLinkInfo(glAxis);
        for (int counter = components.length - 2; counter >= 0; counter--) {
            master.add(getComponentInfo(components[counter]));
        }
        invalidate();
    }
    
    public void replace(Node existingComponent, Node newComponent) {
        if (existingComponent == null || newComponent == null) {
            throw new IllegalArgumentException("Components must be non-null");
        }
        
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        ComponentInfo info = componentInfos.remove(existingComponent);
        if (info == null) {
            throw new IllegalArgumentException("Component must already exist");
        }
        /*host.remove(existingComponent);*/
        host.getControl(GuiControl.class).getLayout().removeChild(existingComponent);
        if (newComponent.getParent() != host) {
            /*host.add(newComponent);*/
            host.getControl(GuiControl.class).getLayout().addChild(newComponent);
        }
        info.setComponent(newComponent);
        componentInfos.put(newComponent, info);
        invalidate();
    }
    
    public void setLayoutStyle(LayoutStyle layoutStyle) {
        this.layoutStyle = layoutStyle;
        invalidate();
    }

    public LayoutStyle getLayoutStyle() {
        return layoutStyle;
    }

    private LayoutStyle getLayoutStyle0() {
        LayoutStyle layoutStyle0 = getLayoutStyle();
        if (layoutStyle0 == null) {
            layoutStyle0 = LayoutStyle.getInstance();
        }
        return layoutStyle0;
    }
    
    @Override
    protected void invalidate() {
        super.invalidate();
        isValid = false;
    }
        
    private void prepare(int sizeType) {
        boolean visChanged = false;
        if (!isValid) {
            isValid = true;
            horizontalGroup.setSize(HORIZONTAL, UNSET, UNSET);
            verticalGroup.setSize(VERTICAL, UNSET, UNSET);
            for (ComponentInfo ci : componentInfos.values()) {
                if (ci.updateVisibility()) {
                    visChanged = true;
                }
                ci.clearCachedSize();
            }
        }
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        
        if (springsChanged || visChanged) {
            checkComponents();
            horizontalGroup.removeAutopadding();
            verticalGroup.removeAutopadding();
            if (getAutoCreateGaps()) {
                insertAutopadding(true);
            } else if (hasPreferredPaddingSprings ||
                    getAutoCreateContainerGaps()) {
                insertAutopadding(false);
            }
            springsChanged = false;
        }
        
        if (sizeType != SPECIFIC_SIZE && (getAutoCreateGaps() ||
                getAutoCreateContainerGaps() || hasPreferredPaddingSprings)) {
            calculateAutopadding(horizontalGroup, HORIZONTAL, sizeType, 0, 0);
            calculateAutopadding(verticalGroup, VERTICAL, sizeType, 0, 0);
        }
    }

    private void calculateAutopadding(Group group, int axis, int sizeType,
            float origin, float size) {
        group.unsetAutopadding();
        switch(sizeType) {
            case MIN_SIZE:
                size = group.getMinimumSize(axis);
                break;
            case PREF_SIZE:
                size = group.getPreferredSize(axis);
                break;
            case MAX_SIZE:
                size = group.getMaximumSize(axis);
                break;
            default:
                break;
        }
        group.setSize(axis, origin, size);
        group.calculateAutopadding(axis);
    }

    private void checkComponents() {
        for (ComponentInfo info : componentInfos.values()) {
            if (info.horizontalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a horizontal group");
            }
            if (info.verticalSpring == null) {
                throw new IllegalStateException(info.component +
                        " is not attached to a vertical group");
            }
        }
    }

    private void registerComponents(Group group, int axis) {
        List<Spring> springs = group.springs;
        for (int counter = springs.size() - 1; counter >= 0; counter--) {
            Spring spring = springs.get(counter);
            if (spring instanceof ComponentSpring) {
                ((ComponentSpring)spring).installIfNecessary(axis);
            } else if (spring instanceof Group) {
                registerComponents((Group)spring, axis);
            }
        }
    }

    private Vector3f adjustSize(float width, float height) {
        Insets3f insets = null;
        if (host instanceof Panel) {
            insets = ((Panel) host).getInsets();
        }
        if (insets == null) {
            insets = new Insets3f(new Vector3f(), new Vector3f());
        }
        return new Vector3f(width + insets.min.x + insets.max.x,
                height + insets.min.x + insets.max.y, 0);
    }

    private void checkParent(Node parent) {
        if (parent != host) {
            throw new IllegalArgumentException(
                    "GroupLayout can only be used with one Container at a time");
        }
    }

    private ComponentInfo getComponentInfo(Node component) {
        ComponentInfo info = componentInfos.get(component);
        if (info == null) {
            info = new ComponentInfo(component);
            componentInfos.put(component, info);
            if (component.getParent() != host) {
                /*host.add(component);*/
                host.getControl(GuiControl.class).getLayout().addChild(component);
            }
        }
        return info;
    }
    
    private void insertAutopadding(boolean insert) {
        horizontalGroup.insertAutopadding(HORIZONTAL,
                new ArrayList<>(1),
                new ArrayList<>(1),
                new ArrayList<>(1),
                new ArrayList<>(1), insert);
        verticalGroup.insertAutopadding(VERTICAL,
                new ArrayList<>(1),
                new ArrayList<>(1),
                new ArrayList<>(1),
                new ArrayList<>(1), insert);
    }
    
    private boolean areParallelSiblings(Node source, Node target,
            int axis) {
        ComponentInfo sourceInfo = getComponentInfo(source);
        ComponentInfo targetInfo = getComponentInfo(target);
        Spring sourceSpring;
        Spring targetSpring;
        if (axis == HORIZONTAL) {
            sourceSpring = sourceInfo.horizontalSpring;
            targetSpring = targetInfo.horizontalSpring;
        } else {
            sourceSpring = sourceInfo.verticalSpring;
            targetSpring = targetInfo.verticalSpring;
        }
        Set<Spring> sourcePath = tmpParallelSet;
        sourcePath.clear();
        Spring spring = sourceSpring.getParent();
        while (spring != null) {
            sourcePath.add(spring);
            spring = spring.getParent();
        }
        spring = targetSpring.getParent();
        while (spring != null) {
            if (sourcePath.contains(spring)) {
                sourcePath.clear();
                while (spring != null) {
                    if (spring instanceof ParallelGroup) {
                        return true;
                    }
                    spring = spring.getParent();
                }
                return false;
            }
            spring = spring.getParent();
        }
        sourcePath.clear();
        return false;
    }
    
    @Override
    public void calculatePreferredSize(Vector3f size) {
        checkParent(getNode());
        prepare(PREF_SIZE);
        Vector3f dim = adjustSize(horizontalGroup.getPreferredSize(HORIZONTAL),
                verticalGroup.getPreferredSize(VERTICAL));
        size.set(dim);
    }

    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        prepare(SPECIFIC_SIZE);
        Insets3f insets = null;
        if (getNode() instanceof Panel) {
            insets = ((Panel) getNode()).getInsets();
        }
        if (insets == null) {
            insets = new Insets3f(new Vector3f(), new Vector3f());
        }

        checkParent(getNode());
        Vector3f dim = getGuiControl().getPreferredSize();
        float width = dim.x - insets.min.x - insets.max.x;
        float height = dim.y - insets.min.y - insets.max.y;
        
        boolean ltr = isLeftToRight();
        if (getAutoCreateGaps() || getAutoCreateContainerGaps() ||
                hasPreferredPaddingSprings) {
            calculateAutopadding(horizontalGroup, HORIZONTAL, SPECIFIC_SIZE, 0,
                    width);
            calculateAutopadding(verticalGroup, VERTICAL, SPECIFIC_SIZE, 0,
                    height);
        }
        
        horizontalGroup.setSize(HORIZONTAL, 0, width);
        verticalGroup.setSize(VERTICAL, 0, height);
        
        for (ComponentInfo info : componentInfos.values()) {
            info.setBounds(insets, width, ltr);
        }
    }

    @Override
    public <T extends Node> T addChild(T n, Object... constraints) {
        if( n != null && n.getControl(GuiControl.class) == null ) {
            throw new IllegalArgumentException( "Child is not GUI element." );
        }
        
        if ( n == null ) { return null; }
        if ( componentInfos.containsKey(n) ) {
            removeChild(n);
        }
        
        if ( isAttached() ) {
            getNode().attachChild(n);
        }
        invalidate();
        return n;
    }

    @Override
    public void removeChild(Node n) {
        ComponentInfo info = componentInfos.remove(n);
        if (info != null) {
            info.dispose();
            springsChanged = true;
            isValid = false;
            invalidate();
        }
    }

    @Override
    public Collection<Node> getChildren() {
        return Collections.unmodifiableSet(componentInfos.keySet());
    }

    @Override
    public void clearChildren() {
        for (final Map.Entry<Node, ComponentInfo> entry : componentInfos.entrySet()) {
            ComponentInfo info = entry.getValue();
            if (info != null) {
                info.dispose();
                springsChanged = true;
                isValid = false;
            }
        }
        invalidate();
    }

    @Override
    public GuiLayout clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private boolean isLeftToRight() {
        return true;
    }
    
    @Override
    public String toString() {
        if (springsChanged) {
            registerComponents(horizontalGroup, HORIZONTAL);
            registerComponents(verticalGroup, VERTICAL);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HORIZONTAL\n");
        createSpringDescription(sb, horizontalGroup, "  ", HORIZONTAL);
        sb.append("\nVERTICAL\n");
        createSpringDescription(sb, verticalGroup, "  ", VERTICAL);
        return sb.toString();
    }

    private void createSpringDescription(StringBuilder sb, Spring spring,
            String indent, int axis) {
        String origin = "";
        String padding = "";
        if (spring instanceof ComponentSpring) {
            ComponentSpring cSpring = (ComponentSpring)spring;
            origin = Float.toString(cSpring.getOrigin()) + " ";
            String name = cSpring.getComponent().getName();
            if (name != null) {
                origin = "name=" + name + ", ";
            }
        }
        if (spring instanceof AutoPreferredGapSpring) {
            AutoPreferredGapSpring paddingSpring =
                    (AutoPreferredGapSpring)spring;
            padding = ", userCreated=" + paddingSpring.getUserCreated() +
                    ", matches=" + paddingSpring.getMatchDescription();
        }
        sb.append(indent).append(Objects.requireNonNull(spring).getClass().getName()).append(' ')
                .append(Integer.toHexString(spring.hashCode())).append(' ')
                .append(origin).append(", size=").append(spring.getSize())
                .append(", alignment=").append(spring.getAlignment())
                .append(" prefs=[").append(spring.getMinimumSize(axis))
                .append(' ').append(spring.getPreferredSize(axis)).append(' ')
                .append(spring.getMaximumSize(axis)).append(padding)
                .append("]\n");
        if (spring instanceof Group) {
            List<Spring> springs = ((Group)spring).springs;
            indent += "  ";
            for (int counter = 0; counter < springs.size(); counter++) {
                createSpringDescription(sb, springs.get(counter), indent,
                        axis);
            }
        }
    }
    
    /**
     * Un objeto de la clase interna <code>Spring</code> consta de un rango: 
     * min, pref y max, un valor en algún lugar del medio de eso, y una 
     * ubicación. Spring almacena en caché el mín/máx/pref. Si min/pref/max ha 
     * cambiado internamente o necesita una actualización se debe invocar le
     * método <code>clear</code>.
     */
    private abstract class Spring {
        private float size;
        private float min;
        private float max;
        private float pref;
        private Spring parent;

        private Alignment alignment;

        Spring() {
            min = pref = max = UNSET;
        }
        
        abstract float calculateMinimumSize(int axis);        
        abstract float calculatePreferredSize(int axis);
        abstract float calculateMaximumSize(int axis);

        
        void setParent(Spring parent) { this.parent = parent; }
        Spring getParent() {  return parent; }

        
        void setAlignment(Alignment alignment) {
            this.alignment = alignment;
        }
        Alignment getAlignment() {
            return alignment;
        }
        
        final float getMinimumSize(int axis) {
            if (min == UNSET) {
                min = constrain(calculateMinimumSize(axis));
            }
            return min;
        }
        
        final float getPreferredSize(int axis) {
            if (pref == UNSET) {
                pref = constrain(calculatePreferredSize(axis));
            }
            return pref;
        }
        
        final float getMaximumSize(int axis) {
            if (max == UNSET) {
                max = constrain(calculateMaximumSize(axis));
            }
            return max;
        }
        
        void setSize(int axis, float origin, float size) {
            this.size = size;
            if (size == UNSET) {
                unset();
            }
        }
        
        void unset() {
            size = min = pref = max = UNSET;
        }
        
        float getSize() {
            return size;
        }

        float constrain(float value) {
            return Math.min(value, INFINITE);
        }

        float getBaseline() {
            return -1;
        }

        BaselineResizeBehavior getBaselineResizeBehavior() {
            return BaselineResizeBehavior.OTHER;
        }

        final boolean isResizable(int axis) {
            float min0 = getMinimumSize(axis);
            float pref0 = getPreferredSize(axis);
            return (min0 != pref0 || pref0 != getMaximumSize(axis));
        }
        
        abstract boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized);
    }
    
    public abstract class Group extends Spring {
        List<Spring> springs;

        Group() {
            springs = new ArrayList<>();
        }
        
        public Group addGroup(Group group) {
            return addSpring(group);
        }
        
        public Group addComponent(Node component) {
            return addComponent(component, DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }
        public Group addComponent(Node component, float min, float pref,
                float max) {
            return addSpring(new ComponentSpring(component, min, pref, max));
        }
        
        public Group addGap(float size) {
            return addGap(size, size, size);
        }
        
        public Group addGap(float min, float pref, float max) {
            return addSpring(new GapSpring(min, pref, max));
        }

        Spring getSpring(int index) {
            return springs.get(index);
        }

        //int indexOf(Spring spring) {
        //    return springs.indexOf(spring);
        //}
        
        Group addSpring(Spring spring) {
            springs.add(spring);
            spring.setParent(this);
            if (!(spring instanceof AutoPreferredGapSpring) ||
                    !((AutoPreferredGapSpring)spring).getUserCreated()) {
                springsChanged = true;
            }
            return this;
        }

        //
        // Métodos Spring
        //

        @Override
        void setSize(int axis, float origin, float size) {
            super.setSize(axis, origin, size);
            if (size == UNSET) {
                for (int counter = springs.size() - 1; counter >= 0;
                counter--) {
                    getSpring(counter).setSize(axis, origin, size);
                }
            } else {
                setValidSize(axis, origin, size);
            }
        }
        
        abstract void setValidSize(int axis, float origin, float size);

        @Override float calculateMinimumSize(int axis) { return calculateSize(axis, MIN_SIZE); }
        @Override float calculatePreferredSize(int axis) { return calculateSize(axis, PREF_SIZE); }
        @Override float calculateMaximumSize(int axis) { return calculateSize(axis, MAX_SIZE); }
        
        float calculateSize(int axis, int type) {
            int count = springs.size();
            if (count == 0) {
                return 0;
            }
            if (count == 1) {
                return getSpringSize(getSpring(0), axis, type);
            }
            float size = constrain(operator(getSpringSize(getSpring(0), axis,
                    type), getSpringSize(getSpring(1), axis, type)));
            for (int counter = 2; counter < count; counter++) {
                size = constrain(operator(size, getSpringSize(
                        getSpring(counter), axis, type)));
            }
            return size;
        }

        float getSpringSize(Spring spring, int axis, int type) {
            switch(type) {
                case MIN_SIZE:
                    return spring.getMinimumSize(axis);
                case PREF_SIZE:
                    return spring.getPreferredSize(axis);
                case MAX_SIZE:
                    return spring.getMaximumSize(axis);
            }
            assert false;
            return 0;
        }
        
        abstract float operator(float a, float b);

        //
        // Padding
        //
        abstract void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert);

        
        void removeAutopadding() {
            unset();
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    if (((AutoPreferredGapSpring)spring).getUserCreated()) {
                        ((AutoPreferredGapSpring)spring).reset();
                    } else {
                        springs.remove(counter);
                    }
                } else if (spring instanceof Group) {
                    ((Group)spring).removeAutopadding();
                }
            }
        }

        void unsetAutopadding() {
            unset();
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    spring.unset();
                } else if (spring instanceof Group) {
                    ((Group)spring).unsetAutopadding();
                }
            }
        }

        void calculateAutopadding(int axis) {
            for (int counter = springs.size() - 1; counter >= 0; counter--) {
                Spring spring = springs.get(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    
                    spring.unset();
                    ((AutoPreferredGapSpring)spring).calculatePadding(axis);
                } else if (spring instanceof Group) {
                    ((Group)spring).calculateAutopadding(axis);
                }
            }
            unset();
        }

        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            for (int i = springs.size() - 1; i >= 0; i--) {
                Spring spring = springs.get(i);
                if (!spring.willHaveZeroSize(treatAutopaddingAsZeroSized)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public class SequentialGroup extends Group {
        private Spring baselineSpring;

        SequentialGroup() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequentialGroup addGroup(Group group) {
            return (SequentialGroup)super.addGroup(group);
        }        
        public SequentialGroup addGroup(boolean useAsBaseline, Group group) {
            super.addGroup(group);
            if (useAsBaseline) {
                baselineSpring = group;
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequentialGroup addComponent(Node component) {
            return (SequentialGroup)super.addComponent(component);
        }
        public SequentialGroup addComponent(boolean useAsBaseline, Node component) {
            super.addComponent(component);
            if (useAsBaseline) {
                baselineSpring = springs.get(springs.size() - 1);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequentialGroup addComponent(Node component, float min,
                float pref, float max) {
            return (SequentialGroup)super.addComponent(
                    component, min, pref, max);
        }
        public SequentialGroup addComponent(boolean useAsBaseline,
                Node component, float min, float pref, float max) {
            super.addComponent(component, min, pref, max);
            if (useAsBaseline) {
                baselineSpring = springs.get(springs.size() - 1);
            }
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequentialGroup addGap(float size) {
            return (SequentialGroup)super.addGap(size);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SequentialGroup addGap(float min,float pref, float max) {
            return (SequentialGroup)super.addGap(min, pref, max);
        }
        
        public SequentialGroup addPreferredGap(Node comp1,
                Node comp2, ComponentPlacement type) {
            return addPreferredGap(comp1, comp2, type, DEFAULT_SIZE,
                    PREFERRED_SIZE);
        }
        
        public SequentialGroup addPreferredGap(Node comp1,
                Node comp2, ComponentPlacement type, float pref,
                float max) {
            if (type == null) {
                throw new IllegalArgumentException("Type must be non-null");
            }
            if (comp1 == null || comp2 == null) {
                throw new IllegalArgumentException(
                        "Components must be non-null");
            }
            checkPreferredGapValues(pref, max);
            return (SequentialGroup)addSpring(new PreferredGapSpring(
                    comp1, comp2, type, pref, max));
        }
        public SequentialGroup addPreferredGap(ComponentPlacement type) {
            return addPreferredGap(type, DEFAULT_SIZE, DEFAULT_SIZE);
        }        
        public SequentialGroup addPreferredGap(ComponentPlacement type,
                float pref, float max) {
            if (type != ComponentPlacement.RELATED &&
                    type != ComponentPlacement.UNRELATED) {
                throw new IllegalArgumentException(
                        "Type must be one of " +
                        "LayoutStyle.ComponentPlacement.RELATED or " +
                        "LayoutStyle.ComponentPlacement.UNRELATED");
            }
            checkPreferredGapValues(pref, max);
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(new AutoPreferredGapSpring(
                    type, pref, max));
        }
        
        public SequentialGroup addContainerGap() {
            return addContainerGap(DEFAULT_SIZE, DEFAULT_SIZE);
        }        
        public SequentialGroup addContainerGap(float pref, float max) {
            if ((pref < 0 && pref != DEFAULT_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE)||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_VALUE " +
                        "or >= 0 and pref <= max");
            }
            hasPreferredPaddingSprings = true;
            return (SequentialGroup)addSpring(
                    new ContainerAutoPreferredGapSpring(pref, max));
        }

        @Override
        float operator(float a, float b) {
            return constrain(a) + constrain(b);
        }

        @Override
        void setValidSize(int axis, float origin, float size) {
            float pref = getPreferredSize(axis);
            if (size == pref) {
                for (Spring spring : springs) {
                    float springPref = spring.getPreferredSize(axis);
                    spring.setSize(axis, origin, springPref);
                    if (axis == HORIZONTAL) {
                        origin += springPref;
                    } else {
                        origin -= springPref;
                    }
                }
            } else if (springs.size() == 1) {
                Spring spring = getSpring(0);
                spring.setSize(axis, origin, Math.min(
                        Math.max(size, spring.getMinimumSize(axis)),
                        spring.getMaximumSize(axis)));
            } else if (springs.size() > 1) {
                setValidSizeNotPreferred(axis, origin, size);
            }
        }

        private void setValidSizeNotPreferred(int axis, float origin, float size) {
            float delta = size - getPreferredSize(axis);
            assert delta != 0;
            boolean useMin = (delta < 0);
            int springCount = springs.size();
            if (useMin) {
                delta *= -1;
            }
            
            List<SpringDelta> resizable = buildResizableList(axis, useMin);
            int resizableCount = resizable.size();

            if (resizableCount > 0) {
                float sDelta = delta / resizableCount;
                float slop = delta - sDelta * resizableCount;
                float[] sizes = new float[springCount];
                float sign = useMin ? -1 : 1;
                
                for (int counter = 0; counter < resizableCount; counter++) {
                    SpringDelta springDelta = resizable.get(counter);
                    if ((counter + 1) == resizableCount) {
                        sDelta += slop;
                    }
                    springDelta.delta = Math.min(sDelta, springDelta.delta);
                    delta -= springDelta.delta;
                    if (springDelta.delta != sDelta && counter + 1 <
                            resizableCount) {
                        sDelta = delta / (resizableCount - counter - 1);
                        slop = delta - sDelta * (resizableCount - counter - 1);
                    }
                    sizes[springDelta.index] = sign * springDelta.delta;
                }

                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    float sSize = spring.getPreferredSize(axis) + sizes[counter];
                    spring.setSize(axis, origin, sSize);
                    if (axis == VERTICAL) {
                        origin -= sSize;
                    } else {
                        origin += sSize;
                    }
                }
            } else {
                for (int counter = 0; counter < springCount; counter++) {
                    Spring spring = getSpring(counter);
                    float sSize;
                    if (useMin) {
                        sSize = spring.getMinimumSize(axis);
                    } else {
                        sSize = spring.getMaximumSize(axis);
                    }
                    spring.setSize(axis, origin, sSize);
                    if (axis == VERTICAL) {
                        origin -= sSize;
                    } else {
                        origin += sSize;
                    }
                }
            }
        }
        
        private List<SpringDelta> buildResizableList(int axis, boolean useMin) {
            int size = springs.size();
            List<SpringDelta> sorted = new ArrayList<>(size);
            for (int counter = 0; counter < size; counter++) {
                Spring spring = getSpring(counter);
                float sDelta;
                if (useMin) {
                    sDelta = spring.getPreferredSize(axis) -
                            spring.getMinimumSize(axis);
                } else {
                    sDelta = spring.getMaximumSize(axis) -
                            spring.getPreferredSize(axis);
                }
                if (sDelta > 0) {
                    sorted.add(new SpringDelta(counter, sDelta));
                }
            }
            Collections.sort(sorted);
            return sorted;
        }

        private int indexOfNextNonZeroSpring(
                int index, boolean treatAutopaddingAsZeroSized) {
            while (index < springs.size()) {
                Spring spring = springs.get(index);
                if (!spring.willHaveZeroSize(treatAutopaddingAsZeroSized)) {
                    return index;
                }
                index++;
            }
            return index;
        }

        @Override
        void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert) {
            List<AutoPreferredGapSpring> newLeadingPadding =
                    new ArrayList<>(leadingPadding);
            List<AutoPreferredGapSpring> newTrailingPadding =
                    new ArrayList<>(1);
            List<ComponentSpring> newLeading =
                    new ArrayList<>(leading);
            List<ComponentSpring> newTrailing = null;
            int counter = 0;
            
            while (counter < springs.size()) {
                Spring spring = getSpring(counter);
                if (spring instanceof AutoPreferredGapSpring) {
                    if (newLeadingPadding.isEmpty()) {
                        AutoPreferredGapSpring padding =
                            (AutoPreferredGapSpring)spring;
                        padding.setSources(newLeading);
                        newLeading.clear();
                        counter = indexOfNextNonZeroSpring(counter + 1, true);
                        if (counter == springs.size()) {                            
                            if (!(padding instanceof
                                  ContainerAutoPreferredGapSpring)) {
                                trailingPadding.add(padding);
                            }
                        } else {
                            newLeadingPadding.clear();
                            newLeadingPadding.add(padding);
                        }
                    } else {
                        counter = indexOfNextNonZeroSpring(counter + 1, true);
                    }
                } else {
                    if (!newLeading.isEmpty() && newLeadingPadding.isEmpty() && insert) {
                        AutoPreferredGapSpring padding =
                                new AutoPreferredGapSpring();
                        
                        springs.add(counter, padding);
                        continue;
                    }
                    if (spring instanceof ComponentSpring) {
                        ComponentSpring cSpring = (ComponentSpring)spring;
                        if (!cSpring.isVisible()) {
                            counter++;
                            continue;
                        }
                        for (AutoPreferredGapSpring gapSpring : newLeadingPadding) {
                            gapSpring.addTarget(cSpring, axis);
                        }
                        newLeading.clear();
                        newLeadingPadding.clear();
                        counter = indexOfNextNonZeroSpring(counter + 1, false);
                        if (counter == springs.size()) {
                            trailing.add(cSpring);
                        } else {
                            newLeading.add(cSpring);
                        }
                    } else if (spring instanceof Group) {
                        if (newTrailing == null) {
                            newTrailing = new ArrayList<>(1);
                        } else {
                            newTrailing.clear();
                        }
                        newTrailingPadding.clear();
                        ((Group)spring).insertAutopadding(axis,
                                newLeadingPadding, newTrailingPadding,
                                newLeading, newTrailing, insert);
                        newLeading.clear();
                        newLeadingPadding.clear();
                        counter = indexOfNextNonZeroSpring(
                                    counter + 1, (newTrailing.isEmpty()));
                        if (counter == springs.size()) {
                            trailing.addAll(newTrailing);
                            trailingPadding.addAll(newTrailingPadding);
                        } else {
                            newLeading.addAll(newTrailing);
                            newLeadingPadding.addAll(newTrailingPadding);
                        }
                    } else {
                        // Gap
                        newLeadingPadding.clear();
                        newLeading.clear();
                        counter++;
                    }
                }
            }
        }

        @Override
        float getBaseline() {
            if (baselineSpring != null) {
                float baseline = baselineSpring.getBaseline();
                if (baseline >= 0) {
                    int size = 0;
                    for (Spring spring : springs) {
                        if (spring == baselineSpring) {
                            return size + baseline;
                        } else {
                            size += spring.getPreferredSize(VERTICAL);
                        }
                    }
                }
            }
            return -1;
        }

        @Override
        BaselineResizeBehavior getBaselineResizeBehavior() {
            if (isResizable(VERTICAL)) {
                if (!baselineSpring.isResizable(VERTICAL)) {
                    boolean leadingResizable = false;
                    for (Spring spring : springs) {
                        if (spring == baselineSpring) {
                            break;
                        } else if (spring.isResizable(VERTICAL)) {
                            leadingResizable = true;
                            break;
                        }
                    }
                    boolean trailingResizable = false;
                    for (int i = springs.size() - 1; i >= 0; i--) {
                        Spring spring = springs.get(i);
                        if (spring == baselineSpring) {
                            break;
                        }
                        if (spring.isResizable(VERTICAL)) {
                            trailingResizable = true;
                            break;
                        }
                    }
                    if (leadingResizable && !trailingResizable) {
                        return BaselineResizeBehavior.CONSTANT_DESCENT;
                    } else if (!leadingResizable && trailingResizable) {
                        return BaselineResizeBehavior.CONSTANT_ASCENT;
                    }
                } else {
                    BaselineResizeBehavior brb = baselineSpring.getBaselineResizeBehavior();
                    if (brb == BaselineResizeBehavior.CONSTANT_ASCENT) {
                        for (Spring spring : springs) {
                            if (spring == baselineSpring) {
                                return BaselineResizeBehavior.CONSTANT_ASCENT;
                            }
                            if (spring.isResizable(VERTICAL)) {
                                return BaselineResizeBehavior.OTHER;
                            }
                        }
                    } else if (brb == BaselineResizeBehavior.CONSTANT_DESCENT) {
                        for (int i = springs.size() - 1; i >= 0; i--) {
                            Spring spring = springs.get(i);
                            if (spring == baselineSpring) {
                                return BaselineResizeBehavior.CONSTANT_DESCENT;
                            }
                            if (spring.isResizable(VERTICAL)) {
                                return BaselineResizeBehavior.OTHER;
                            }
                        }
                    }
                }
                return BaselineResizeBehavior.OTHER;
            }
            
            return BaselineResizeBehavior.CONSTANT_ASCENT;
        }

        private void checkPreferredGapValues(float pref, float max) {
            if ((pref < 0 && pref != DEFAULT_SIZE && pref != PREFERRED_SIZE) ||
                    (max < 0 && max != DEFAULT_SIZE && max != PREFERRED_SIZE)||
                    (pref >= 0 && max >= 0 && pref > max)) {
                throw new IllegalArgumentException(
                        "Pref and max must be either DEFAULT_SIZE, " +
                        "PREFERRED_SIZE, or >= 0 and pref <= max");
            }
        }
    }
    
    private static final class SpringDelta implements Comparable<SpringDelta> {
        public final int index;
        public float delta;

        public SpringDelta(int index, float delta) {
            this.index = index;
            this.delta = delta;
        }

        @Override
        public int compareTo(SpringDelta o) {
            return (int) (delta - o.delta);
        }

        @Override
        public String toString() {
            return super.toString() + "[index=" + index + ", delta=" +
                    delta + "]";
        }
    }
    
    public class ParallelGroup extends Group {
        private final Alignment childAlignment;
        private final boolean resizable;

        ParallelGroup(Alignment childAlignment, boolean resizable) {
            this.childAlignment = childAlignment;
            this.resizable = resizable;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParallelGroup addGroup(Group group) {
            return (ParallelGroup)super.addGroup(group);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParallelGroup addComponent(Node component) {
            return (ParallelGroup)super.addComponent(component);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParallelGroup addComponent(Node component, float min, float pref,
                float max) {
            return (ParallelGroup)super.addComponent(component, min, pref, max);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParallelGroup addGap(float pref) {
            return (ParallelGroup)super.addGap(pref);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParallelGroup addGap(float min, float pref, float max) {
            return (ParallelGroup)super.addGap(min, pref, max);
        }
        public ParallelGroup addGroup(Alignment alignment, Group group) {
            checkChildAlignment(alignment);
            group.setAlignment(alignment);
            return (ParallelGroup)addSpring(group);
        }
        
        public ParallelGroup addComponent(Node component,
                Alignment alignment) {
            return addComponent(component, alignment, DEFAULT_SIZE, DEFAULT_SIZE,
                    DEFAULT_SIZE);
        }        
        public ParallelGroup addComponent(Node component,
                Alignment alignment, float min, float pref, float max) {
            checkChildAlignment(alignment);
            ComponentSpring spring = new ComponentSpring(component,
                    min, pref, max);
            spring.setAlignment(alignment);
            return (ParallelGroup)addSpring(spring);
        }

        boolean isResizable() {
            return resizable;
        }

        @Override
        float operator(float a, float b) {
            return Math.max(a, b);
        }

        @Override
        float calculateMinimumSize(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.calculateMinimumSize(axis);
        }

        @Override
        float calculateMaximumSize(int axis) {
            if (!isResizable()) {
                return getPreferredSize(axis);
            }
            return super.calculateMaximumSize(axis);
        }

        @Override
        void setValidSize(int axis, float origin, float size) {
            for (Spring spring : springs) {
                setChildSize(spring, axis, origin, size);
            }
        }

        void setChildSize(Spring spring, int axis, float origin, float size) {
            Alignment alignment = spring.getAlignment();
            float springSize = Math.min(
                    Math.max(spring.getMinimumSize(axis), size),
                    spring.getMaximumSize(axis));            
            if (alignment == null) {
                alignment = childAlignment;
            }
            switch (alignment) {
                case TRAILING:
                    spring.setSize(axis, origin + size - springSize,
                            springSize);
                    break;
                case CENTER:
                    spring.setSize(axis, origin +
                            (size - springSize) / 2,springSize);
                    break;
                default: // LEADING, ó BASELINE
                    spring.setSize(axis, origin, springSize);
                    break;
            }
        }

        @Override
        void insertAutopadding(int axis,
                List<AutoPreferredGapSpring> leadingPadding,
                List<AutoPreferredGapSpring> trailingPadding,
                List<ComponentSpring> leading, List<ComponentSpring> trailing,
                boolean insert) {
            for (Spring spring : springs) {
                if (spring instanceof ComponentSpring) {
                    if (((ComponentSpring)spring).isVisible()) {
                        for (AutoPreferredGapSpring gapSpring :
                                 leadingPadding) {
                            gapSpring.addTarget((ComponentSpring)spring, axis);
                        }
                        trailing.add((ComponentSpring)spring);
                    }
                } else if (spring instanceof Group) {
                    ((Group)spring).insertAutopadding(axis, leadingPadding,
                            trailingPadding, leading, trailing, insert);
                } else if (spring instanceof AutoPreferredGapSpring) {
                    ((AutoPreferredGapSpring)spring).setSources(leading);
                    trailingPadding.add((AutoPreferredGapSpring)spring);
                }
            }
        }

        private void checkChildAlignment(Alignment alignment) {
            checkChildAlignment(alignment, (this instanceof BaselineGroup));
        }

        private void checkChildAlignment(Alignment alignment,
                boolean allowsBaseline) {
            if (alignment == null) {
                throw new IllegalArgumentException("Alignment must be non-null");
            }
            if (!allowsBaseline && alignment == Alignment.BASELINE) {
                throw new IllegalArgumentException("Alignment must be one of:" +
                        "LEADING, TRAILING or CENTER");
            }
        }
    }
    
    private class BaselineGroup extends ParallelGroup {
        private boolean allSpringsHaveBaseline;
        
        private float prefAscent;
        private float prefDescent;

        private boolean baselineAnchorSet;
        private boolean baselineAnchoredToTop;
        private boolean calcedBaseline;

        BaselineGroup(boolean resizable) {
            super(Alignment.LEADING, resizable);
            prefAscent = prefDescent = -1;
            calcedBaseline = false;
        }

        BaselineGroup(boolean resizable, boolean baselineAnchoredToTop) {
            this(resizable);
            this.baselineAnchoredToTop = baselineAnchoredToTop;
            baselineAnchorSet = true;
        }

        @Override
        void unset() {
            super.unset();
            prefAscent = prefDescent = -1;
            calcedBaseline = false;
        }

        @Override
        void setValidSize(int axis, float origin, float size) {
            checkAxis(axis);
            if (prefAscent == -1) {
                super.setValidSize(axis, origin, size);
            } else {
                baselineLayout(origin, size);
            }
        }

        @Override
        float calculateSize(int axis, int type) {
            checkAxis(axis);
            if (!calcedBaseline) {
                calculateBaselineAndResizeBehavior();
            }
            if (type == MIN_SIZE) {
                return calculateMinSize();
            }
            if (type == MAX_SIZE) {
                return calculateMaxSize();
            }
            if (allSpringsHaveBaseline) {
                return prefAscent + prefDescent;
            }
            return Math.max(prefAscent + prefDescent,
                    super.calculateSize(axis, type));
        }

        private void calculateBaselineAndResizeBehavior() {
            prefAscent = 0;
            prefDescent = 0;
            float baselineSpringCount = 0;
            BaselineResizeBehavior resizeBehavior = null;
            for (Spring spring : springs) {
                if (spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) {
                    float baseline = spring.getBaseline();
                    if (baseline >= 0) {
                        if (spring.isResizable(VERTICAL)) {
                            BaselineResizeBehavior brb = spring.
                                    getBaselineResizeBehavior();
                            if (resizeBehavior == null) {
                                resizeBehavior = brb;
                            } else if (brb != resizeBehavior) {
                                resizeBehavior = BaselineResizeBehavior.
                                        CONSTANT_ASCENT;
                            }
                        }
                        prefAscent = Math.max(prefAscent, baseline);
                        prefDescent = Math.max(prefDescent, spring.
                                getPreferredSize(VERTICAL) - baseline);
                        baselineSpringCount++;
                    }
                }
            }
            if (!baselineAnchorSet) {
                this.baselineAnchoredToTop = resizeBehavior != BaselineResizeBehavior.CONSTANT_DESCENT;
            }
            allSpringsHaveBaseline = (baselineSpringCount == springs.size());
            calcedBaseline = true;
        }

        private float calculateMaxSize() {
            float maxAscent = prefAscent;
            float maxDescent = prefDescent;
            float nonBaselineMax = 0;
            for (Spring spring : springs) {
                float baseline;
                float springMax = spring.getMaximumSize(VERTICAL);
                if ((spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) &&
                        (baseline = spring.getBaseline()) >= 0) {
                    float springPref = spring.getPreferredSize(VERTICAL);
                    if (springPref != springMax) {
                        switch (spring.getBaselineResizeBehavior()) {
                            case CONSTANT_ASCENT:
                                if (baselineAnchoredToTop) {
                                    maxDescent = Math.max(maxDescent,
                                            springMax - baseline);
                                }
                                break;
                            case CONSTANT_DESCENT:
                                if (!baselineAnchoredToTop) {
                                    maxAscent = Math.max(maxAscent,
                                            springMax - springPref + baseline);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    nonBaselineMax = Math.max(nonBaselineMax, springMax);
                }
            }
            return Math.max(nonBaselineMax, maxAscent + maxDescent);
        }

        private float calculateMinSize() {
            float minAscent = 0;
            float minDescent = 0;
            float nonBaselineMin = 0;
            if (baselineAnchoredToTop) {
                minAscent = prefAscent;
            } else {
                minDescent = prefDescent;
            }
            for (Spring spring : springs) {
                float springMin = spring.getMinimumSize(VERTICAL);
                float baseline;
                if ((spring.getAlignment() == null ||
                        spring.getAlignment() == Alignment.BASELINE) &&
                        (baseline = spring.getBaseline()) >= 0) {
                    float springPref = spring.getPreferredSize(VERTICAL);
                    BaselineResizeBehavior brb = spring.
                            getBaselineResizeBehavior();
                    switch (brb) {
                        case CONSTANT_ASCENT:
                            if (baselineAnchoredToTop) {
                                minDescent = Math.max(springMin - baseline,
                                        minDescent);
                            } else {
                                minAscent = Math.max(baseline, minAscent);
                            }
                            break;
                        case CONSTANT_DESCENT:
                            if (!baselineAnchoredToTop) {
                                minAscent = Math.max(
                                        baseline - (springPref - springMin),
                                        minAscent);
                            } else {
                                minDescent = Math.max(springPref - baseline,
                                        minDescent);
                            }
                            break;
                        default:
                            minAscent = Math.max(baseline, minAscent);
                            minDescent = Math.max(springPref - baseline,
                                    minDescent);
                            break;
                    }
                } else {
                    nonBaselineMin = Math.max(nonBaselineMin, springMin);
                }
            }
            return Math.max(nonBaselineMin, minAscent + minDescent);
        }
        
        private void baselineLayout(float origin, float size) {
            float ascent;
            float descent;
            if (baselineAnchoredToTop) {
                ascent = prefAscent;
                descent = size - ascent;
            } else {
                ascent = size - prefDescent;
                descent = prefDescent;
            }
            for (Spring spring : springs) {
                Alignment alignment = spring.getAlignment();
                if (alignment == null || alignment == Alignment.BASELINE) {
                    float baseline = spring.getBaseline();
                    if (baseline >= 0) {
                        float springMax = spring.getMaximumSize(VERTICAL);
                        float springPref = spring.getPreferredSize(VERTICAL);
                        float height = springPref;
                        float y;
                        switch(spring.getBaselineResizeBehavior()) {
                            case CONSTANT_ASCENT:
                                y = origin + ascent - baseline;
                                height = Math.min(descent, springMax -
                                        baseline) + baseline;
                                break;
                            case CONSTANT_DESCENT:
                                height = Math.min(ascent, springMax -
                                        springPref + baseline) +
                                        (springPref - baseline);
                                y = origin + ascent +
                                        (springPref - baseline) - height;
                                break;
                            default: // CENTER_OFFSET & OTHER, not resizable
                                y = origin + ascent - baseline;
                                break;
                        }
                        spring.setSize(VERTICAL, y, height);
                    } else {
                        setChildSize(spring, VERTICAL, origin, size);
                    }
                } else {
                    setChildSize(spring, VERTICAL, origin, size);
                }
            }
        }

        @Override
        float getBaseline() {
            if (springs.size() > 1) {
                getPreferredSize(VERTICAL);
                return prefAscent;
            } else if (springs.size() == 1) {
                return springs.get(0).getBaseline();
            }
            return -1;
        }

        @Override
        BaselineResizeBehavior getBaselineResizeBehavior() {
            if (springs.size() == 1) {
                return springs.get(0).getBaselineResizeBehavior();
            }
            if (baselineAnchoredToTop) {
                return BaselineResizeBehavior.CONSTANT_ASCENT;
            }
            return BaselineResizeBehavior.CONSTANT_DESCENT;
        }

        private void checkAxis(int axis) {
            if (axis == HORIZONTAL) {
                throw new IllegalStateException(
                        "Baseline must be used along vertical axis");
            }
        }
    }
    
    private final class ComponentSpring extends Spring {
        private Node component;
        private float origin;
        
        private final float min;
        private final float pref;
        private final float max;
        
        private float baseline = -1;
        
        private boolean installed;

        private ComponentSpring(Node component, float min, float pref,
                float max) {
            this.component = component;
            if (component == null) {
                throw new IllegalArgumentException(
                        "Component must be non-null");
            }

            checkSize(min, pref, max, true);

            this.min = min;
            this.max = max;
            this.pref = pref;
            
            getComponentInfo(component);
        }

        @Override
        float calculateMinimumSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MIN_SIZE);
            }
            return calculateNonlinkedMinimumSize(axis);
        }

        @Override
        float calculatePreferredSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, PREF_SIZE);
            }
            float min0 = getMinimumSize(axis);
            float pref0 = calculateNonlinkedPreferredSize(axis);
            float max0 = getMaximumSize(axis);
            return Math.min(max0, Math.max(min0, pref0));
        }

        @Override
        float calculateMaximumSize(int axis) {
            if (isLinked(axis)) {
                return getLinkSize(axis, MAX_SIZE);
            }
            return Math.max(getMinimumSize(axis),
                    calculateNonlinkedMaximumSize(axis));
        }

        boolean isVisible() {
            return getComponentInfo(getComponent()).isVisible();
        }

        float calculateNonlinkedMinimumSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (min >= 0) {
                return min;
            }
            if (min == PREFERRED_SIZE) {
                return calculateNonlinkedPreferredSize(axis);
            }
            assert (min == DEFAULT_SIZE);            
            final Vector3f dim = component.getControl(GuiControl.class).getPreferredSize();
            return getSizeAlongAxis(axis, dim);
        }

        float calculateNonlinkedPreferredSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (pref >= 0) {
                return pref;
            }
            assert (pref == DEFAULT_SIZE || pref == PREFERRED_SIZE);
            return getSizeAlongAxis(axis, component.getControl(GuiControl.class).getPreferredSize());
        }

        float calculateNonlinkedMaximumSize(int axis) {
            if (!isVisible()) {
                return 0;
            }
            if (max >= 0) {
                return max;
            }
            if (max == PREFERRED_SIZE) {
                return calculateNonlinkedPreferredSize(axis);
            }
            assert (max == DEFAULT_SIZE);
            /*return getSizeAlongAxis(axis, component.getMaximumSize());*/
            return getSizeAlongAxis(axis, component.getControl(GuiControl.class).getPreferredSize());
        }

        private float getSizeAlongAxis(int axis, Vector3f size) {
            return (axis == HORIZONTAL) ? size.x : size.y;
        }

        private float getLinkSize(int axis, int type) {
            if (!isVisible()) {
                return 0;
            }
            ComponentInfo ci = getComponentInfo(component);
            return ci.getLinkSize(axis, type);
        }

        @Override
        void setSize(int axis, float origin, float size) {
            super.setSize(axis, origin, size);
            this.origin = origin;
            if (size == UNSET) {
                baseline = -1;
            }
        }

        float getOrigin() {
            return origin;
        }

        void setComponent(Node component) {
            this.component = component;
        }

        Node getComponent() {
            return component;
        }

        @Override
        float getBaseline() {
            if (baseline == -1) {
                Spring horizontalSpring = getComponentInfo(component).
                        horizontalSpring;
                float width = horizontalSpring.getPreferredSize(HORIZONTAL);
                float height = getPreferredSize(VERTICAL);
                if (width > 0 && height > 0) {
                    Number number = component.getUserData(Jme3HudlConstants.BASELINE);
                    if (number != null) {
                        if (number instanceof Float) {
                            baseline = (Float) number;
                        } else {
                            baseline = number.floatValue();
                        }
                    }
                    baseline = -1;
                }
            }
            return baseline;
        }

        @Override
        BaselineResizeBehavior getBaselineResizeBehavior() {
            BaselineResizeBehavior behavior = getComponent().getUserData(Jme3HudlConstants.BASELINE_RESIZE_BEHAVIOR);
            if (behavior == null) {
                return BaselineResizeBehavior.OTHER;
            }
            return behavior;
        }

        private boolean isLinked(int axis) {
            return getComponentInfo(component).isLinked(axis);
        }

        void installIfNecessary(int axis) {
            if (!installed) {
                installed = true;
                if (axis == HORIZONTAL) {
                    getComponentInfo(component).horizontalSpring = this;
                } else {
                    getComponentInfo(component).verticalSpring = this;
                }
            }
        }

        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return !isVisible();
        }
    }
    
    private class PreferredGapSpring extends Spring {
        private final Node  source;
        private final Node  target;
        private final ComponentPlacement type;
        private final float pref;
        private final float max;

        PreferredGapSpring(Node source,Node  target,
                ComponentPlacement type, float pref, float max) {
            this.source = source;
            this.target = target;
            this.type = type;
            this.pref = pref;
            this.max = max;
        }

        @Override
        float calculateMinimumSize(int axis) {
            return getPadding(axis);
        }

        @Override
        float calculatePreferredSize(int axis) {
            if (pref == DEFAULT_SIZE || pref == PREFERRED_SIZE) {
                return getMinimumSize(axis);
            }
            float min0 = getMinimumSize(axis);
            float max0 = getMaximumSize(axis);
            return Math.min(max0, Math.max(min0, pref));
        }

        @Override
        float calculateMaximumSize(int axis) {
            if (max == PREFERRED_SIZE || max == DEFAULT_SIZE) {
                return getPadding(axis);
            }
            return Math.max(getMinimumSize(axis), max);
        }

        private float getPadding(int axis) {
            int position;
            if (axis == HORIZONTAL) {
                position = Jme3HudlConstants.EAST;
            } else {
                position = Jme3HudlConstants.SOUTH;
            }
            return getLayoutStyle0().getPreferredGap(source,
                    target, type, position, host);
        }

        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return false;
        }
    }
    
    private class GapSpring extends Spring {
        private final float min;
        private final float pref;
        private final float max;

        GapSpring(float min, float pref, float max) {
            checkSize(min, pref, max, false);
            this.min = min;
            this.pref = pref;
            this.max = max;
        }

        @Override
        float calculateMinimumSize(int axis) {
            if (min == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return min;
        }

        @Override
        float calculatePreferredSize(int axis) {
            return pref;
        }

        @Override
        float calculateMaximumSize(int axis) {
            if (max == PREFERRED_SIZE) {
                return getPreferredSize(axis);
            }
            return max;
        }

        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return false;
        }
    }
    
    private class AutoPreferredGapSpring extends Spring {
        List<ComponentSpring> sources;
        ComponentSpring source;
        private List<AutoPreferredGapMatch> matches;
        float size;
        float lastSize;
        private final float pref;
        private final float max;
        
        private ComponentPlacement type;
        private boolean userCreated;

        private AutoPreferredGapSpring() {
            this.pref = PREFERRED_SIZE;
            this.max = PREFERRED_SIZE;
            this.type = ComponentPlacement.RELATED;
        }

        AutoPreferredGapSpring(float pref, float max) {
            this.pref = pref;
            this.max = max;
        }

        AutoPreferredGapSpring(ComponentPlacement type, float pref, float max) {
            this.type = type;
            this.pref = pref;
            this.max = max;
            this.userCreated = true;
        }

        public void setSource(ComponentSpring source) {
            this.source = source;
        }

        public void setSources(List<ComponentSpring> sources) {
            this.sources = new ArrayList<>(sources);
        }

        public void setUserCreated(boolean userCreated) {
            this.userCreated = userCreated;
        }

        public boolean getUserCreated() {
            return userCreated;
        }

        @Override
        void unset() {
            lastSize = getSize();
            super.unset();
            size = 0;
        }

        public void reset() {
            size = 0;
            sources = null;
            source = null;
            matches = null;
        }

        public void calculatePadding(int axis) {
            size = UNSET;
            float maxPadding = UNSET;
            if (matches != null) {
                LayoutStyle p = getLayoutStyle0();
                int position;
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = Jme3HudlConstants.EAST;
                    } else {
                        position = Jme3HudlConstants.WEST;
                    }
                } else {
                    position = Jme3HudlConstants.SOUTH;
                }
                for (int i = matches.size() - 1; i >= 0; i--) {
                    AutoPreferredGapMatch match = matches.get(i);
                    maxPadding = Math.max(maxPadding,
                            calculatePadding(p, position, match.source,
                            match.target));
                }
            }
            if (size == UNSET) {
                size = 0;
            }
            if (maxPadding == UNSET) {
                maxPadding = 0;
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }

        private float calculatePadding(LayoutStyle p, int position,
                ComponentSpring source,
                ComponentSpring target) {
            float delta = target.getOrigin() - (source.getOrigin() +
                    source.getSize());
            if (delta >= 0) {
                int padding;
                if ((source.getComponent() != null) &&
                        (target.getComponent() != null)) {
                    padding = p.getPreferredGap(
                            source.getComponent(),
                            target.getComponent(), type, position,
                            host);
                } else {
                    padding = 10;
                }
                if (padding > delta) {
                    size = Math.max(size, padding - delta);
                }
                return padding;
            }
            return 0;
        }

        public void addTarget(ComponentSpring spring, int axis) {
            int oAxis = (axis == HORIZONTAL) ? VERTICAL : HORIZONTAL;
            if (source != null) {
                if (areParallelSiblings(source.getComponent(),
                        spring.getComponent(), oAxis)) {
                    addValidTarget(source, spring);
                }
            } else {
                Node component = spring.getComponent();
                for (int counter = sources.size() - 1; counter >= 0;
                         counter--){
                    ComponentSpring getSource = sources.get(counter);
                    if (areParallelSiblings(getSource.getComponent(),
                            component, oAxis)) {
                        addValidTarget(getSource, spring);
                    }
                }
            }
        }

        private void addValidTarget(ComponentSpring source,
                ComponentSpring target) {
            if (matches == null) {
                matches = new ArrayList<>(1);
            }
            matches.add(new AutoPreferredGapMatch(source, target));
        }

        @Override
        float calculateMinimumSize(int axis) {
            return size;
        }

        @Override
        float calculatePreferredSize(int axis) {
            if (pref == PREFERRED_SIZE || pref == DEFAULT_SIZE) {
                return size;
            }
            return Math.max(size, pref);
        }

        @Override
        float calculateMaximumSize(int axis) {
            if (max >= 0) {
                return Math.max(getPreferredSize(axis), max);
            }
            return size;
        }

        String getMatchDescription() {
            return (matches == null) ? "" : matches.toString();
        }

        @Override
        public String toString() {
            return super.toString() + getMatchDescription();
        }

        @Override
        boolean willHaveZeroSize(boolean treatAutopaddingAsZeroSized) {
            return treatAutopaddingAsZeroSized;
        }
    }
    
    private static final class AutoPreferredGapMatch {
        public final ComponentSpring source;
        public final ComponentSpring target;

        AutoPreferredGapMatch(ComponentSpring source, ComponentSpring target) {
            this.source = source;
            this.target = target;
        }

        private String toString(ComponentSpring spring) {
            return spring.getComponent().getName();
        }

        @Override
        public String toString() {
            return "[" + toString(source) + "-" + toString(target) + "]";
        }
    }
    
    private class ContainerAutoPreferredGapSpring extends
            AutoPreferredGapSpring {
        private List<ComponentSpring> targets;

        ContainerAutoPreferredGapSpring() {
            super();
            ContainerAutoPreferredGapSpring.this.setUserCreated(true);
        }

        ContainerAutoPreferredGapSpring(float pref, float max) {
            super(pref, max);
            ContainerAutoPreferredGapSpring.this.setUserCreated(true);
        }

        @Override
        public void addTarget(ComponentSpring spring, int axis) {
            if (targets == null) {
                targets = new ArrayList<>(1);
            }
            targets.add(spring);
        }

        @Override
        public void calculatePadding(int axis) {
            LayoutStyle p = getLayoutStyle0();
            float maxPadding = 0;
            int position;
            size = 0;
            if (targets != null) {
                // Leading
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = Jme3HudlConstants.WEST;
                    } else {
                        position = Jme3HudlConstants.EAST;
                    }
                } else {
                    position = Jme3HudlConstants.SOUTH;
                }
                for (int i = targets.size() - 1; i >= 0; i--) {
                    ComponentSpring targetSpring = targets.get(i);
                    int padding = 10;
                    if (targetSpring.getComponent() != null) {
                        padding = p.getContainerGap(
                                targetSpring.getComponent(),
                                position, host);
                        maxPadding = Math.max(padding, maxPadding);
                        padding -= targetSpring.getOrigin();
                    } else {
                        maxPadding = Math.max(padding, maxPadding);
                    }
                    size = Math.max(size, padding);
                }
            } else {
                // Trailing
                if (axis == HORIZONTAL) {
                    if (isLeftToRight()) {
                        position = Jme3HudlConstants.EAST;
                    } else {
                        position = Jme3HudlConstants.WEST;
                    }
                } else {
                    position = Jme3HudlConstants.SOUTH;
                }
                if (sources != null) {
                    for (int i = sources.size() - 1; i >= 0; i--) {
                        ComponentSpring sourceSpring = sources.get(i);
                        maxPadding = Math.max(maxPadding,
                                updateSize(p, sourceSpring, position));
                    }
                } else if (source != null) {
                    maxPadding = updateSize(p, source, position);
                }
            }
            if (lastSize != UNSET) {
                size += Math.min(maxPadding, lastSize);
            }
        }

        private float updateSize(LayoutStyle p, ComponentSpring sourceSpring,
                int position) {
            float padding = 10;
            if (sourceSpring.getComponent() != null) {
                padding = p.getContainerGap(
                        sourceSpring.getComponent(), position,
                        host);
            }
            float delta = Math.max(0, getParent().getSize() -
                    sourceSpring.getSize() - sourceSpring.getOrigin());
            size = Math.max(size, padding - delta);
            return padding;
        }

        @Override
        String getMatchDescription() {
            if (targets != null) {
                return "leading: " + targets.toString();
            }
            if (sources != null) {
                return "trailing: " + sources.toString();
            }
            return "--";
        }
    }
    
    private static class LinkInfo {
        private final int axis;
        private final List<ComponentInfo> linked;
        private float size;

        LinkInfo(int axis) {
            linked = new ArrayList<>();
            size = UNSET;
            this.axis = axis;
        }

        public void add(ComponentInfo child) {
            LinkInfo childMaster = child.getLinkInfo(axis, false);
            if (childMaster == null) {
                linked.add(child);
                child.setLinkInfo(axis, this);
            } else if (childMaster != this) {
                linked.addAll(childMaster.linked);
                for (ComponentInfo childInfo : childMaster.linked) {
                    childInfo.setLinkInfo(axis, this);
                }
            }
            clearCachedSize();
        }

        public void remove(ComponentInfo info) {
            linked.remove(info);
            info.setLinkInfo(axis, null);
            if (linked.size() == 1) {
                linked.get(0).setLinkInfo(axis, null);
            }
            clearCachedSize();
        }

        public void clearCachedSize() {
            size = UNSET;
        }

        public float getSize(int axis) {
            if (size == UNSET) {
                size = calculateLinkedSize(axis);
            }
            return size;
        }

        private float calculateLinkedSize(int axis) {
            float size0 = 0;
            for (ComponentInfo info : linked) {
                ComponentSpring spring;
                if (axis == HORIZONTAL) {
                    spring = info.horizontalSpring;
                } else {
                    assert (axis == VERTICAL);
                    spring = info.verticalSpring;
                }
                size0 = Math.max(size0,
                        spring.calculateNonlinkedPreferredSize(axis));
            }
            return size0;
        }
    }
    
    private class ComponentInfo {
        private Node component;

        ComponentSpring horizontalSpring;
        ComponentSpring verticalSpring;
        
        private LinkInfo horizontalMaster;
        private LinkInfo verticalMaster;

        private boolean visible;
        private Boolean honorsVisibility;

        ComponentInfo(Node component) {
            this.component = component;
            ComponentInfo.this.updateVisibility();
        }

        public void dispose() {
            removeSpring(horizontalSpring);
            horizontalSpring = null;
            removeSpring(verticalSpring);
            verticalSpring = null;
            
            if (horizontalMaster != null) {
                horizontalMaster.remove(this);
            }
            if (verticalMaster != null) {
                verticalMaster.remove(this);
            }
        }

        void setHonorsVisibility(Boolean honorsVisibility) {
            this.honorsVisibility = honorsVisibility;
        }

        private void removeSpring(Spring spring) {
            if (spring != null) {
                ((Group)spring.getParent()).springs.remove(spring);
            }
        }

        public boolean isVisible() {
            return visible;
        }
        
        boolean updateVisibility() {
            boolean honorsVisibility0;
            if (this.honorsVisibility == null) {
                honorsVisibility0 = GroupLayout.this.getHonorsVisibility();
            } else {
                honorsVisibility0 = this.honorsVisibility;
            }
            boolean newVisible = (honorsVisibility0) ?
                (component.getParent() != null) : true;
            if (visible != newVisible) {
                visible = newVisible;
                return true;
            }
            return false;
        }

        public void setBounds(Insets3f insets, float parentWidth, boolean ltr) {
            float x = horizontalSpring.getOrigin();
            float w = horizontalSpring.getSize();
            float y = verticalSpring.getOrigin();
            float h = verticalSpring.getSize();

            if (!ltr) {
                x = parentWidth - x - w;
            }

            component.setLocalTranslation(x + insets.min.x, y - insets.max.y,
                    component.getLocalTranslation().z);
            
            GuiControl control = component.getControl(GuiControl.class);
            control.setSize(new Vector3f(w, h, 
                    control.getPreferredSize().z));
        }

        public void setComponent(Node component) {
            this.component = component;
            if (horizontalSpring != null) {
                horizontalSpring.setComponent(component);
            }
            if (verticalSpring != null) {
                verticalSpring.setComponent(component);
            }
        }

        public Node getComponent() {
            return component;
        }
        
        public boolean isLinked(int axis) {
            if (axis == HORIZONTAL) {
                return horizontalMaster != null;
            }
            assert (axis == VERTICAL);
            return (verticalMaster != null);
        }

        private void setLinkInfo(int axis, LinkInfo linkInfo) {
            if (axis == HORIZONTAL) {
                horizontalMaster = linkInfo;
            } else {
                assert (axis == VERTICAL);
                verticalMaster = linkInfo;
            }
        }

        public LinkInfo getLinkInfo(int axis) {
            return getLinkInfo(axis, true);
        }

        private LinkInfo getLinkInfo(int axis, boolean create) {
            if (axis == HORIZONTAL) {
                if (horizontalMaster == null && create) {
                    new LinkInfo(HORIZONTAL).add(this);
                }
                return horizontalMaster;
            } else {
                assert (axis == VERTICAL);
                if (verticalMaster == null && create) {
                    new LinkInfo(VERTICAL).add(this);
                }
                return verticalMaster;
            }
        }

        public void clearCachedSize() {
            if (horizontalMaster != null) {
                horizontalMaster.clearCachedSize();
            }
            if (verticalMaster != null) {
                verticalMaster.clearCachedSize();
            }
        }

        float getLinkSize(int axis, int type) {
            if (axis == HORIZONTAL) {
                return horizontalMaster.getSize(axis);
            } else {
                assert (axis == VERTICAL);
                return verticalMaster.getSize(axis);
            }
        }
    }
}
