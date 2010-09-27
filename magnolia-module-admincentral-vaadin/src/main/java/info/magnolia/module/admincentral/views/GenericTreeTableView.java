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

import info.magnolia.context.LifeTimeJCRSessionUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admincentral.tree.MenuItem;
import info.magnolia.module.admincentral.tree.TreeRegistry;
import info.magnolia.module.admincentral.tree.action.TreeAction;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.Action;
import com.vaadin.terminal.ExternalResource;


/**
 * A generic tree table view which can show data from any repository.
 * 
 * @author fgrilli
 */
public class GenericTreeTableView extends AbstractTreeTableView {

    private static final Logger log = LoggerFactory.getLogger(GenericTreeTableView.class);

    private static final long serialVersionUID = 1704972467182396882L;

    public GenericTreeTableView(String treeName) {
        try {
            setTreeDefinition(TreeRegistry.getInstance().getTree(treeName));
        }
        catch (RepositoryException e) {
            // TODO: we need to somehow properly handle this
            log.error(e.getMessage(), e);
        }
        getTreeTable().setContainerDataSource(createContainer(getTreeTable()));
        addContextMenu();
    }

    void addContextMenu() {

        getTreeTable().addActionHandler(new Action.Handler() {

            private static final long serialVersionUID = 4311121075528949148L;

            public Action[] getActions(Object target, Object sender) {

                ArrayList<Action> actions = new ArrayList<Action>();
                try {
                    String itemId = (String) target;
                    Session session = LifeTimeJCRSessionUtil.getHierarchyManager(getTreeDefinition().getRepository()).getWorkspace().getSession();
                    Node node = session.getNode(itemId);

                    for (MenuItem mi : getTreeDefinition().getContextMenuItems()) {
                        TreeAction action = mi.getAction();

                        if (!action.isAvailable(node, null))
                            continue;

                        action.setCaption(mi.getLabel());
                        action.setIcon(new ExternalResource(MgnlContext.getContextPath() + mi.getIcon()));
                        actions.add(action);

                    }
                }
                catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                }

                return actions.toArray(new Action[actions.size()]);
            }

            public void handleAction(Action action, Object sender, Object target) {
                try {
                    ((TreeAction) action).handleAction(GenericTreeTableView.this, getTreeDefinition(), sender, target);
                }
                catch (ClassCastException e) {
                    // not our action
                    log.error("Encountered untreatable action {}:{}", action.getCaption(), e.getMessage());
                }
                catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }
}
