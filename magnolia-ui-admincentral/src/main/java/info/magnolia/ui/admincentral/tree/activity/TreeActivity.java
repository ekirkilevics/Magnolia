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
package info.magnolia.ui.admincentral.tree.activity;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.workbench.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.workbench.event.ContentChangedEvent.Handler;
import info.magnolia.ui.admincentral.workbench.place.ItemSelectedPlace;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Activity for displaying dialogs.
 *
 * @author tmattsson
 */
public class TreeActivity extends AbstractActivity implements TreeView.Presenter, Handler {

    private PlaceController placeController;
    private TreeView treeView;
    private String path = "/";
    private Shell shell;

    public TreeActivity(TreeView treeView, PlaceController placeController, Shell shell, ComponentProvider componentProvider) {
        this.placeController = placeController;
        this.treeView = treeView;
        this.shell = shell;
    }

    public void update(ItemSelectedPlace place){
        final String path = place.getPath();
        if(!this.path.equals(path)){
            this.path = path;
            treeView.select(path);
        }
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        treeView.setPresenter(this);
        treeView.select(path);
        eventBus.addHandler(ContentChangedEvent.class, this);
        viewPort.setView(treeView);
    }

    public void onItemSelection(Item jcrItem) {
        this.path = treeView.getPathInTree(jcrItem);
        try {
            placeController.goTo(new ItemSelectedPlace(jcrItem.getSession().getWorkspace().getName(), this.path));
        }
        catch (RepositoryException e) {
            shell.showError("Can't access item.", e);
        }
    }

    public void onContentChanged(ContentChangedEvent event) {
        // FIXME only if we are not the source!
        treeView.refresh();
    }
}
