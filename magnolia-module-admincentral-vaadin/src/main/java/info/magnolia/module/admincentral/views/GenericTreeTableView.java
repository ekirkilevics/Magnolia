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

import java.util.Date;

import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.navigation.AdminCentralAction;
import info.magnolia.module.admincentral.tree.TreeManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Window;

/**
 * A generic tree table view which can show data from any repository.
 * TODO remove bunch of hardcoded stuff for context menu.
 * @author fgrilli
 *
 */
public class GenericTreeTableView extends AbstractTreeTableView {

    private static final Logger log = LoggerFactory.getLogger(GenericTreeTableView.class);
    private static final long serialVersionUID = 1L;

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

    public GenericTreeTableView(String repo) {
        treeDefinition = TreeManager.getInstance().getTree(repo);
        treeTable.setContainerDataSource(getContainer());
        ftlActions = new Action[]{actionAdd, actionDelete};
        jspActions = new Action[]{actionOpen, actionAdd, actionDelete};
        addContextMenu();
    }

    private Action createAddAction() {
        Action add = new AdminCentralAction("Add") {

            @Override
            public void handleAction(Object sender, Object target) {
                Object itemId = treeTable.addItem();
                treeTable.setParent(itemId, target);

                Item item = treeTable.getItem(itemId);
                Property name = item.getItemProperty(PAGE);
                name.setValue("untitled");
                Property status = item.getItemProperty(STATUS);
                status.setValue(0);
                Property modDate = item.getItemProperty(MOD_DATE);
                modDate.setValue(new Date());
                }
        };
        add.setIcon(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/document_plain_earth_add.gif"));
        return add;
    }

    private Action createDeleteAction() {
        Action add = new AdminCentralAction("Delete") {

            @Override
            public void handleAction(Object sender, Object target) {
                treeTable.removeItem(target);
            }};
        add.setIcon(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/delete2.gif"));
        return add;
    }

    private Action createOpenAction() {
        Action add = new AdminCentralAction("Open page") {

            @Override
            public void handleAction(Object sender, Object target) {
                if (target == null || !(target instanceof String)) {
                    return;
                }
                String id = StringUtils.substringBefore((String) target, "@");
                if (StringUtils.isBlank(id)) {
                    return;
                }
                try {
                    String handle = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContentByUUID(id).getHandle();
                // we no longer store handle as part of tree container ...
//                TreeTable tree = treeTable;
//                Item item = tree.getItem(target);//item.getItemPropertyIds()
//                Property handleProp = item.getItemProperty("handle");
//                String handle = (String) handleProp.getValue();
                    String uri = MgnlContext.getContextPath() + handle + ".html";
                    Window window = getApplication().getMainWindow();
                    window.open(new ExternalResource(uri));
                } catch (RepositoryException e) {
                    log.error("Failed to retrieve page handle for " + target, e);
                }
            }};
        add.setIcon(new ExternalResource(MgnlContext.getContextPath() + "/.resources/icons/16/document_plain_earth.gif"));
        return add;
    }

    void addContextMenu() {
        treeTable.addActionHandler(new Action.Handler() {

            public Action[] getActions(Object target, Object sender) {
                Item selection = treeTable.getItem(target);
                String template = (String) selection.getItemProperty(TEMPLATE).getValue();
                if (template == null) {
                    return new Action[0];
                }
                // TODO: Just a dummy demo for creating different context menus depending on
                // selected item...
                return template.indexOf("JSP") != -1 ? jspActions : ftlActions;
            }

            public void handleAction(Action action, Object sender, Object target) {
                try {
                    ((AdminCentralAction) action).handleAction(sender, target);
                } catch (ClassCastException e) {
                    // not our action
                    log.error("Encountered untreatable action {}:{}", action.getCaption(), e.getMessage());
                }
            }

        });
    }
}
