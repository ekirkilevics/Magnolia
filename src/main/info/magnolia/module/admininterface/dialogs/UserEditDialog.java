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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id$
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

    private static final String NODE_ACLUSERS = "acl_users"; //$NON-NLS-1$

    private static final String NODE_ACLROLES = "acl_userroles"; //$NON-NLS-1$

    private static final String NODE_ROLES = "roles"; //$NON-NLS-1$

    private static final String NODE_ACLCONFIG = "acl_config"; //$NON-NLS-1$

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
        dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js"); //$NON-NLS-1$

        // opener.document.location.reload();window.close();

        dialog.setConfig("width", DialogDialog.DIALOGSIZE_SLIM_WIDTH); //$NON-NLS-1$
        dialog.setConfig("height", DialogDialog.DIALOGSIZE_SLIM_HEIGHT); //$NON-NLS-1$

        dialog.setLabel(msgs.get("users.edit.edit")); //$NON-NLS-1$

        DialogTab tab = dialog.addTab();

        DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
        spacer.setConfig("line", false); //$NON-NLS-1$

        DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request, response, null, null);
        lineHalf.setConfig("line", false); //$NON-NLS-1$

        DialogStatic nameEdit = DialogFactory.getDialogStaticInstance(request, response, null, null);
        nameEdit.setLabel("<strong>" + msgs.get("users.edit.username") + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        nameEdit.setValue("<strong>" + storageNode.getName() + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(nameEdit);
        tab.addSub(spacer);

        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title"); //$NON-NLS-1$
        title.setLabel(msgs.get("users.edit.fullname")); //$NON-NLS-1$

        tab.addSub(title);

        DialogPassword pswd = DialogFactory.getDialogPasswordInstance(request, response, storageNode, null);
        pswd.setName("pswd"); //$NON-NLS-1$
        pswd.setLabel(msgs.get("users.edit.password")); //$NON-NLS-1$
        tab.addSub(pswd);

        tab.addSub(spacer);

        // select language
        DialogSelect langSelect = DialogFactory.getDialogSelectInstance(request, response, storageNode, null);
        langSelect.setName("language"); //$NON-NLS-1$
        langSelect.setLabel(msgs.get("users.edit.language")); //$NON-NLS-1$
        List options = new ArrayList();

        Collection col = MessagesManager.getAvailableLocales();

        for (Iterator iter = col.iterator(); iter.hasNext();) {
            Locale locale = (Locale) iter.next();
            String code = locale.getLanguage();
            if (StringUtils.isNotEmpty(locale.getCountry())) {
                code += "_" + locale.getCountry(); //$NON-NLS-1$
            }
            String name = locale.getDisplayName(MessagesManager.getCurrentLocale(request));
            SelectOption option = new SelectOption(name, code);
            options.add(option);
        }

        // sort them
        Collections.sort(options, new Comparator() {

            public int compare(Object arg0, Object arg1) {
                try {
                    String name0 = ((SelectOption) arg0).getLabel();
                    String name1 = ((SelectOption) arg1).getLabel();
                    return name0.compareTo(name1);
                }
                catch (Exception e) {
                    return 0;
                }
            }
        });

        langSelect.setOptions(options);
        tab.addSub(langSelect);
        tab.addSub(spacer);

        tab.addSub(spacer);

        DialogInclude roles = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
        roles.setLabel(msgs.get("users.edit.roles")); //$NON-NLS-1$
        roles.setName("aclRolesRepository"); //$NON-NLS-1$
        roles.setConfig("file", "/.magnolia/dialogpages/usersEditRolesInclude.html"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(roles);

        DialogButton add = DialogFactory.getDialogButtonInstance(request, response, null, null);
        add.setConfig("buttonLabel", msgs.get("buttons.add")); //$NON-NLS-1$ //$NON-NLS-2$
        add.setConfig("lineSemi", true); //$NON-NLS-1$
        add.setConfig("onclick", "mgnlAclAdd(true,-1);"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(add);

        dialog.setConfig("saveOnclick", "mgnlAclFormSubmit(true);"); //$NON-NLS-1$ //$NON-NLS-2$
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
        for (int i = 0; i < ContentRepository.getAllRepositoryNames().length; i++) {
            String repository = ContentRepository.getAllRepositoryNames()[i];
            try {
                user.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // new user
            }
        }

        // rewrite
        try {

            Content aclUsers;

            aclUsers = user.createContent(NODE_ACLUSERS, ItemType.CONTENTNODE);

            user.createContent(NODE_ACLROLES, ItemType.CONTENTNODE);
            user.createContent(NODE_ACLCONFIG, ItemType.CONTENTNODE);

            // give user permission to read and edit himself
            Content u3 = aclUsers.createContent("0", ItemType.CONTENTNODE); //$NON-NLS-1$
            u3.createNodeData("path").setValue(user.getHandle() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
            u3.createNodeData("permissions").setValue(Permission.ALL); //$NON-NLS-1$

            // ######################
            // # roles acl
            // ######################
            // remove existing
            try {
                user.delete(NODE_ROLES);
            }
            catch (RepositoryException re) {
                // roles node did not exist yet
            }

            // rewrite
            Content roles = user.createContent(NODE_ROLES, ItemType.CONTENTNODE);

            String[] rolesValue = form.getParameter("aclList").split(";"); //$NON-NLS-1$ //$NON-NLS-2$

            for (int i = 0; i < rolesValue.length; i++) {
                try {
                    String newLabel = Path.getUniqueLabel(hm, roles.getHandle(), "0"); //$NON-NLS-1$
                    Content r = roles.createContent(newLabel, ItemType.CONTENTNODE);
                    r.createNodeData("path").setValue(rolesValue[i]); //$NON-NLS-1$
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