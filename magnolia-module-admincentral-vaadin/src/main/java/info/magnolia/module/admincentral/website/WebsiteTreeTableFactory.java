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
package info.magnolia.module.admincentral.website;

import com.vaadin.addon.treetable.HieararchicalContainerOrderedWrapper;
import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Table.TableDragMode;


/**
 * Creates the TreeTable for the Websites
 *
 * @author dlipp
 */
public class WebsiteTreeTableFactory {

    private static WebsiteTreeTableFactory instance;

    /**
     * Prevent creation of multiple instances
     */
    private WebsiteTreeTableFactory() {
        super();
    }

    public static WebsiteTreeTableFactory getInstance() {
        if (instance == null) {
            instance = new WebsiteTreeTableFactory();
        }
        return instance;
    }

    /**
     * @return TreeTable hosting the Websites
     */
    public TreeTable createWebsiteTreeTable() {
        final TreeTable table = new TreeTable();
        table.setSizeFull();
        table.setDragMode(TableDragMode.ROW);
        table.setDropHandler(new DropHandler() {

            private void forceRefreshOfTreeTable() {
                // TODO replace this hack - get Table to be refreshed the proper
                // way (Hack from Vaadin Demo-Sources... - TreeTableWorkLog)
                Object tempId = table.getContainerDataSource().addItem();
                table.removeItem(tempId);
            }

            /*
             * @see com.vaadin.event.dd.DropHandler#getAcceptCriterion()
             */
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            /*
             * @seecom.vaadin.event.dd.DropHandler#drop(com.vaadin.event.dd.
             * DragAndDropEvent)
             */
            public void drop(DragAndDropEvent event) {
                // Wrapper for the object that is dragged
                Transferable t = event.getTransferable();

                // Make sure the drag source is the same tree
                if (t.getSourceComponent() != table)
                    return;

                AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                // Get ids of the dragged item and the target item
                Object sourceItemId = t.getData("itemId");
                Object targetItemId = target.getItemIdOver();
                // On which side of the target the item was dropped
                VerticalDropLocation location = target.getDropLocation();

                // TODO: remove next line as soon as it's properly working
                System.out.println("DropLocation: " + location.name());

                HieararchicalContainerOrderedWrapper container = (HieararchicalContainerOrderedWrapper) table
                        .getContainerDataSource();
                // Drop right on an item -> make it a child -
                if (location == VerticalDropLocation.MIDDLE) {
                    table.setParent(sourceItemId, targetItemId);
                    forceRefreshOfTreeTable();
                }
                // Drop at the top of a subtree -> make it previous
                else if (location == VerticalDropLocation.TOP) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        // TODO: remove next line as soon as it's properly
                        // working
                        System.out.println("Parent:" + container.getItem(parentId));
                        container.setParent(sourceItemId, parentId);
                        container.addItemAfter(parentId, sourceItemId);
                        forceRefreshOfTreeTable();
                    }
                }

                // Drop below another item -> make it next
                else if (location == VerticalDropLocation.BOTTOM) {
                    Object parentId = container.getParent(targetItemId);
                    if (parentId != null) {
                        container.setParent(sourceItemId, parentId);
                        // container.moveAfterSibling(sourceItemId,
                        // targetItemId);
                        container.removeItem(targetItemId);
                        container.addItemAfter(sourceItemId, targetItemId);
                        forceRefreshOfTreeTable();
                    }
                }
            }
        });
        return table;
    };
}
