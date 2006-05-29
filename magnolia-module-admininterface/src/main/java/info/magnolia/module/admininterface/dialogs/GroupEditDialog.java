package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.DialogButton;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;
import info.magnolia.module.admininterface.SaveHandler;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * @author jackie
 */
public class GroupEditDialog extends ConfiguredDialog {

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(UserEditDialog.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NODE_GROUPUSERS = "users"; //$NON-NLS-1$

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
    public GroupEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
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

        dialog.setLabel("Edit Group"); //$NON-NLS-1$

        DialogTab tab = dialog.addTab();

        DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
        spacer.setConfig("line", false); //$NON-NLS-1$

        DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request, response, null, null);
        lineHalf.setConfig("line", false); //$NON-NLS-1$

        DialogStatic nameEdit = DialogFactory.getDialogStaticInstance(request, response, null, null);
        nameEdit.setLabel("<strong>" + "Group Name" + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        nameEdit.setValue("<strong>" + storageNode.getName() + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(nameEdit);
        tab.addSub(spacer);

        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title"); //$NON-NLS-1$
        title.setLabel("full name"); //$NON-NLS-1$
        tab.addSub(title);
        tab.addSub(spacer);

        DialogEdit desc = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        desc.setName("desc"); //$NON-NLS-1$
        desc.setLabel("Description"); //$NON-NLS-1$
        tab.addSub(desc);
        tab.addSub(spacer);

        DialogInclude users = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
        users.setLabel("users"); //$NON-NLS-1$
        users.setName("aclRolesRepository"); //$NON-NLS-1$
        users.setConfig("file", "/.magnolia/pages/groupEditUsersInclude.html"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(users);

        DialogButton addUser = DialogFactory.getDialogButtonInstance(request, response, null, null);
        addUser.setConfig("buttonLabel", msgs.get("buttons.add")); //$NON-NLS-1$ //$NON-NLS-2$
        addUser.setConfig("lineSemi", true); //$NON-NLS-1$
        addUser.setConfig("onclick", "mgnlAclAdd(true,-1);"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(addUser);

        dialog.setConfig("saveOnclick", "mgnlAclFormSubmit(true);"); //$NON-NLS-1$ //$NON-NLS-2$
        return dialog;
    }

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#configureSaveHandler(info.magnolia.module.admininterface.SaveHandler)
     */
    protected void configureSaveHandler(SaveHandler save) {
        super.configureSaveHandler(save);
        save.setPath(path);
    }

    /* (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#onPostSave(info.magnolia.module.admininterface.SaveHandler)
     */
    protected boolean onPostSave(SaveHandler saveControl) {
        Content group = this.getStorageNode();

        // ######################
        // # write users and group relationships
        // ######################

        // remove existing
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
            try {
                group.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // new user
            }
        }

        // rewrite
        try {

            // ######################
            // # groups
            // ######################
            // remove existing
            try {
                // delete "users" node under "groups" node
                group.delete(NODE_GROUPUSERS);
            }
            catch (RepositoryException re) {
                // roles node did not exist yet
            }

            // rewrite
            // create "users" node under "group" node
            Content users = group.createContent(NODE_GROUPUSERS, ItemType.CONTENTNODE);

            String[] usersValue = form.getParameter("aclList").split(";"); //$NON-NLS-1$ //$NON-NLS-2$

            for (int i = 0; i < usersValue.length; i++) {
                try {
                    // create node "<newlabel>" with attribute paht=">user>" under node "users"
                    String newLabel = Path.getUniqueLabel(hm, users.getHandle(), "0"); //$NON-NLS-1$
                    Content r = users.createContent(newLabel, ItemType.CONTENTNODE);
                    r.createNodeData("path").setValue(usersValue[i]); //$NON-NLS-1$
                    // add gourp reference to user's repository
                    new JCRUserUtil().addGroupForUser(group.getJCRNode().getPath(), usersValue[i]);
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            hm.save();
            log.info("change group ok. repo = " + this.getRepository() + ", hm = " + hm.getWorkspace().getName());
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return true;
    }
}
