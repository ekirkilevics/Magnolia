package info.magnolia.module.adminInterface;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeMenuItem;

import javax.servlet.http.HttpServletRequest;


/**
 * Handles the tree rendering for the "users" repository.
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class AdminTreeUsers implements AdminTree {

    /**
     * @see AdminTree#configureTree(Tree, HttpServletRequest, String, String, String, boolean, String)
     */
    public void configureTree(Tree tree, HttpServletRequest request, String path, String pathOpen, String pathSelected,
        boolean create, String createItemType) {

        tree.setDrawShifter(false);
        // context path is already added by Tree
        tree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif");
        if (Server.isAdmin())
            tree.setIconOndblclick("mgnlTreeMenuOpenDialog("
                + tree.getJavascriptTree()
                + ",'.magnolia/adminCentral/users/dialog.html');");
        tree.addItemType(ItemType.NT_CONTENT);
        if (create) {
            tree.createNode(createItemType);
        }
        else {
            tree.setPathOpen(pathOpen);
            tree.setPathSelected(pathSelected);
        }
        TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
        column0.setIsLabel(true);
        if (Server.isAdmin())
            column0.setHtmlEdit();
        column0.setTitle("User name");
        column0.setWidth(2);
        TreeColumn column1 = new TreeColumn(tree.getJavascriptTree(), request);
        column1.setName("title");
        if (Server.isAdmin())
            column1.setHtmlEdit();
        column1.setTitle("Full name");
        column1.setWidth(2);
        TreeColumn columnIcons = new TreeColumn(tree.getJavascriptTree(), request);
        columnIcons.setCssClass("");
        columnIcons.setWidth(1);
        columnIcons.setIsIcons(true);
        columnIcons.setIconsActivation(true);
        TreeColumn column2 = new TreeColumn(tree.getJavascriptTree(), request);
        column2.setName(MetaData.LAST_MODIFIED);
        column2.setIsMeta(true);
        column2.setDateFormat("yyyy-MM-dd, HH:mm");
        column2.setTitle("Mod. date");
        column2.setWidth(2);
        tree.addColumn(column0);
        tree.addColumn(column1);
        if (Server.isAdmin()) {
            tree.addColumn(columnIcons);
        }
        tree.addColumn(column2);
        TreeMenuItem menuOpen = new TreeMenuItem();
        menuOpen.setLabel("Edit user...");
        menuOpen.setOnclick("mgnlTreeMenuOpenDialog("
            + tree.getJavascriptTree()
            + ",'.magnolia/adminCentral/users/dialog.html');");
        menuOpen.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuNewPage = new TreeMenuItem();
        menuNewPage.setLabel("New user");
        menuNewPage.setOnclick(tree.getJavascriptTree() + ".createRootNode('" + ItemType.NT_CONTENT + "');");
        TreeMenuItem menuDelete = new TreeMenuItem();
        menuDelete.setLabel("Delete");
        menuDelete.setOnclick(tree.getJavascriptTree() + ".deleteNode();");
        menuDelete.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuMove = new TreeMenuItem();
        menuMove.setLabel("Move user");
        menuMove.setOnclick(tree.getJavascriptTree() + ".cutNode();");
        menuMove.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuCopy = new TreeMenuItem();
        menuCopy.setLabel("Copy user");
        menuCopy.setOnclick(tree.getJavascriptTree() + ".copyNode();");
        menuCopy.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuActivateExcl = new TreeMenuItem();
        menuActivateExcl.setLabel("Activate user");
        menuActivateExcl.setOnclick(tree.getJavascriptTree() + ".activateNode(" + Tree.ACTION_ACTIVATE + ",false);");
        menuActivateExcl.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuDeActivate = new TreeMenuItem();
        menuDeActivate.setLabel("De-activate user");
        menuDeActivate.setOnclick(tree.getJavascriptTree() + ".deActivateNode(" + Tree.ACTION_DEACTIVATE + ");");
        menuDeActivate.addJavascriptCondition("mgnlTreeMenuItemConditionSelectedNotRoot");
        TreeMenuItem menuRefresh = new TreeMenuItem();
        menuRefresh.setLabel("Refresh");
        menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();");
        if (Server.isAdmin()) {
            tree.addMenuItem(menuOpen);
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuNewPage);
        }
        tree.addMenuItem(menuDelete);
        if (Server.isAdmin()) {
            tree.addMenuItem(null); // line
            tree.addMenuItem(menuActivateExcl);
            tree.addMenuItem(menuDeActivate);
        }
        tree.addMenuItem(null); // line
        tree.addMenuItem(menuRefresh);

    }

}
