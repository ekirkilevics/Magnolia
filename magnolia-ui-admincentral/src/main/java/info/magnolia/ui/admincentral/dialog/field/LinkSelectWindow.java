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
package info.magnolia.ui.admincentral.dialog.field;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.wrapper.LazyNodeWrapper;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilder;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

/**
 * A window for selecting a node by browsing a tree view.
 *
 * @version $Id$
 */
public class LinkSelectWindow extends Window implements JcrView.Presenter {

    /**
     * Presenter for LinkSelectWindow.
     *
     * @version $Id$
     */
    public interface Presenter {
        void onCancel();

        void onClose();
    }

    private Node selectedNode;
    private Presenter presenter;
    private JcrView jcrView;

    public LinkSelectWindow(Presenter presenter, Application application, JcrViewBuilderProvider jcrViewBuilderProvider, WorkbenchDefinition workbenchDefinition) {
        this.presenter = presenter;

        JcrViewBuilder builder = jcrViewBuilderProvider.getBuilder();

        jcrView = builder.build(workbenchDefinition, JcrView.ViewType.TREE);
        jcrView.setPresenter(this);

        setCaption("Select");
        setWidth(600, Sizeable.UNITS_PIXELS);
        setModal(true);

        Button ok = new Button("OK");
        ok.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onClose();
            }
        });
        Button cancel = new Button("Cancel");
        cancel.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onCancel();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.addComponent(ok);
        buttons.setComponentAlignment(ok, Alignment.MIDDLE_RIGHT);
        buttons.addComponent(cancel);
        buttons.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);

        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(((IsVaadinComponent) jcrView).asVaadinComponent());
        layout.addComponent(buttons);
        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);

        addComponent(layout);
        application.getMainWindow().addWindow(this);
    }

    private void onClose() {
        presenter.onClose();
    }

    private void onCancel() {
        presenter.onCancel();
    }

    public void select(String path) {
        try {
            jcrView.select(path);
        } catch (RuntimeRepositoryException ignored) {
            // This happens when the path doesn't exist
        }
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    @Override
    public void onItemSelection(Item item) {
        if (item.isNode()) {
            try {
                this.selectedNode = new LazyNodeWrapper((Node) item);
            } catch (RepositoryException e) {
                throw new RuntimeRepositoryException(e);
            }
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
