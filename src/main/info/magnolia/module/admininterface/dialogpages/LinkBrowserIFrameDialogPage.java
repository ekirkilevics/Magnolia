package info.magnolia.module.admininterface.dialogpages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.gui.misc.Spacer;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.module.admininterface.DialogPageMVCHandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

//import com.obinary.magnolia.module.dms.gui.DMSTreeControl;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class LinkBrowserIFrameDialogPage extends DialogPageMVCHandler {

    public LinkBrowserIFrameDialogPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    protected void draw(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        Messages msgs = MessagesManager.getMessages(request);
        // WARNING: no white spaces tolerated for save nodeData!

        int treeHeight = 50;

        String repository = request.getParameter("repository"); //$NON-NLS-1$
        if (StringUtils.isEmpty(repository)) {
            repository = ContentRepository.WEBSITE;
        }

        String path = request.getParameter("path"); //$NON-NLS-1$
        if (StringUtils.isEmpty(path)) {
            path = "/"; //$NON-NLS-1$
        }

        String pathOpen = request.getParameter("pathOpen"); //$NON-NLS-1$
        String pathSelected = request.getParameter("pathSelected"); //$NON-NLS-1$

        StringBuffer html = new StringBuffer();

        boolean snippetMode = false;
        String mode = request.getParameter("treeMode"); //$NON-NLS-1$
        if (mode == null) {
            mode = StringUtils.EMPTY;
        }
        if (mode.equals("snippet")) { //$NON-NLS-1$
            snippetMode = true;
        }

        if (!snippetMode) {
            html.append("<html><head>"); //$NON-NLS-1$
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //$NON-NLS-1$
            html.append(new Sources(request.getContextPath()).getHtmlJs());
            html.append(new Sources(request.getContextPath()).getHtmlCss());
            html.append("</head>"); //$NON-NLS-1$

            html.append("<body class=\"mgnlBgDark\" onload=\"mgnlTreeControl.resize();\">"); //$NON-NLS-1$

            html.append(Spacer.getHtml(20, 20));

            out.println(html);

        }

        // if (!snippetMode || repository.equals(ContentRepository.WEBSITE)) {
        if (repository.equals(ContentRepository.WEBSITE)) {
            Tree websiteTree = new Tree(ContentRepository.WEBSITE, request);
            websiteTree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
            websiteTree.setSnippetMode(snippetMode);
            websiteTree.setHeight(treeHeight);

            websiteTree.setPath(path);

            websiteTree.setPathOpen(pathOpen);
            websiteTree.setPathSelected(pathSelected);

            websiteTree.addItemType(ItemType.CONTENT);

            TreeColumn column0 = new TreeColumn(websiteTree.getJavascriptTree(), request);
            column0.setIsLabel(true);
            column0.setWidth(3);

            TreeColumn column1 = new TreeColumn(websiteTree.getJavascriptTree(), request);
            column1.setName("title"); //$NON-NLS-1$
            column1.setTitle(msgs.get("linkbrowser.web.title")); //$NON-NLS-1$
            column1.setWidth(2);

            websiteTree.addColumn(column0);
            websiteTree.addColumn(column1);

            ContextMenuItem menuRefresh = new ContextMenuItem();
            menuRefresh.setLabel(msgs.get("linkbrowser.refresh")); //$NON-NLS-1$
            menuRefresh.setOnclick(websiteTree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

            websiteTree.addMenuItem(menuRefresh);

            String display = "none"; //$NON-NLS-1$
            if (repository.equals(ContentRepository.WEBSITE)) {
                display = "block"; //$NON-NLS-1$
            }

            if (!snippetMode) {
                out.println("<div id=" //$NON-NLS-1$
                    + websiteTree.getJavascriptTree() + "_DivSuper style=\"display:" //$NON-NLS-1$
                    + display + ";\">"); //$NON-NLS-1$
            }
            out.print(websiteTree.getHtml()); // print, not println! because of snippet mode!
            if (!snippetMode) {
                out.println("</div>"); //$NON-NLS-1$
            }

        }

        // if (!snippetMode || repository.equals(ContentRepository.USERS)) {
        if (repository.equals(ContentRepository.USERS)) {
            Tree usersTree = new Tree(ContentRepository.USERS, request);
            // usersTree.setJavascriptTree("mgnlUsersTree");
            usersTree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
            usersTree.setSnippetMode(snippetMode);
            usersTree.setHeight(treeHeight);
            usersTree.setDrawShifter(false);
            usersTree.setIconPage(Tree.ICONDOCROOT + "pawn_glass_yellow.gif"); //$NON-NLS-1$

            usersTree.addItemType(ItemType.CONTENT);

            usersTree.setPathOpen(pathOpen);
            usersTree.setPathSelected(pathSelected);

            TreeColumn column0 = new TreeColumn(usersTree.getJavascriptTree(), request);
            column0.setIsLabel(true);
            column0.setTitle(msgs.get("linkbrowser.user.username")); //$NON-NLS-1$
            column0.setWidth(2);

            TreeColumn column1 = new TreeColumn(usersTree.getJavascriptTree(), request);
            column1.setName("title"); //$NON-NLS-1$
            column1.setTitle(msgs.get("linkbrowser.user.fullname")); //$NON-NLS-1$
            column1.setWidth(2);

            usersTree.addColumn(column0);
            usersTree.addColumn(column1);

            ContextMenuItem menuRefresh = new ContextMenuItem();
            menuRefresh.setLabel(msgs.get("linkbrowser.refresh")); //$NON-NLS-1$
            menuRefresh.setOnclick(usersTree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

            usersTree.addMenuItem(menuRefresh);

            String display = "none"; //$NON-NLS-1$
            if (repository.equals(ContentRepository.USERS)) {
                display = "block"; //$NON-NLS-1$
            }

            if (!snippetMode) {
                out.println("<div id=" //$NON-NLS-1$
                    + usersTree.getJavascriptTree() + "_DivSuper style=\"display:" //$NON-NLS-1$
                    + display + ";\">"); //$NON-NLS-1$
            }
            out.print(usersTree.getHtml()); // print, not println! because of snippet mode!
            if (!snippetMode) {
                out.println("</div>"); //$NON-NLS-1$
            }
        }

        // if (!snippetMode || repository.equals(ContentRepository.USER_ROLES)) {
        if (repository.equals(ContentRepository.USER_ROLES)) {
            Tree rolesTree = new Tree(ContentRepository.USER_ROLES, request);
            // rolesTree.setJavascriptTree("mgnlUserRolesTree");
            rolesTree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
            rolesTree.setSnippetMode(snippetMode);
            rolesTree.setHeight(treeHeight);
            rolesTree.setDrawShifter(false);
            rolesTree.setIconPage(Tree.ICONDOCROOT + "hat_white.gif"); //$NON-NLS-1$

            rolesTree.addItemType(ItemType.CONTENT);

            rolesTree.setPathOpen(pathOpen);
            rolesTree.setPathSelected(pathSelected);

            TreeColumn column0 = new TreeColumn(rolesTree.getJavascriptTree(), request);
            column0.setIsLabel(true);
            column0.setWidth(2);
            column0.setTitle("name"); //$NON-NLS-1$

            TreeColumn column1 = new TreeColumn(rolesTree.getJavascriptTree(), request);
            column1.setName("title"); //$NON-NLS-1$
            column1.setWidth(2);
            column1.setTitle(msgs.get("linkbrowser.role.fullrolename")); //$NON-NLS-1$

            rolesTree.addColumn(column0);
            rolesTree.addColumn(column1);

            ContextMenuItem menuRefresh = new ContextMenuItem();
            menuRefresh.setLabel(msgs.get("linkbrowser.refresh")); //$NON-NLS-1$
            menuRefresh.setOnclick(rolesTree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

            rolesTree.addMenuItem(menuRefresh);

            String display = "none"; //$NON-NLS-1$
            if (repository.equals(ContentRepository.USER_ROLES)) {
                display = "block"; //$NON-NLS-1$
            }

            if (!snippetMode) {
                out.println("<div id=" //$NON-NLS-1$
                    + rolesTree.getJavascriptTree() + "_DivSuper style=\"display:" //$NON-NLS-1$
                    + display + ";\">"); //$NON-NLS-1$
            }
            out.print(rolesTree.getHtml()); // print, not println! because of snippet mode!
            if (!snippetMode) {
                out.println("</div>"); //$NON-NLS-1$
            }
        }

        if (repository.equals(ContentRepository.CONFIG)) {
            Tree websiteTree = new Tree(ContentRepository.CONFIG, request);
            // websiteTree.setJavascriptTree("mgnlWebsiteTree");
            websiteTree.setIconPage(Tree.ICONDOCROOT + "folder_cubes.gif"); //$NON-NLS-1$
            websiteTree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
            websiteTree.setSnippetMode(snippetMode);
            websiteTree.setHeight(treeHeight);

            websiteTree.setPath(path);

            websiteTree.setPathOpen(pathOpen);
            websiteTree.setPathSelected(pathSelected);

            websiteTree.addItemType(ItemType.CONTENT);
            websiteTree.addItemType(ItemType.CONTENTNODE);
            websiteTree.addItemType(ItemType.NT_NODEDATA);

            TreeColumn column0 = new TreeColumn(websiteTree.getJavascriptTree(), request);
            column0.setIsLabel(true);
            column0.setWidth(3);

            websiteTree.addColumn(column0);

            ContextMenuItem menuRefresh = new ContextMenuItem();
            menuRefresh.setLabel(msgs.get("linkbrowser.refresh")); //$NON-NLS-1$
            menuRefresh.setOnclick(websiteTree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

            websiteTree.addMenuItem(menuRefresh);

            String display = "none"; //$NON-NLS-1$
            if (repository.equals(ContentRepository.CONFIG)) {
                display = "block"; //$NON-NLS-1$
            }

            if (!snippetMode) {
                out.println("<div id=" //$NON-NLS-1$
                    + websiteTree.getJavascriptTree() + "_DivSuper style=\"display:" //$NON-NLS-1$
                    + display + ";\">"); //$NON-NLS-1$
            }
            out.print(websiteTree.getHtml()); // print, not println! because of snippet mode!
            if (!snippetMode) {
                out.println("</div>"); //$NON-NLS-1$
            }
        }

        // TODO remove this
        // this is definitly a hack
        if (repository.equals("dms")) {
            //DMSTreeControl tree = new DMSTreeControl("dms", request, response); 
            Tree tree = new Tree("dms", request);
            // not the new header
            //tree.setNewDesign(false);
            
            // websiteTree.setJavascriptTree("mgnlWebsiteTree");
            tree.setIconPage("/admindocroot/icons/16/folder.gif");
            tree.setIconContentNode("/admindocroot/fileIcons/general.gif");
            
            tree.setJavascriptTree("mgnlTreeControl"); //$NON-NLS-1$
            tree.setSnippetMode(snippetMode);
            tree.setHeight(treeHeight);

            tree.setPath(path);

            tree.setPathOpen(pathOpen);
            tree.setPathSelected(pathSelected);

            tree.addItemType(ItemType.CONTENT);
            tree.addItemType(ItemType.CONTENTNODE);

            TreeColumn column0 = new TreeColumn(tree.getJavascriptTree(), request);
            column0.setIsLabel(true);
            column0.setWidth(3);

            tree.addColumn(column0);

            ContextMenuItem menuRefresh = new ContextMenuItem();
            menuRefresh.setLabel(msgs.get("linkbrowser.refresh")); //$NON-NLS-1$
            menuRefresh.setOnclick(tree.getJavascriptTree() + ".refresh();"); //$NON-NLS-1$

            tree.addMenuItem(menuRefresh);

            String display = "none"; //$NON-NLS-1$
            if (repository.equals("dms")) {
                display = "block"; //$NON-NLS-1$
            }

            if (!snippetMode) {
                out.println("<div id=" //$NON-NLS-1$
                    + tree.getJavascriptTree() + "_DivSuper style=\"display:" //$NON-NLS-1$
                    + display + ";\">"); //$NON-NLS-1$
            }
            out.print(tree.getHtml()); // print, not println! because of snippet mode!
            if (!snippetMode) {
                out.println("</div>"); //$NON-NLS-1$
            }
        }

        if (!snippetMode) {
            out.println("</body></html>"); //$NON-NLS-1$
        }

        // WARNING: no white spaces below!
    }

}
