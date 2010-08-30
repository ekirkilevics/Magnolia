/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.views;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.terminal.ExternalResource;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.module.admincentral.tree.TreeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.navigator.Navigator;

import java.util.Date;

/**
 * WebsiteTreeTableView.
 *
 * @author fgrilli
 */
public class WebsiteTreeTableView extends AbstractTreeTableView {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(WebsiteTreeTableView.class);

    // TODO static proof-of-concept hard coded stuff, needs to be replaced with generic configuration

    public static final String PAGE = "Page";
    public static final String TITLE = "Title";
    public static final String STATUS = "Status";
    public static final String TEMPLATE = "Template";
    public static final String MOD_DATE = "Mod. Date";

    private final Action actionAdd = createAddAction();
    private final Action actionDelete = createDeleteAction();
    private final Action actionOpen = createOpenAction();

    private final Action[] ftlActions;
    private final Action[] jspActions;

    public WebsiteTreeTableView() {
        ftlActions = new Action[]{actionOpen, actionAdd, actionDelete};
        jspActions = new Action[]{actionAdd, actionDelete};
        setTreeDefinition(TreeManager.getInstance().getTree("website"));
        getTreeTable().setContainerDataSource(getContainer());
        addContextMenu();
    }

    public void init(Navigator navigator, Application application) {
    }

    public void navigateTo(String requestedDataId) {
        log.error("was asked to navigate to {}, but no one thought me how :(", requestedDataId);
    }

    public String getWarningForNavigatingFrom() {
        // TODO Auto-generated method stub
        return null;
    }

    private Action createAddAction() {
        Action add = new Action("Add");
        add.setIcon(new ExternalResource(ServerConfiguration.getInstance().getDefaultBaseUrl() + ".resources/icons/16/document_plain_earth_add.gif"));
        return add;
    }

    private Action createDeleteAction() {
        Action add = new Action("Delete");
        add.setIcon(new ExternalResource(ServerConfiguration.getInstance().getDefaultBaseUrl() + ".resources/icons/16/delete2.gif"));
        return add;
    }

    private Action createOpenAction() {
        Action add = new Action("Open page");
        add.setIcon(new ExternalResource(ServerConfiguration.getInstance().getDefaultBaseUrl() + ".resources/icons/16/document_plain_earth.gif"));
        return add;
    }

    void addContextMenu() {
        getTreeTable().addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                Item selection = getTreeTable().getItem(target);
                String template = (String) selection.getItemProperty(TEMPLATE).getValue();
                if (template == null) {
                    return new Action[0];
                }
                // TODO: Just a dummy demo for creating different context menus depending on
                // selected item...
                return template.endsWith("JSP") ? jspActions : ftlActions;
            }

            /*
             * Handle actions
             */
            public void handleAction(Action action, Object sender, Object target) {
                if (action == actionAdd) {
                    Object itemId = getTreeTable().addItem();
                    getTreeTable().setParent(itemId, target);

                    Item item = getTreeTable().getItem(itemId);
                    Property name = item.getItemProperty(PAGE);
                    name.setValue("New Item");
                    Property status = item.getItemProperty(STATUS);
                    status.setValue(0);
                    Property modDate = item.getItemProperty(MOD_DATE);
                    modDate.setValue(new Date());
                } else if (action == actionDelete) {
                    getTreeTable().removeItem(target);
                }
            }
        });
    }
}
