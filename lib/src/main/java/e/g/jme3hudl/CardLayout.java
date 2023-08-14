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
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Un objeto de la clase <code>CardLayout</code> es un administrador de diseño
 * para un envase.
 * <p>
 * Trata cada componente del contenedor como una tarjeta.<br>
 * 
 * Solo una tarjeta es visible a la vez, y el contenedor actúa como
 * una pila de cartas. El primer componente añadido a un objeto 
 * <code>CardLayout</code> es el componente visible cuando el contenedor se
 * muestra por primera vez.
 * </p>
 * <p>
 * El orden de las tarjetas está determinado por el propio contenedor interno
 * </p>
 * <p>
 * Clase <code>CardLayout</code> define un conjunto de métodos que permiten a
 * una aplicación moverse a través de estas tarjetas de forma secuencial, o para
 * mostrar una tarjeta específica.
 * </p>
 * <p>
 * {@link CardLayout#addLayoutComponent} es el método que se puede usar para 
 * asociar un identificador de cadena con una tarjeta determinada para acceso 
 * aleatorio rápido.
 * </p>
 * 
 * @author wil
 * @version 1.0-SNAPSHOT
 * 
 * @since 1.0.0
 */
public class CardLayout extends AbstractGuiComponent implements GuiLayout {

    /** Logger de la clase {@link CardLayout}. */
    private static final Logger LOG = Logger.getLogger(CardLayout.class.getName());
    
    /**
     * Esto genera una lista para almacenar los parse de componentes
     * y sus respectivos nombres asociados.
     */
    private final List<Card> cs = new ArrayList<>();
    
    /**
     * Un par de Componente y Cadena que representa su nombre.
     */
    class Card {
        public String name;
        public Node comp;
        public Card(String cardName, Node cardComponent) {
            name = cardName;
            comp = cardComponent;
        }
    }
    
    /**
     * Índice del componente actualmente mostrado por {@link CardLayout}.
     */
    int currentCard = 0;

    /**
     * Bandera encargado de indicar si un componente hijo es eliminado por este diseño ({@link CardLayout}), para
     * evitar una sobrecarga de avisos por consola.
     */
    boolean flag = false;

    /**
     * Una brecha del diseño de tarjetas (recuadro). 
     * <P>
     * Especific el espacio entre los bordes superior e inferior de un
     * contenedor y el componente actual.
     * </p>
     * <p>
     * <b>Nota:</b> Debe ser un valor no negativo.
     * </p>
     */
    Vector3f gap;
    
    /**
     * Crea un nuevo diseño de tarjeta con espacios de tamaño cero.
     */
    public CardLayout() {
        this(new Vector3f(0.0F, 0.0F, 0.0F));
    }
            
    /**
     * Crea un nuevo diseño de tarjeta con las brechas de espacio
     * espesificados.
     * 
     * @param gap Brechas entre componentes.
     */
    public CardLayout(Vector3f gap) {
        this.gap = Objects.requireNonNull(gap, "Invalid gap.");
    }
        
    /**
     * (non-JavaDoc)
     * @param size vector-3f
     * @see GuiLayout#calculatePreferredSize(com.jme3.math.Vector3f) 
     */
    @Override
    public void calculatePreferredSize(Vector3f size) {       
        Insets3f insets = ((Panel) getNode()).getInsets();
        if (insets == null) {
            insets = new Insets3f(new Vector3f(), new Vector3f());
        }
        
        float w = 0,
              h = 0, 
              z = 0;

        for (Card c : cs) {
            Panel comp = (Panel) c.comp;
            Vector3f d = comp.getPreferredSize();
            if (d.x > w) {
                w = d.x;
            }
            if (d.y > h) {
                h = d.z;
            }
            if (d.z > z) {
                z = d.z;
            }
        }
        size.set(insets.min.x + insets.max.x + w + gap.x*2,
                 insets.min.y + insets.max.y + h + gap.y*2, 
                 insets.min.z + insets.max.z + z + gap.z*2);
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
        
        int ncomponents = cs.size();        
        boolean currentFound = false;
        
        Panel comp;
        for (Card c : cs) {
            comp = (Panel) c.comp;
            
            comp.setLocalTranslation(gap.x + insets.min.x, -(gap.y + insets.min.y), gap.z + insets.min.z);
            comp.setSize(new Vector3f(size.x - (gap.x * 2 + insets.min.x + insets.max.x),
                                      size.y - (gap.y * 2 + insets.min.y + insets.max.y),
                                      size.z - (gap.z * 2 + insets.min.z + insets.max.z)));
            if (comp.getParent() != null) {
                currentFound = true;
            }
        }
        
        if (!currentFound && ncomponents > 0) {
            comp = (Panel) (cs.get(0)).comp;
            getNode().attachChild(comp);
        }
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
        if (constraints.length == 1 && (constraints[0] instanceof String)) {
            addLayoutComponent(n, (String) constraints[0]);
            return n;
        } else {
            throw new IllegalArgumentException("cannot add to layout: constraint must be a string");
        }
    }
    
    /**
     * Método encargado de agregar un nuevo componente hijo a este diseño.
     * @param <T> tipo de componente(nodo)
     * @param comp nuevo componente.
     * @param name nombre clave(unico).
     */
    public <T extends Node> void addLayoutComponent(T comp, String name) {
        if (cs.isEmpty() && isAttached()) {
            getNode().attachChild(comp);
        }
        for (Card c : cs) {
            if (c.name.equals(name)) {
                if (c.comp.getParent() != null) {
                    c.comp.removeFromParent();
                }
                c.comp = comp;
                return;
            }
        }
        cs.add(new Card(name, comp));
        invalidate();
    }
    
    /**
     * (non-JavaDoc)
     * @param comp nodo
     * @see GuiLayout#removeChild(com.jme3.scene.Node) 
     */
    @Override
    public void removeChild(Node comp) {
        for (int i = 0; i < cs.size(); i++) {
            if ((cs.get(i)).comp == comp) {
                // si eliminamos el componente actual, deberíamos mostrar el siguiente
                if ((comp.getParent() != null) && comp.removeFromParent()) {
                    next();
                }
                
                cs.remove(i);
                
                // tarjeta actual correcta si es necesario
                if (currentCard > i) {
                    currentCard--;
                }
                break;
            }
        }
        invalidate();
    }
    
    /**
     * Pasa a la siguiente carta del contenedor especificado. Si la tarjeta 
     * actualmente visible es la última, este método cambia a la primera carta
     * en el diseño.
     * 
     * @see CardLayout#previous() 
     */
    public void next() {
        for (int i = 0; i < cs.size(); i++) {
            Node comp = (cs.get(i)).comp;
            if (comp.getParent() != null) {
                flag = true;
                comp.removeFromParent();
                currentCard = (i + 1) % cs.size();
                comp = (cs.get(currentCard)).comp;
                getNode().attachChild(comp);
                invalidate();
                flag = false;
                return;
            }
        }
        showDefaultComponent();
    }
    
    /**
     * Cambia a la carta anterior del contenedor especificado. Si la tarjeta 
     * actualmente visible es la primera, este método cambia a la última carta
     * en el diseño.
     * 
     * @see CardLayout#next() 
     */
    public void previous() {
        for (int i = 0; i < cs.size(); i++) {
            Node comp = (cs.get(i)).comp;
            if (comp.getParent() != null) {
                flag = true;
                comp.removeFromParent();
                currentCard = ((i > 0) ? i-1 : cs.size()-1);
                comp = (cs.get(currentCard)).comp;
                getNode().attachChild(comp);
                invalidate();
                flag = false;
                return;
            }
        }
        showDefaultComponent();
    }
    
    /**
     * Método encargado de mostrar la targeta predeterminada, siempre y cuando
     * exista una targeta.
     */
    void showDefaultComponent() {
        if (!cs.isEmpty()) {
            currentCard = 0;
            getNode().attachChild((cs.get(0)).comp);
            invalidate();
        }
    }

    /**
     * Da la vuelta a la primera carta del contenedor.
     * @see CardLayout#last() 
     */
    public void first() {
        int ncomponents = cs.size();
        for (Card c : cs) {
            Node comp = c.comp;
            if (comp.getParent() != null) {
                flag = true;
                comp.removeFromParent();
                break;
            }
        }
        flag = false;
        if (ncomponents > 0) {
            currentCard = 0;
            getNode().attachChild(cs.get(currentCard).comp);
            invalidate();
        }
    }

    /**
     * Da la vuelta a la última carta del contenedor.
     * @see CardLayout#first() 
     */
    public void last() {
        int ncomponents = cs.size();
        for (Card c : cs) {
            Node comp = c.comp;
            if (comp.getParent() != null) {
                flag = true;
                comp.removeFromParent();
                break;
            }
        }
        flag = false;
        if (ncomponents > 0) {
            currentCard = ncomponents - 1;
            getNode().attachChild(cs.get(currentCard).comp);
            invalidate();
        }
    }

    /**
     * Cambia al componente que se agregó a este diseño utilizando su nombre
     * clave especificado.
     * <p>
     * Si no existe tal componente, entonces no pasa nada.
     * </p>
     * 
     * @param name nombre clave del componente.
     * @see CardLayout#addLayoutComponent(com.jme3.scene.Node, java.lang.String) 
     */
    public void show(String name) {
        Node next = null;
        int ncomponents = cs.size();
        for (int i = 0; i < ncomponents; i++) {
            Card card = cs.get(i);
            if (card.name.equals(name)) {
                next = card.comp;
                currentCard = i;
                break;
            }
        }
        if ((next != null) && (next.getParent() == null)) {
            for (int i = 0; i < ncomponents; i++) {
                Node comp = cs.get(i).comp;
                if (comp.getParent() != null) {
                    flag = true;
                    comp.removeFromParent();
                    break;
                }
            }
            flag = false;
            getNode().attachChild(next);
            invalidate();
        }
    }

    /**
     * Devuelve una representación de cadena del estado de este diseño de tarjeta.
     * @return a string representation of this card layout.
     */
    @Override
    public String toString() {
        return getClass().getName() + "[gap=" + gap + "]";
    }

    /**
     * Devuelve una colección de componentes que gestiona este diseño.
     * @return coleccón de componente(nodos hijos).
     */
    public Collection<Node> getLayoutChildren() {
        List<Node> children = new ArrayList<>();
        for (final Card card : this.cs) {
            children.add(card.comp);
        }
        return children;
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#detach(com.simsilica.lemur.core.GuiControl) 
     * @param parent gui-control
     */
    @Override
    public void detach(GuiControl parent) {
        super.detach(parent);
        Collection<Node> copy = new ArrayList<>(getLayoutChildren());    
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
        if (currentCard >= 0 && currentCard < cs.size()) {
            Card card = cs.get(currentCard);
            show(card.name);
        }
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#getChildren() 
     * @see CardLayout#getLayoutChildren() 
     * 
     * @return list
     * @deprecated no se utiliza para evitar complicaciones, use el metodo
     * <code>getLayoutChildren()</code> en su lugar.
     */
    @Override
    @Deprecated
    public Collection<Node> getChildren() {
        if ( !flag ){
            LOG.info("To get the children, use the getLayoutChildren() method.");
        }
        return new ArrayList<>();
    }

    /**
     * (non-JavaDoc)
     * @see GuiLayout#clearChildren() 
     */
    @Override
    public void clearChildren() {
        for (int i = 0; i < cs.size(); i++) {
            (cs.get(i)).comp.removeFromParent();
            cs.remove(i);
            i--;
        }
        currentCard = 0;
        invalidate();
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
