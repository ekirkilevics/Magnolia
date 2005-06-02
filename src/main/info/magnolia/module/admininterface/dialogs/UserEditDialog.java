package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
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
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserEditDialog extends ConfiguredDialog {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(UserEditDialog.class);

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

    private static final String NODE_ACLCONFIG = "acl_config";

    private static final String CONTROLNAME_ISADMIN_CONFIG = "permissionConfig";

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#getRepository()
     */
    public String getRepository() {
        String repository = super.getRepository();
        if (repository == null) {
            repository = ContentRepository.USERS;
        }
        return repository;
    }

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public UserEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected DialogDialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        DialogDialog dialog = super.createDialog(configNode, storageNode);
        dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js");

        // opener.document.location.reload();window.close();

        dialog.setConfig("width", DialogDialog.DIALOGSIZE_SLIM_WIDTH);
        dialog.setConfig("height", DialogDialog.DIALOGSIZE_SLIM_HEIGHT);

        dialog.setLabel(msgs.get("users.edit.edit"));

        DialogTab tab = dialog.addTab();

        DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
        spacer.setConfig("line", false);

        DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request, response, null, null);
        lineHalf.setConfig("line", false);

        DialogStatic nameEdit = DialogFactory.getDialogStaticInstance(request, response, null, null);
        nameEdit.setLabel("<strong>" + msgs.get("users.edit.username") + "</strong>");
        nameEdit.setValue("<strong>" + storageNode.getName() + "</strong>");
        tab.addSub(nameEdit);
        tab.addSub(spacer);

        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title");
        title.setLabel(msgs.get("users.edit.fullname"));

        tab.addSub(title);

        DialogPassword pswd = DialogFactory.getDialogPasswordInstance(request, response, storageNode, null);
        pswd.setName("pswd");
        pswd.setLabel(msgs.get("users.edit.password"));
        tab.addSub(pswd);

        tab.addSub(spacer);

        // select language
        DialogSelect langSelect = DialogFactory.getDialogSelectInstance(request, response, storageNode, null);
        langSelect.setName("language");
        langSelect.setLabel(msgs.get("users.edit.language"));
        List options = new ArrayList();

        Collection col = MessagesManager.getAvailableLocales();

        for (Iterator iter = col.iterator(); iter.hasNext();) {
            Locale locale = (Locale) iter.next();
            String code = locale.getLanguage();
            String name = locale.getDisplayName(MessagesManager.getDefaultLocale());
            SelectOption option = new SelectOption(name, code);
            options.add(option);
        }
        langSelect.setOptions(options);
        tab.addSub(langSelect);
        tab.addSub(spacer);

        tab.addSub(spacer);

        DialogInclude roles = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
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

    protected Save onPreSave() {
        Save control = super.onPreSave();
        control.setPath(path);
        return control;
    }

    protected void onPostSave(Save saveControl) {
        Content user = this.getStorageNode();

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

            Content u0 = aclUsers.createContent("00", ItemType.CONTENTNODE);
            u0.createNodeData("path", saveControl.getValue(user.getHandle() + "/" + NODE_ROLES), PropertyType.STRING);
            u0.createNodeData("permissions", saveControl.getValue(PERMISSION_READ), PropertyType.LONG);

            Content u1 = aclUsers.createContent("01", ItemType.CONTENTNODE);
            u1.createNodeData(
                "path",
                saveControl.getValue(user.getHandle() + "/" + NODE_ROLES + "/*"),
                PropertyType.STRING);
            u1.createNodeData("permissions", saveControl.getValue(PERMISSION_READ), PropertyType.LONG);

            Content u2 = aclUsers.createContent("02", ItemType.CONTENTNODE);
            u2.createNodeData("path", saveControl.getValue(user.getHandle()), PropertyType.STRING);
            u2.createNodeData("permissions", saveControl.getValue(PERMISSION_ALL), PropertyType.LONG);

            Content u3 = aclUsers.createContent("03", ItemType.CONTENTNODE);
            u3.createNodeData("path", saveControl.getValue(user.getHandle() + "/*"), PropertyType.STRING);
            u3.createNodeData("permissions", saveControl.getValue(PERMISSION_READ), PropertyType.LONG);

            // read access to all roles
            Content r0 = aclRoles.createContent("0", ItemType.CONTENTNODE);
            r0.createNodeData("path", saveControl.getValue("/*"), PropertyType.STRING);
            r0.createNodeData("permissions", saveControl.getValue(PERMISSION_READ), PropertyType.LONG);
            // read access to config repository
            Content c0 = aclConfig.createContent("0", ItemType.CONTENTNODE);
            c0.createNodeData("path", saveControl.getValue("/*"), PropertyType.STRING);
            c0.createNodeData("permissions", saveControl.getValue(PERMISSION_READ), PropertyType.LONG);

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