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
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Un <code>NullLayout</code> se utiliza cuando es necesario sobreponer
 * componentes uno tras otro. Este diseño no proporciona ningun calculo
 * para redimensionar componentes hijos.
 * <p>
 * Este diseño actua como:
 * <pre><code>
 * ...
 * container.setLayout(null);
 * ...
 * </code></pre>
 * con la diferencia que existe un objeto para ello, dado que Lemur no permite
 * el valor <code>null</code>.
 * </p>
 * 
 * @author wil
 * @version 1.0-SNAPSHOT
 * 
 * @since 1.0.0
 */
public class NullLayout extends AbstractGuiComponent implements GuiLayout {

    /** Lista de hijos que contiene el nodo padre. */
    private final List<Node> children = new ArrayList<>();

    /**
     * Constructor predeterminado de la clase<code>NullLayout</code>.
     */
    public NullLayout() {
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
        if ( children.contains(n) ) {
            removeChild(n);
        }

        children.add(n);
        if (isAttached()) {
            getNode().attachChild(n);
        }
        return n;
    }

    /**
     * (non-JavaDoc)
     * @param n nodo
     * @see GuiLayout#removeChild(com.jme3.scene.Node) 
     */
    @Override
    public void removeChild(Node n) {
        if ( children.remove(n) ) {
            n.removeFromParent();
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
        for ( final Node n : children ) {
            n.removeFromParent();
        }
        children.clear();
    }

    /**
     * (non-JavaDoc)
     * @param size vector-3f
     * @see GuiLayout#calculatePreferredSize(com.jme3.math.Vector3f) 
     */
    @Override
    public void calculatePreferredSize(Vector3f size) { }

    /**
     * (non-JavaDoc)
     * @param pos vector-3f
     * @param size vector-3f
     * @see GuiLayout#reshape(com.jme3.math.Vector3f, com.jme3.math.Vector3f) 
     */
    @Override
    public void reshape(Vector3f pos, Vector3f size) { }

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
