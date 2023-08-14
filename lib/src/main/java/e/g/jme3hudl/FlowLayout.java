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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Un diseño de flujo organiza los componentes en un flujo direccional, se
 * parece mucho a como líneas de texto en un párrafo.
 * <p>
 * Los diseños de flujo se utilizan normalmente para organizar los botones en un
 * panel. Ordena los botones horizontalmente hasta que no quepan más botones en
 * la misma línea. La alineación de línea está determinada por la propiedad
 * {@code align}. Los valores posibles son:
 * <ul>
 * <li>{@link #LEFT LEFT}
 * <li>{@link #RIGHT RIGHT}
 * <li>{@link #CENTER CENTER}
 * <li>{@link #LEADING LEADING}
 * <li>{@link #TRAILING TRAILING}
 * </ul>
 * </p>
 * <p>
 * Un diseño de flujo permite que cada componente asuma su tamaño natural (preferido).
 * </p>
 * 
 * @author wil
 * @version 1.0-SNAPSHOT
 * 
 * @since 1.0.0
 */
public class FlowLayout extends AbstractGuiComponent implements GuiLayout {
    
    /**
     * Este valor indica que cada fila de componentes
     * debe estar justificado a la izquierda.
     */
    public static final short LEFT = 0;
    
    /**
     * Este valor indica que cada fila de componentes
     * debe estar centrado.
     */
    public static final short CENTER = 1;
    
    /**
     * Este valor indica que cada fila de componentes
     * debe estar justificado a la derecha.
     */
    public static final short RIGHT = 2;
    
    /**
     * Este valor indica que cada fila de componentes
     * debe justificarse hasta el borde de ataque del contenedor.
     * <p>
     * Por ejemplo, a la izquierda en orientaciones de izquierda a derecha.
     * </p>
     */
    public static final short LEADING = 3;
    
    /**
     * Este valor indica que cada fila de componentes
     * debe justificarse hasta el borde posterior del contenedor.
     * <p>
     * Por ejemplo, a la derecha en orientaciones de izquierda a derecha.
     * </p>
     */
    public static final short TRAILING = 4;
    
    /** Lista de hijos que contiene el nodo padre. */
    private final List<Node> children = new ArrayList<>();
    
    /**
     * {@code align} es la propiedad que determina cómo cada fila distribuye el 
     * espacio vacío.
     * <p>
     * Puede ser uno de los siguientes valores:
     * <ul>
     * <li>{@code LEFT}
     * <li>{@code RIGHT}
     * <li>{@code CENTER}
     * <li>{@code LEADING}
     * <li>{@code TRAILING}
     * </ul>
     * </p>
     */
    short align;
    
    /**
     * El administrador de diseño de flujo permite una separación de componentes
     * con huecos. Los espacios se especifican el espacio entre componentes y 
     * los bordes.
     */
    Vector3f gap;

    /**
     * Construye un nuevo <code>FlowLayout</code> con una alineación centrada y
     * una separación predeterminada de 5 unidades.
     */
    public FlowLayout() {
        this(CENTER, new Vector3f(5.0F, 5.0F, 0.0F));
    }

    /**
     * Construye un nuevo <code>FlowLayout</code> una alineación especificada
     * con una separación predeterminado de 5 unidades.
     * <p>
     * El valor del argumento de alineación debe ser:
     * {@code FlowLayout.LEFT}, {@code FlowLayout.RIGHT},
     * {@code FlowLayout.CENTER}, {@code FlowLayout.LEADING},
     * o {@code FlowLayout.TRAILING}.
     * </p>
     * @param align el valor de alineación
     */
    public FlowLayout(short align) {
        this(align, new Vector3f(5.0F, 5.0F, 5.0F));
    }
    
    /**
     * Crea un nuevo administrador <code>FlowLayout</code> de diseño de flujo 
     * con la alineación indicada y los espacios indicados.
     *
     * <p>
     * El valor del argumento de alineación debe ser uno de
     * {@code FlowLayout.LEFT}, {@code FlowLayout.RIGHT},
     * {@code FlowLayout.CENTER}, {@code FlowLayout.LEADING},
     * o {@code FlowLayout.TRAILING}.
     * </p>
     * 
     * @param align el valor de alineación
     * @param gap los espacios entre componentes y los bordes del 
     * {@code Container} padre.
     */
    public FlowLayout(short align, Vector3f gap) {
        this.align = align;
        this.gap = gap;
    }

    /**
     * (non-JavaDoc)
     * @param size vector-3f
     * @see GuiLayout#calculatePreferredSize(com.jme3.math.Vector3f) 
     */
    @Override
    public void calculatePreferredSize(Vector3f size) {
        Vector3f dim = new Vector3f(0.0F, 0.0F, 0.0F);
        boolean firstVisibleComponent = true;

        for (Node m : children) {
            if (m.getParent() != null) {
                Vector3f d = m.getControl(GuiControl.class).getPreferredSize();
                dim.y = Math.max(dim.y, d.y);
                dim.z = Math.max(dim.z, d.z);
                if (firstVisibleComponent) {
                    firstVisibleComponent = false;
                } else {
                    dim.x += gap.x;
                }
            }
        }
        
        Insets3f insets = ((Panel) getNode()).getInsets();
        if (insets == null) {
            insets = new Insets3f(new Vector3f(), new Vector3f());
        }
        
        dim.x += insets.min.x + insets.max.x + gap.x*2;
        dim.y += insets.min.y + insets.max.y + gap.y*2;
        dim.z += insets.min.z + insets.max.z + gap.z*2;
        size.set(dim);
    }
    
    /**
     * Centra los elementos en la fila especificada, si hay holgura.
     * @param target el componente que necesita ser movido
     * @param x la coordenada {@code x}.
     * @param y la coordenada {@code y}.
     * @param z la coordenada {@code z}.
     * @param width las dimensiones del ancho
     * @param height las dimensiones del largo
     * @param rowStart el comienzo de la fila
     * @param rowEnd the ending of the row
     * @return altura de fila real
     */
    private int moveComponents(Node target, float x, float y, float z, float width, float height,
                                int rowStart, int rowEnd, boolean ltr) {
        switch (align) {
            case LEFT    -> x += ltr ? 0 : width;
            case CENTER  -> x += width / 2;
            case RIGHT   -> x += ltr ? width : 0;
            case LEADING -> {
            }
            case TRAILING -> x += width;
            default -> throw new AssertionError();
        }
        
        for (int i = rowStart; i < rowEnd; i++) {
            Node m = children.get(i);
            if (m.getParent() != null) {
                float cy = -(y + (height - m.getControl(GuiControl.class).getSize().y) / 2);
                if ( ltr ) {
                    m.setLocalTranslation(x, cy, z);
                } else {
                    m.setLocalTranslation(target.getControl(GuiControl.class).getSize().x - x - m.getControl(GuiControl.class).getSize().x, cy, z);
                }
                x += m.getControl(GuiControl.class).getSize().x + gap.x;
            }
        }
        return Float.valueOf(height).intValue();
    }
    
    /**
     * (non-JavaDoc)
     * @param pos vector-3f
     * @param size vector-3f
     * @see GuiLayout#reshape(com.jme3.math.Vector3f, com.jme3.math.Vector3f) 
     */
    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        Insets3f insets = ((Panel) getNode()).getInsets();
        if (insets == null) {
            insets = new Insets3f(new Vector3f(), new Vector3f());
        }
        GuiControl target = getGuiControl();
        
        float maxwidth = target.getSize().x - (insets.min.x + insets.max.x + gap.x * 2);
        float x = 0, y = insets.max.y + gap.y, z = insets.max.z + gap.z;
        
        int nmembers = children.size();
        int rowh = 0, start = 0;
        
        boolean ltr = false;
        for (int i = 0; i < nmembers; i++) {
            Node m = children.get(i);
            if (m.getParent() != null) {
                Vector3f d = m.getControl(GuiControl.class).getPreferredSize();
                m.getControl(GuiControl.class).setSize(d);
                
                if ((x == 0) || ((x + d.x) <= maxwidth)) {
                    if (x > 0) {
                        x += gap.x;
                    }
                    x += d.x;
                    rowh = Math.max(rowh, Float.valueOf(d.y).intValue());
                } else {
                    rowh = moveComponents(target.getNode(), insets.min.x + gap.x, y, z, maxwidth - x, rowh, start, i, ltr);
                    x = d.x;
                    y += gap.y + rowh;
                    rowh = Float.valueOf(d.y).intValue();
                    start = i;
                }
            }
        }
        
        moveComponents(target.getNode(), insets.min.x + gap.x, y, z, maxwidth - x, rowh, start, nmembers, ltr);
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#addChild(com.jme3.scene.Node, java.lang.Object...) 
     * 
     * @param <T> tipo-componente
     * @param n componente
     * @param constraints parámetros
     * @return componente
     */
    @Override
    public <T extends Node> T addChild(T n, Object... constraints) {
        if( n != null && n.getControl(GuiControl.class) == null ) {
            throw new IllegalArgumentException( "Child is not GUI element." );
        }
        
        if (constraints.length > 0) {
            throw new IllegalArgumentException("Invalid parameters.");
        }
        
        if (!children.contains(n)) {
            children.add(n);
        }
        if (isAttached()) {
            getNode().attachChild(n);
        }
        invalidate();
        return n;
    }

    /**
     * (non-JavaDoc)
     * @param n nodo
     * @see GuiLayout#removeChild(com.jme3.scene.Node) 
     */
    @Override
    public void removeChild(Node n) {
        if (children.contains(n) 
                && children.remove(n)) {
            n.removeFromParent();
            invalidate();
        }
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#getChildren() 
     * @see CardLayout#getLayoutChildren() 
     * 
     * @return list
     */
    @Override
    public Collection<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#clearChildren() 
     */
    @Override
    public void clearChildren() {
        for (final Node entry : children) {
            entry.removeFromParent();
        }
        children.clear();
        invalidate();
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#detach(com.simsilica.lemur.core.GuiControl) 
     * @param parent gui-control
     */
    @Override
    public void detach(GuiControl parent) {
        super.detach(parent);
        Collection<Node> copy = new ArrayList<>(children);    
        for( Node n : copy ) {
            n.removeFromParent();
        }
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#attach(com.simsilica.lemur.core.GuiControl) 
     * @param parent gui-control
     */
    @Override
    public void attach(GuiControl parent) {
        super.attach(parent);
        for ( Node n : children ) {
            getNode().attachChild(n);
        }
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#clone() 
     * @return <code>null</code>.
     * @throws UnsupportedOperationException Este método no soporta la
     * clonación de clases u objetos.
     */
    @Override
    public GuiLayout clone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
