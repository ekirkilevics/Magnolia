/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
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
import info.magnolia.module.vaadin.activity.AbstractActivity;
import info.magnolia.module.vaadin.component.HasComponent;
import info.magnolia.module.vaadin.event.EventBus;


/**
 * Shows the detail view and command list.
 */
public class DetailViewActivity extends AbstractActivity implements DetailView.Presenter {

    private UIModel uiModel;
    private String workspace;
    private String path;
    private DetailView detailView;
    private EventBus eventBus;

    public DetailViewActivity(String workspace, String path, UIModel uiModel) {
        this.workspace = workspace;
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
            if (!path.equals("/")) {
                this.path = path;
                detailView.showCommands(uiModel.getCommandsForItem(workspace, path));
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCommandSelected(String commandName) {
        try {
            uiModel.executeCommand(commandName, workspace, path);
            // FIXME this has to be more granular
            eventBus.fireEvent(new ContentChangedEvent(workspace, path));
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
                + ((workspace == null) ? 0 : workspace.hashCode());
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
        if (workspace == null) {
            if (other.workspace != null)
                return false;
        } else if (!workspace.equals(other.workspace))
            return false;
        return true;
    }


}
