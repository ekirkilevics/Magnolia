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
package info.magnolia.ui.admincentral.jcr.view.activity;

import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.workbench.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
/**
 * A base class for jcr-related displaying activities.
 * @author fgrilli
 * @see TreeActivity
 * @see ListActivity
 */
public abstract class AbstractJcrActivity extends AbstractActivity implements JcrView.Presenter, ContentChangedEvent.Handler {

    protected PlaceController placeController;
    protected String path = "/";
    protected Shell shell;
    protected JcrView jcrView;

    public AbstractJcrActivity(WorkbenchDefinition workbenchDefinition, JcrViewBuilderProvider jcrViewBuilderProvider, PlaceController placeController, Shell shell) {
        this.shell = shell;
        this.placeController = placeController;
    }

    public void update(ItemSelectedPlace place) {
        final String path = place.getPath();
        if(!this.path.equals(path)){
            this.path = path;
            jcrView.select(path);
        }
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus) {
        jcrView.setPresenter(this);
        jcrView.select(path);
        eventBus.addHandler(ContentChangedEvent.class, this);
        viewPort.setView(jcrView);
    }

    @Override
    public void onItemSelection(Item jcrItem) {
        this.path = jcrView.getPathInTree(jcrItem);
        try {
            placeController.goTo(new ItemSelectedPlace(jcrItem.getSession().getWorkspace().getName(), this.path, getViewType()));
        }
        catch (RepositoryException e) {
            shell.showError("Can't access item.", e);
        }
    }

    private ViewType getViewType() {
        return jcrView instanceof TreeView ? ViewType.TREE: ViewType.LIST;
    }

    @Override
    public void onContentChanged(ContentChangedEvent event) {
        // FIXME only if we are not the source!
        jcrView.refresh();
    }

    public JcrView getJcrView() {
        return jcrView;
    }
}