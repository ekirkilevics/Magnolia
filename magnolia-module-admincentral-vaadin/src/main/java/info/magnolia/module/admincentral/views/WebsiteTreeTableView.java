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
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import info.magnolia.module.admincentral.tree.TreeManager;
import org.apache.commons.lang.ArrayUtils;
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

    public WebsiteTreeTableView() {
        new Exception().printStackTrace();
        setTreeDefinition(TreeManager.getInstance().getTree("website"));
        getTreeTable().setContainerDataSource(getContainer());
        addContextMenu();
        addEditingByDoubleClick();
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

    // TODO static proof-of-concept hard coded stuff, needs to be replaced with generic configuration

    public static final String PAGE = "Page";
    public static final String TITLE = "Title";
    public static final String STATUS = "Status";
    public static final String TEMPLATE = "Template";
    public static final String MOD_DATE = "Mod. Date";

    private static final Action ACTION_ADD = createAddAction();
    private static final Action ACTION_DELETE = createDeleteAction();
    private static final Action ACTION_OTHER = createHelpAction();

    private static final Action[] FTL_ACTIONS = new Action[]{ACTION_ADD, ACTION_OTHER};
    private static final Action[] JSP_ACTIONS = new Action[]{ACTION_ADD, ACTION_DELETE};

    private static Action createAddAction() {
        Action add = new Action("Add");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Add.ico"));
        return add;
    }

    private static Action createDeleteAction() {
        Action add = new Action("Delete");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Delete.ico"));
        return add;
    }

    private static Action createHelpAction() {
        Action add = new Action("Other");
        add.setIcon(new ExternalResource("http://www.iconarchive.com/download/deleket/button/Button-Help.ico"));
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
                return template.endsWith("JSP") ? JSP_ACTIONS : FTL_ACTIONS;
            }

            /*
             * Handle actions
             */
            public void handleAction(Action action, Object sender, Object target) {
                if (action == ACTION_ADD) {
                    Object itemId = getTreeTable().addItem();
                    getTreeTable().setParent(itemId, target);

                    Item item = getTreeTable().getItem(itemId);
                    Property name = item.getItemProperty(PAGE);
                    name.setValue("New Item");
                    Property status = item.getItemProperty(STATUS);
                    status.setValue(0);
                    Property modDate = item.getItemProperty(MOD_DATE);
                    modDate.setValue(new Date());
                } else if (action == ACTION_DELETE) {
                    getTreeTable().removeItem(target);
                }
            }
        });
    }

    public static final String[] EDITABLE_FIELDS = {PAGE, TITLE, TEMPLATE};

    private Object selectedItemId = null;
    private Object selectedPropertyId = null;

    void addEditingByDoubleClick() {

        getTreeTable().setTableFieldFactory(new DefaultFieldFactory() {

            public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
                if (selectedItemId != null) {
                    if ((selectedItemId.equals(itemId)) && (selectedPropertyId.equals(propertyId))) {
                        if (ArrayUtils.contains(EDITABLE_FIELDS, propertyId)) {
                            return super.createField(container, itemId, propertyId, uiContext);
                        }
                    }
                }
                return null;
            }
        });

        getTreeTable().addListener(new ItemClickEvent.ItemClickListener() {

            public void itemClick(ItemClickEvent event) {
                if (event.isDoubleClick()) {

                    // TODO we need to unset these somehow...

                    selectedItemId = event.getItemId();
                    selectedPropertyId = event.getPropertyId();
                    getTreeTable().setEditable(true);
                } else if (getTreeTable().isEditable()) {
                    getTreeTable().setEditable(false);
                    getTreeTable().setValue(event.getItemId());
                }
            }
        });
    }
}
