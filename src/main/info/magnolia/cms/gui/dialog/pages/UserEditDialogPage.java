package info.magnolia.cms.gui.dialog.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogPassword;
import info.magnolia.cms.gui.dialog.DialogSelect;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserEditDialogPage extends BasePageServlet {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger("user dialog");

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    // todo: permission global available somewhere
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

    private static final String NODE_ACLUSERS = "acl_users";

    private static final String NODE_ACLROLES = "acl_userroles";

    private static final String NODE_ROLES = "roles";

    // static final String CONTROLNAME_ISADMIN_USERS = "permissionUsers";

    // static final String CONTROLNAME_ISADMIN_ROLES = "permissionRoles";

    // static final String CONTROLNAME_ISADMIN_ACTIVATE="permissionActivate";
    private static final String NODE_ACLCONFIG = "acl_config";

    private static final String CONTROLNAME_ISADMIN_CONFIG = "permissionConfig";

    private class MyDialog {

        private HttpServletRequest request;

        private HttpServletResponse response;

        private PrintWriter out;

        private Messages msgs;

        private MultipartForm form;

        private String path;

        private String nodeCollectionName;

        private String nodeName;

        private String paragraph;

        private String richE;

        private String richEPaste;

        private String repository;

        private HierarchyManager hm;

        private boolean create;

        private Content user;

        MyDialog(HttpServletRequest request, HttpServletResponse response) throws IOException {
            this.request = request;
            this.response = response;
            this.out = response.getWriter();
            this.msgs = MessagesManager.getMessages(request);
            this.form = Resource.getPostedForm(request);

            this.path = "";
            this.nodeCollectionName = "";
            this.nodeName = "";
            this.paragraph = "";
            this.richE = "";
            this.richEPaste = "";

            this.create = false;

            hm = new HierarchyManager(request);

            if (form != null) {
                path = form.getParameter("mgnlPath");
                repository = form.getParameter("mgnlRepository");
            }
            else {
                path = request.getParameter("mgnlPath");
                repository = request.getParameter("mgnlRepository");
            }

            if (repository == null) {
                repository = ContentRepository.USERS;
            }
            if (StringUtils.isEmpty(path)) {
                create = true;
            }

            hm = SessionAccessControl.getHierarchyManager(request, repository);

            if (!create) {
                try {
                    user = hm.getContent(path);
                }
                catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        /**
         * @see info.magnolia.cms.servlets.BasePageServlet#draw(HttpServletRequest, HttpServletResponse)
         */
        public void execute() throws IOException, RepositoryException {

            if (form != null) {
                // save

                save();

                out.println("<html>");
                out.println(new Sources(request.getContextPath()).getHtmlJs());
                out.println("<script type=\"text/javascript\">");
                // out.println("opener.mgnlUsersTree.refresh();");
                out.println("opener.mgnlTree.refresh();");
                out.println("window.close();");
                out.println("</script></html>");

            }
            else {
                nodeCollectionName = request.getParameter("mgnlNodeCollection");
                nodeName = request.getParameter("mgnlNode");
                paragraph = request.getParameter("mgnlParagraph");
                richE = request.getParameter("mgnlRichE");
                richEPaste = request.getParameter("mgnlRichEPaste");

                DialogDialog dialog = createDialog();
                dialog.drawHtml(out);
            }

        }

        /**
         * @return
         * @throws RepositoryException
         */
        private DialogDialog createDialog() throws RepositoryException {
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

            if (create) {
                dialog.setLabel(msgs.get("users.edit.create"));
            }
            else {
                dialog.setLabel(msgs.get("users.edit.edit"));
            }

            DialogTab tab = dialog.addTab();

            DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
            spacer.setConfig("line", false);

            DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request, response, null, null);
            lineHalf.setConfig("line", false);

            if (!create) {
                DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
                // name.setConfig("line",false);
                name.setLabel("<strong>" + msgs.get("users.edit.username") + "</strong>");
                name.setValue("<strong>" + user.getName() + "</strong>");
                tab.addSub(name);
            }
            else {
                DialogEdit name = DialogFactory.getDialogEditInstance(request, response, null, null);
                name.setName("name");
                name.setConfig("onchange", "mgnlDialogVerifyName(this.id);");
                name.setSaveInfo(false);
                name.setLabel("<strong>" + msgs.get("users.edit.username") + "</strong>");
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
                pswd.setConfig("labelDescription", msgs.get("users.edit.leaveEmpty"));
            }
            tab.addSub(pswd);

            tab.addSub(spacer);

            // select language
            DialogSelect langSelect = DialogFactory.getDialogSelectInstance(request, response, user, null);
            langSelect.setName("language");
            langSelect.setLabel(msgs.get("users.edit.language"));
            List options = new ArrayList();

            Collection col = MessagesManager.getAvailableLocales();
            Messages langMsgs = MessagesManager.getMessages(
                request,
                "info.magnolia.module.admininterface.messages_languages");

            for (Iterator iter = col.iterator(); iter.hasNext();) {
                Locale locale = (Locale) iter.next();
                String code = locale.getLanguage();
                String name = langMsgs.get(code);
                SelectOption option = new SelectOption(name, code);
                options.add(option);
            }
            langSelect.setOptions(options);
            tab.addSub(langSelect);
            tab.addSub(spacer);

            // adding the roles checkboxes
            /*
             * DialogButtonSet isUserAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
             * isUserAdmin.setName(CONTROLNAME_ISADMIN_USERS); isUserAdmin.setConfig("type",
             * PropertyType.TYPENAME_BOOLEAN); isUserAdmin.setConfig("lineSemi", true);
             * isUserAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX); Button isUserAdminButton = new Button();
             * isUserAdminButton.setLabel(msgs.get("users.edit.usersAdministrator"));
             * isUserAdminButton.setValue("true"); isUserAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" +
             * isUserAdmin.getName() + "');"); isUserAdmin.addOption(isUserAdminButton); tab.addSub(isUserAdmin);
             * DialogButtonSet isRoleAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
             * isRoleAdmin.setName(CONTROLNAME_ISADMIN_ROLES); isRoleAdmin.setConfig("type",
             * PropertyType.TYPENAME_BOOLEAN); isRoleAdmin.setConfig("lineSemi", true);
             * isRoleAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX); Button isRolesAdminButton = new Button();
             * isRolesAdminButton.setLabel(msgs.get("users.edit.rolesAdministrator"));
             * isRolesAdminButton.setValue("true"); isRolesAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" +
             * isRoleAdmin.getName() + "');"); isRoleAdmin.addOption(isRolesAdminButton); tab.addSub(isRoleAdmin);
             * DialogButtonSet isConfigAdmin = DialogFactory.getDialogButtonSetInstance(request, response, user, null);
             * isConfigAdmin.setName(CONTROLNAME_ISADMIN_CONFIG); isConfigAdmin.setConfig("type",
             * PropertyType.TYPENAME_BOOLEAN); isConfigAdmin.setConfig("lineSemi", true);
             * isConfigAdmin.setButtonType(ControlSuper.BUTTONTYPE_CHECKBOX); Button isConfigAdminButton = new Button();
             * isConfigAdminButton.setLabel(msgs.get("users.edit.configAdministrator"));
             * isConfigAdminButton.setValue("true"); isConfigAdminButton.setOnclick("mgnlDialogShiftCheckboxSwitch('" +
             * isConfigAdmin.getName() + "');"); isConfigAdmin.addOption(isConfigAdminButton);
             * tab.addSub(isConfigAdmin);
             */

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
            return dialog;
        }

        /**
         *
         */
        private void save() {
            // create new user
            if (create) {
                String name = form.getParameter("name");
                path = "/" + name;
                try {
                    user = hm.createPage("/", name);
                }
                catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                }
            }

            // ######################
            // # write to .node.xml (controls with saveInfo (full name,
            // password))
            // ######################
            Save nodeXml = new Save(form, request);
            nodeXml.setPath(path);
            nodeXml.save();

            // ######################
            // # write users and roles acl
            // ######################

            // remove existing
            for (int i = 0; i < ContentRepository.ALL_REPOSITORIES.length; i++) {
                String repository = ContentRepository.ALL_REPOSITORIES[i];
                try {
                    user.delete("acl_" + repository);
                }
                catch (RepositoryException re) {
                }
            }

            // rewrite
            try {

                Content aclUsers;

                aclUsers = user.createContent(NODE_ACLUSERS, ItemType.CONTENTNODE);

                Content aclRoles = user.createContent(NODE_ACLROLES, ItemType.CONTENTNODE);
                Content aclConfig = user.createContent(NODE_ACLCONFIG, ItemType.CONTENTNODE);
                Save save = nodeXml;
                Content u0 = aclUsers.createContent("00", ItemType.CONTENTNODE);
                u0.createNodeData("path", save.getValue(user.getHandle() + "/" + NODE_ROLES), PropertyType.STRING);
                u0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                Content u1 = aclUsers.createContent("01", ItemType.CONTENTNODE);
                u1.createNodeData(
                    "path",
                    save.getValue(user.getHandle() + "/" + NODE_ROLES + "/*"),
                    PropertyType.STRING);
                u1.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                Content u2 = aclUsers.createContent("02", ItemType.CONTENTNODE);
                u2.createNodeData("path", save.getValue(user.getHandle()), PropertyType.STRING);
                u2.createNodeData("permissions", save.getValue(PERMISSION_ALL), PropertyType.LONG);

                Content u3 = aclUsers.createContent("03", ItemType.CONTENTNODE);
                u3.createNodeData("path", save.getValue(user.getHandle() + "/*"), PropertyType.STRING);
                u3.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                // read access to all roles
                Content r0 = aclRoles.createContent("0", ItemType.CONTENTNODE);
                r0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                r0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);
                // read access to config repository
                Content c0 = aclConfig.createContent("0", ItemType.CONTENTNODE);
                c0.createNodeData("path", save.getValue("/*"), PropertyType.STRING);
                c0.createNodeData("permissions", save.getValue(PERMISSION_READ), PropertyType.LONG);

                // ######################
                // # roles acl
                // ######################
                // remove existing
                try {
                    user.delete(NODE_ROLES);
                }
                catch (RepositoryException re) {
                }

                // rewrite
                Content roles = user.createContent(NODE_ROLES, ItemType.CONTENTNODE);

                String[] rolesValue = form.getParameter("aclList").split(";");

                for (int i = 0; i < rolesValue.length; i++) {
                    try {
                        String newLabel = Path.getUniqueLabel(hm, roles.getHandle(), "0");
                        Content r = roles.createContent(newLabel, ItemType.CONTENTNODE);
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
        }
    }

    public void draw(HttpServletRequest request, HttpServletResponse response) throws IOException, RepositoryException {

        new MyDialog(request, response).execute();
    }
}