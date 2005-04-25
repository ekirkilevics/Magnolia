package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ControlSuper;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogButtonSet;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogPassword;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.cms.gui.misc.Sources;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.servlets.BasePageServlet;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserEditDialogPage extends BasePageServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    static Logger log = Logger.getLogger("user dialog");

    // todo: permission global available somewhere
    static final long PERMISSION_ALL = Permission.ALL;

    static final long PERMISSION_READ = Permission.READ;

    static final long PERMISSION_NO = 0;

    static final String NODE_ACLUSERS = "acl_users";

    static final String NODE_ACLROLES = "acl_userroles";

    static final String NODE_ROLES = "roles";

    static final String CONTROLNAME_ISADMIN_USERS = "permissionUsers";

    static final String CONTROLNAME_ISADMIN_ROLES = "permissionRoles";

    // static final String CONTROLNAME_ISADMIN_ACTIVATE="permissionActivate";
    static final String NODE_ACLCONFIG = "acl_config";

    static final String CONTROLNAME_ISADMIN_CONFIG = "permissionConfig";

    /**
     * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
     */
    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {
        PrintWriter out = response.getWriter();
        Messages msgs = MessagesManager.getMessages(request);

        MultipartForm form = Resource.getPostedForm(request);
        boolean drawDialog = true;

        String path = "";
        String nodeCollectionName = "";
        String nodeName = "";
        String paragraph = "";
        String richE = "";
        String richEPaste = "";
        String repository;

        if (form != null) {
            path = form.getParameter("mgnlPath");
            repository = form.getParameter("mgnlRepository");
        }
        else {
            path = request.getParameter("mgnlPath");
            repository = request.getParameter("mgnlRepository");
        }

        if (repository == null)
            repository = ContentRepository.USERS;

        HierarchyManager hm = new HierarchyManager(request);
        boolean create = false;
        if (path.equals(""))
            create = true;

        try {
            Session t = SessionAccessControl.getSession(request, repository);
            Node rootNode = t.getRootNode();
            hm.init(rootNode);
        }
        catch (Exception e) {
        }

        Content user = null;
        if (!create) {
            try {
                user = hm.getPage(path);
            }
            catch (RepositoryException re) {
                re.printStackTrace();
            }
        }

        if (form != null) {
            // save

            // create new user
            if (create) {
                String name = form.getParameter("name");
                path = "/" + name;
                try {
                    user = hm.createPage("/", name);
                }
                catch (RepositoryException re) {
                    re.printStackTrace();
                }
            }

            // ######################
            // # write to .node.xml (controls with saveInfo (full name, password))
            // ######################
            Save nodeXml = new Save(form, request);
            nodeXml.setPath(path);
            nodeXml.save();

            // ######################
            // # write users and roles acl
            // ######################

            
            // remove existing
            try {
                user.deleteContentNode(NODE_ACLUSERS);
            }
            catch (RepositoryException re) {
            }
            try {
                user.deleteContentNode(NODE_ACLROLES);
            }
            catch (RepositoryException re) {
            }

            try {
                user.deleteContentNode(NODE_ACLCONFIG);
            }
            catch (RepositoryException re) {
            }

            // rewrite
            try {

                ContentNode aclUsers;

                aclUsers = user.createContentNode(NODE_ACLUSERS);

                ContentNode aclRoles = user.createContentNode(NODE_ACLROLES);
                ContentNode aclConfig = user.createContentNode(NODE_ACLCONFIG);
                Save save = new Save();

                if (form.getParameter(CONTROLNAME_ISADMIN_USERS).equals("true")) {
                    // System.out.println("IS user admin");
                    // is user admin
                    // full access to all users
                    ContentNode u0 = aclUsers.createContentNode("0");
                    u0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    u0.createNodeData("permissions", save.getValue(PERMISSION_ALL), PropertyType.LONG);
                }
                else {
                    // not users admin
                    // allow access to own user
                    ContentNode u0 = aclUsers.createContentNode("00");
                    u0.createNodeData("path", save.getValue(user.getHandle() + "/" + NODE_ROLES), PropertyType.STRING);
                    u0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                    ContentNode u1 = aclUsers.createContentNode("01");
                    u1.createNodeData(
                        "path",
                        save.getValue(user.getHandle() + "/" + NODE_ROLES + "/*"),
                        PropertyType.STRING);
                    u1.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                    ContentNode u2 = aclUsers.createContentNode("02");
                    u2.createNodeData("path", save.getValue(user.getHandle()), PropertyType.STRING);
                    u2.createNodeData("permissions", save.getValue(PERMISSION_ALL), PropertyType.LONG);

                    ContentNode u3 = aclUsers.createContentNode("03");
                    u3.createNodeData("path", save.getValue(user.getHandle() + "/*"), PropertyType.STRING);
                    u3.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                    ContentNode u4 = aclUsers.createContentNode("04");
                    u4.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    u4.createNodeData("permissions", save.getValue(PERMISSION_NO), PropertyType.LONG);
                }

                if (form.getParameter(CONTROLNAME_ISADMIN_ROLES) != null) {
                    // is roles admin:
                    // full access to all roles
                    ContentNode r0 = aclRoles.createContentNode("0");
                    r0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    r0.createNodeData("permissions", save.getValue(PERMISSION_ALL), PropertyType.LONG);
                }
                else {
                    // not roles admin:
                    // read access to all roles
                    ContentNode r0 = aclRoles.createContentNode("0");
                    r0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    r0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);
                }

                if (form.getParameter(CONTROLNAME_ISADMIN_CONFIG) != null) {
                    // is config admin:
                    // full access to entire config repository
                    ContentNode c0 = aclConfig.createContentNode("0");
                    c0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    c0.createNodeData("permissions", save.getValue(PERMISSION_ALL), PropertyType.LONG);
                }
                else {
                    // not config admin:
                    // read access to config repository
                    ContentNode c0 = aclConfig.createContentNode("0");
                    c0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                    c0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);
                }

                // ######################
                // # roles acl
                // ######################
                // remove existing
                try {
                    user.deleteContentNode(NODE_ROLES);
                }
                catch (RepositoryException re) {
                }

                // rewrite
                ContentNode roles = user.createContentNode(NODE_ROLES);

                String[] rolesValue = form.getParameter("aclList").split(";");
                // System.out.println(form.getParameter("aclList"));
                for (int i = 0; i < rolesValue.length; i++) {
                    try {
                        String newLabel = Path.getUniqueLabel(hm, roles.getHandle(), "0");
                        ContentNode r = roles.createContentNode(newLabel);
                        r.createNodeData("path").setValue(rolesValue[i]);
                    }
                    catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }

                hm.save();
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }

            out.println("<html>");
            out.println(new Sources(request.getContextPath()).getHtmlJs());
            out.println("<script type=\"text/javascript\">");
            // out.println("opener.mgnlUsersTree.refresh();");
            out.println("opener.mgnlTree.refresh();");
            out.println("window.close();");
            out.println("</script></html>");
            drawDialog = false;
        }
        else {
            nodeCollectionName = request.getParameter("mgnlNodeCollection");
            nodeName = request.getParameter("mgnlNode");
            paragraph = request.getParameter("mgnlParagraph");
            richE = request.getParameter("mgnlRichE");
            richEPaste = request.getParameter("mgnlRichEPaste");
        }

        if (drawDialog) {
            DialogDialog dialog = DialogFactory.getDialogDialogInstance(request, response, user, null);

            dialog.setConfig("path", path);
            dialog.setConfig("nodeCollection", nodeCollectionName);
            dialog.setConfig("node", nodeName);
            dialog.setConfig("paragraph", paragraph);
            dialog.setConfig("richE", richE);
            dialog.setConfig("richEPaste", richEPaste);
            dialog.setConfig("repository", repository);
            dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js");

            // opener.document.location.reload();window.close();

            dialog.setConfig("width", DialogDialog.DIALOGSIZE_SLIM_WIDTH);
            dialog.setConfig("height", DialogDialog.DIALOGSIZE_SLIM_HEIGHT);

            if (create)
                dialog.setLabel(msgs.get("users.edit.create"));
            else
                dialog.setLabel(msgs.get("users.edit.edit"));

            DialogTab tab = dialog.addTab();

            DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
            spacer.setConfig("line", false);

            DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request, response, null, null);
            lineHalf.setConfig("line", false);

            if (!create) {
                DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
                // name.setConfig("line",false);
                name.setLabel("<strong>"+msgs.get("users.edit.username")+"</strong>");
                name.setValue("<strong>" + user.getName() + "</strong>");
                tab.addSub(name);
            }
            else {
                DialogEdit name = DialogFactory.getDialogEditInstance(request, response, null, null);
                name.setName("name");
                name.setConfig("onchange", "mgnlDialogVerifyName(this.id);");
                name.setSaveInfo(false);
                name.setLabel("<strong>"+msgs.get("users.edit.username")+"</strong>");
                name.setDescription("Legal characters: a-z, 0-9, _ (underscore), - (divis)");
                tab.addSub(name);
            }

            tab.addSub(spacer);

            DialogEdit title = DialogFactory.getDialogEditInstance(request, response, user, null);
            title.setName("title");
            title.setLabel(msgs.get("users.edit.fullname"));

            if (create) {
                title.setConfig("onchange", "mgnlAclSetName(this.value);");
            }
            tab.addSub(title);

            DialogPassword pswd = DialogFactory.getDialogPasswordInstance(request, response, user, null);
            pswd.setName("pswd");
            pswd.setLabel(msgs.get("users.edit.password"));
            if (!create) {
                pswd.setConfig("labelDescription",msgs.get("users.edit.leaveEmpty"));
            }
            tab.addSub(pswd);

            tab.addSub(spacer);

            DialogButtonSet isUserAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
            isUserAdmin.setName(CONTROLNAME_ISADMIN_USERS);
            isUserAdmin.setConfig("type", PropertyType.TYPENAME_BOOLEAN);
            isUserAdmin.setConfig("lineSemi", true);
            isUserAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
            Button isUserAdminButton = new Button();
            isUserAdminButton.setLabel(msgs.get("users.edit.usersAdministrator"));
            isUserAdminButton.setValue("true");
            isUserAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" + isUserAdmin.getName() + "');");
            isUserAdmin.addOption(isUserAdminButton);
            tab.addSub(isUserAdmin);

            DialogButtonSet isRoleAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
            isRoleAdmin.setName(CONTROLNAME_ISADMIN_ROLES);
            isRoleAdmin.setConfig("type", PropertyType.TYPENAME_BOOLEAN);
            isRoleAdmin.setConfig("lineSemi", true);
            isRoleAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
            Button isRolesAdminButton = new Button();
            isRolesAdminButton.setLabel(msgs.get("users.edit.rolesAdministrator"));
            isRolesAdminButton.setValue("true");
            isRolesAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" + isRoleAdmin.getName() + "');");
            isRoleAdmin.addOption(isRolesAdminButton);
            tab.addSub(isRoleAdmin);

            DialogButtonSet isConfigAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
            isConfigAdmin.setName(CONTROLNAME_ISADMIN_CONFIG);
            isConfigAdmin.setConfig("type", PropertyType.TYPENAME_BOOLEAN);
            isConfigAdmin.setConfig("lineSemi", true);
            isConfigAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX);
            Button isConfigAdminButton = new Button();
            isConfigAdminButton.setLabel(msgs.get("users.edit.configAdministrator"));
            isConfigAdminButton.setValue("true");
            isConfigAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" + isConfigAdmin.getName() + "');");
            isConfigAdmin.addOption(isConfigAdminButton);
            tab.addSub(isConfigAdmin);

            tab.addSub(spacer);

            DialogInclude roles = DialogFactory.getDialogIncludeInstance(request, response, user, null);
            roles.setLabel(msgs.get("users.edit.roles"));
            roles.setName("aclRolesRepository");
            roles.setConfig("file", "/admintemplates/adminCentral/dialogs/usersEdit/includeRoles.jsp");
            tab.addSub(roles);

            DialogButton add = DialogFactory.getDialogButtonInstance(request, response, null, null);
            add.setConfig("buttonLabel", msgs.get("buttons.add"));
            add.setConfig("lineSemi", true);
            add.setConfig("onclick", "mgnlAclAdd(true,-1);");
            tab.addSub(add);

            dialog.setConfig("saveOnclick", "mgnlAclFormSubmit(true);");

            dialog.drawHtml(out);
        }

    }

}
