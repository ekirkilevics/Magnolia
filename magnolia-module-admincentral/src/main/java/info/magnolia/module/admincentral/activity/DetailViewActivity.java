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
package info.magnolia.module.admincentral.activity;

import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.event.ContentChangedEvent;
import info.magnolia.module.admincentral.model.UIModel;
import info.magnolia.module.admincentral.views.DetailView;
import info.magnolia.ui.activity.AbstractActivity;
import info.magnolia.ui.component.HasComponent;
import info.magnolia.ui.event.EventBus;


/**
 * Shows the detail view and command list.
 */
public class DetailViewActivity extends AbstractActivity implements DetailView.Presenter {

    private UIModel uiModel;
    private String treeName;
    private String path;
    private DetailView detailView;
    private EventBus eventBus;

    public DetailViewActivity(String treeName, String path, UIModel uiModel) {
        this.treeName = treeName;
        this.uiModel = uiModel;
        detailView = new DetailView(this);
        showItem(path);
    }

    public void start(HasComponent display, EventBus eventBus) {
        this.eventBus = eventBus;
        display.setComponent(detailView);
    }

    private void showItem(String path) {
        try {
            // Displaying commands for the root node makes no sense
            if (!"/".equals(path)) {
                this.path = path;
                detailView.showCommands(uiModel.getCommandsForItem(treeName, path));
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCommandSelected(String commandName) {
        try {
            uiModel.executeCommand(commandName, treeName, path);
            // FIXME this has to be more granular
            eventBus.fireEvent(new ContentChangedEvent(treeName, path));
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result
                + ((treeName == null) ? 0 : treeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetailViewActivity other = (DetailViewActivity) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (treeName == null) {
            if (other.treeName != null)
                return false;
        } else if (!treeName.equals(other.treeName))
            return false;
        return true;
    }


}
