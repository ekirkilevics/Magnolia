package info.magnolia.module.admininterface.trees;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.Subscriber;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


public class AdminTreeGroups extends AdminTreeMVCHandler {

    public AdminTreeGroups(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);

    }

    protected void prepareTree(Tree tree, HttpServletRequest request) {
        tree.setDrawShifter(false);

        tree.setIconPage(Tree.ICONDOCROOT + "hat_white.gif"); //$NON-NLS-1$
        if (Server.isAdmin()) {
            tree.setIconOndblclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
                + tree.getJavascriptTree() + ",'.magnolia/dialogs/roleedit.html');"); //$NON-NLS-1$
        }
        tree.addItemType(ItemType.CONTENT);

        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        if (Server.isAdmin()) {
            column0.setHtmlEdit();
        }
        column0.setWidth(2);
        column0.setTitle("group name"); //$NON-NLS-1$
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title"); //$NON-NLS-1$
        if (Server.isAdmin()) {
            column1.setHtmlEdit();
        }
        column1.setWidth(2);
        column1.setTitle("Full Name"); //$NON-NLS-1$
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass(StringUtils.EMPTY);
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.CREATION_DATE);
        // column2.setName(MetaData.SEQUENCE_POS);
        column2.setIsMeta(true);
        column2.setDateFormat("yyyy-MM-dd, HH:mm"); //$NON-NLS-1$
        column2.setTitle("date"); //$NON-NLS-1$
        column2.setWidth(2);

        tree.addColumn(column0);
        if (!this.isBrowseMode()) {
            tree.addColumn(column1);
            if (Server.isAdmin() || Subscriber.isSubscribersEnabled()) {
                tree.addColumn(columnIcons);
            }
            tree.addColumn(column2);
        }

    }

    protected void prepareContextMenu(Tree tree, HttpServletRequest request) {
        // Messages msgs = MessagesManager.getMessages(request);

        ContextMenuItem menuNewPage = new ContextMenuItem("new");
        menuNewPage.setLabel("new"); //$NON-NLS-1$
        menuNewPage.setIcon(request.getContextPath() + "/.resources/icons/16/hat_white_add.gif"); //$NON-NLS-1$
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createRootNode('" //$NON-NLS-1$
            + ItemType.CONTENT.getSystemName() + "');"); //$NON-NLS-1$

        ContextMenuItem menuOpen = new ContextMenuItem("edit");
        menuOpen.setLabel("edit"); //$NON-NLS-1$
        menuOpen.setIcon(request.getContextPath() + "/.resources/icons/16/hat_white.gif"); //$NON-NLS-1$
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ",'.magnolia/dialogs/groupedit.html');"); //$NON-NLS-1$
        menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuAssignRoles = new ContextMenuItem("assign");
        menuAssignRoles.setLabel("Assign roles"); //$NON-NLS-1$
        menuAssignRoles.setIcon(request.getContextPath() + "/.resources/icons/16/hat_white.gif"); //$NON-NLS-1$
        menuAssignRoles.setOnclick("mgnlTreeMenuOpenDialog(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ",'.magnolia/dialogs/groupassignroles.html');"); //$NON-NLS-1$
        menuAssignRoles.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuDelete = new ContextMenuItem("delete");
        menuDelete.setLabel("delete"); //$NON-NLS-1$
        menuDelete.setIcon(request.getContextPath() + "/.resources/icons/16/delete2.gif"); //$NON-NLS-1$
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();"); //$NON-NLS-1$
        menuDelete.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        ContextMenuItem menuMove = new ContextMenuItem("move");
        menuMove.setLabel("move"); //$NON-NLS-1$
        menuMove.setIcon(request.getContextPath() + "/.resources/icons/16/up_down.gif"); //$NON-NLS-1$
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();"); //$NON-NLS-1$
        menuMove.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        ContextMenuItem menuCopy = new ContextMenuItem("copy");
        menuCopy.setLabel("copy"); //$NON-NLS-1$
        menuCopy.setIcon(request.getContextPath() + "/.resources/icons/16/copy.gif"); //$NON-NLS-1$
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();"); //$NON-NLS-1$
        menuCopy.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        ContextMenuItem menuActivateExcl = new ContextMenuItem("activate");
        menuActivateExcl.setLabel("activate"); //$NON-NLS-1$
        menuActivateExcl.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_right_green.gif"); //$NON-NLS-1$
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);"); //$NON-NLS-1$ //$NON-NLS-2$
        menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$
        ContextMenuItem menuDeActivate = new ContextMenuItem("deactivate");
        menuDeActivate.setLabel("deactivate"); //$NON-NLS-1$
        menuDeActivate.setIcon(request.getContextPath() + "/.resources/icons/16/arrow_left_red.gif"); //$NON-NLS-1$
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");"); //$NON-NLS-1$ //$NON-NLS-2$
        menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        if (!Server.isAdmin()) {
            menuOpen.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuNewPage.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!Subscriber.isSubscribersEnabled()) {
            menuActivateExcl.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
            menuDeActivate.addJavascriptCondition("new mgnlTreeMenuItemConditionBoolean(false)"); //$NON-NLS-1$
        }

        if (!this.isBrowseMode()) {
            tree.addMenuItem(menuNewPage);
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(menuAssignRoles);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuDelete);

            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeActivate);
        }
        else {
            ContextMenuItem menuRefresh = new ContextMenuItem("refresh");
            menuRefresh.setLabel("refresh"); //$NON-NLS-1$
            menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif"); //$NON-NLS-1$
            menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$
            tree.addMenuItem(menuRefresh);
        }

    }

    /**
     * @see info.magnolia.module.admininterface.AdminTreeMVCHandler#prepareFunctionBar(info.magnolia.cms.gui.control.Tree,
     * javax.servlet.http.HttpServletRequest)
     */
    protected void prepareFunctionBar(Tree tree, HttpServletRequest request) {
        // Messages msgs = MessagesManager.getMessages();
        ContextMenu cm = tree.getMenu();
        ContextMenuItem cmItem = cm.getMenuItemByName("edit");
        if (cmItem != null) {
            tree.addFunctionBarItem(new FunctionBarItem(cmItem));
        }
        cmItem = cm.getMenuItemByName("new");
        if (cmItem != null) {
            tree.addFunctionBarItem(new FunctionBarItem(cmItem));
        }
        // null is separator :)
        tree.addFunctionBarItem(null);
        cmItem = cm.getMenuItemByName("activate");
        if (cmItem != null) {
            tree.addFunctionBarItem(new FunctionBarItem(cmItem));
        }
        cmItem = cm.getMenuItemByName("deactivate");
        if (cmItem != null) {
            tree.addFunctionBarItem(new FunctionBarItem(cmItem));
        }
        tree.addFunctionBarItem(null);
        ContextMenuItem menuRefresh = new ContextMenuItem("refresh");
        menuRefresh.setLabel("refresh"); //$NON-NLS-1$
        menuRefresh.setIcon(request.getContextPath() + "/.resources/icons/16/refresh.gif"); //$NON-NLS-1$
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$
        tree.addFunctionBarItem(new FunctionBarItem(menuRefresh));
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    protected String getRepository() {
        return "usergroups";
    }

}
