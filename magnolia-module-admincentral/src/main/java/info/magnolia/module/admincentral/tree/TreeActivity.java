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
package info.magnolia.module.admincentral.tree;

import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.place.ItemSelectedPlace;
import info.magnolia.module.vaadin.activity.AbstractActivity;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;
import info.magnolia.module.vaadin.place.PlaceController;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

/**
 * TODO: write javadoc.
 *
 * @author tmattsson
 *
 */
public class TreeActivity extends AbstractActivity implements TreeView.Presenter {

    private final String treeName;
    private EventBus eventBus;
    private PlaceController placeController;
    // FIXME use the interface not the implementation
    private TreeViewImpl treeView;
    private UIModel uiModel;
    private String path;

    public TreeActivity(String treeName, String path, PlaceController placeController, UIModel uiModel) {
        this.uiModel = uiModel;
        this.treeName = treeName;
        this.path = path;
        this.placeController = placeController;
    }

    public void start(HasComponent display, EventBus eventBus) {
        this.eventBus = eventBus;
        try {
            treeView = new TreeViewImpl(treeName, this, uiModel);
            treeView.select(path);
            display.setComponent(treeView);
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public UIModel getUIModel() {
        return uiModel;
    }

    public void onItemSelection(Item jcrItem) {
        try {
            placeController.goTo(new ItemSelectedPlace(treeName, jcrItem.getPath()));
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void executeActionForSelectedItem(String actionName) {
        treeView.executeActionForSelectedItem(actionName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeActivity that = (TreeActivity) o;
        // FIXME this won't work if someone else changes the place -> the tree would not navigate, example: breadcrump
        if (treeName != null ? !treeName.equals(that.treeName) : that.treeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return treeName != null ? treeName.hashCode() : 0;
    }
}
