/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.column;

import info.magnolia.context.MgnlContext;
import info.magnolia.exception.RuntimeRepositoryException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * UI component that displays a label and on double click opens it for editing by switching the
 * label to save text field. Implements {@link Comparable} to allow sorting of columns holding this
 * component with Vaadin. Default implementation for <code>compareTo(..),</code> method uses jcr's
 * item name for comparison. Subclasses may use more specific properties.
 *
 * @author tmattsson
 * @author mrichert
 */
public abstract class Editable extends Label {

    private static final Logger log = LoggerFactory.getLogger(Editable.class);

    /**
     * Presenter for AbstractEditable.
     */
    public interface Presenter {

        void edit(Item item, info.magnolia.ui.framework.editor.Editor editor) throws RepositoryException;

        boolean save(Item item) throws RepositoryException;

        void onClick(Item item) throws RepositoryException;
    }

    /**
     * Result object used by subclasses to return the component to switch to on double click and an
     * editor which is to be used by the editor driver.
     */
    public static class ComponentAndEditor {

        private Component component;

        private info.magnolia.ui.framework.editor.Editor editor;

        public ComponentAndEditor(Component component, info.magnolia.ui.framework.editor.Editor editor) {
            this.component = component;
            this.editor = editor;
        }

        public Component getComponent() {
            return component;
        }

        public info.magnolia.ui.framework.editor.Editor getEditor() {
            return editor;
        }
    }

    private final String workspace;

    private final String nodeIdentifier;

    private final String propertyName;

    private Presenter presenter;

    private ComponentAndEditor componentAndEditor;

    public Presenter getPresenter() {
        return presenter;
    }

    protected Editable(Item item, Presenter presenter) throws RepositoryException {
        setCaption(getLabelText(item));
        this.presenter = presenter;
        workspace = item.getSession().getWorkspace().getName();
        nodeIdentifier = item.isNode() ? ((Node) item).getIdentifier() : item.getParent().getIdentifier();
        propertyName = item.isNode() ? null : item.getName();

        // layout = new CssLayout();

        // TODO the double click event should be removed when the text field is visible, otherwise
        // its not possible to double click to mark words
        // layout.addListener(new LayoutEvents.LayoutClickListener() {
        //
        // @Override
        // public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
        // Component parent = AbstractEditable.this.getParent();
        // while (!(parent instanceof JcrBrowser)) {
        // parent = parent.getParent();
        // if (parent == null) {
        // return;
        // }
        // }
        // JcrBrowser browser = (JcrBrowser) parent;
        // String path;
        // try {
        // path = getItem().getPath();
        // }
        // catch (RepositoryException e) {
        // throw new RuntimeRepositoryException(e);
        // }
        // JcrContainer container = browser.getContainer();
        // ContainerItemId itemId = container.getItemByPath(path);
        //
        // if (browser.isSelected(itemId)) {
        // try {
        // Item item = getItem();
        // ComponentAndEditor componentAndEditor = getComponentAndEditor(item);
        // AbstractEditable.this.presenter.edit(item, componentAndEditor.getEditor());
        // layout.removeAllComponents();
        // layout.addComponent(componentAndEditor.getComponent());
        // }
        // catch (RepositoryException e) {
        // throw new RuntimeRepositoryException(e);
        // }
        // }
        // }
        // });
        // layout.addComponent(new Label(getLabelText(item)));
        // layout.setSizeFull();
        // setCompositionRoot(layout);
        setSizeFull();

        // FIXME this is a hack to show the label which is a div inline with the preceding icons. It
        // probably breaks on IE as it uses display: inline-block.
        addStyleName("m-inline-div");
    }

    public void addListener(EditListener listener) {

    }

    protected void cancel() {
        componentAndEditor = null;
    }

    protected void save() {
        try {
            if (presenter.save(getItem())) {
                componentAndEditor = null;
            }
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof Editable) {
            Editable o = (Editable) other;
            try {

                log.debug("comparing {} and {}", getItem().getName().toLowerCase(), o.getItem().getName().toLowerCase());

                return getItem().getName().toLowerCase().compareTo(o.getItem().getName().toLowerCase());
            }
            catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
        else {
            return super.compareTo(other);
        }
    }

    protected abstract String getLabelText(Item item) throws RepositoryException;

    protected abstract ComponentAndEditor getComponentAndEditor(Item item) throws RepositoryException;

    protected Item getItem() throws RepositoryException {
        Node node = MgnlContext.getJCRSession(workspace).getNodeByIdentifier(nodeIdentifier);
        return propertyName == null ? node : node.getProperty(propertyName);
    }

    public Component getEditorComponent() {
        try {
            Item item = getItem();
            componentAndEditor = getComponentAndEditor(item);
            presenter.edit(item, componentAndEditor.getEditor());
            return componentAndEditor.getComponent();
        }
        catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
