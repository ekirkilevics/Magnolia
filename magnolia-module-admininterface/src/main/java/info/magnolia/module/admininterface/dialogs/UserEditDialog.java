package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogMultiSelect;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.admininterface.SaveHandler;

import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UserEditDialog extends ConfiguredDialog {

    /**
     * Logger.
     */
    protected static Logger log = LoggerFactory.getLogger(UserEditDialog.class);

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

    /**
     * @see info.magnolia.module.admininterface.DialogMVCHandler#configureSaveHandler(info.magnolia.module.admininterface.SaveHandler)
     */
    protected void configureSaveHandler(SaveHandler save) {
        super.configureSaveHandler(save);
        save.setPath(path);
    }
    
    protected DialogDialog createDialog(Content configNode, Content storageNode) throws RepositoryException {
        DialogDialog dialog = super.createDialog(configNode, storageNode);
        
        Content node = this.getStorageNode();
        if(node!= null){
            DialogMultiSelect roles = (DialogMultiSelect)dialog.getSub("roles");
            List rolesValues = roles.getValues();
            if(node.hasContent(NODE_ROLES)){
                Content rolesNode = node.getContent(NODE_ROLES);
                for (Iterator iter = rolesNode.getChildren().iterator(); iter.hasNext();) {
                    Content roleNode = (Content) iter.next();
                    String path =  NodeDataUtil.getString(roleNode, "path");
                    rolesValues.add(path);
                }
            }
        }
        return dialog;
    }

    protected boolean onPostSave(SaveHandler saveControl) {
        Content user = this.getStorageNode();

        // ######################
        // # write users and roles acl
        // ######################

        // remove existing
        Iterator repositoryNames = ContentRepository.getAllRepositoryNames();
        while (repositoryNames.hasNext()) {
            String repository = (String) repositoryNames.next();
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

            String[] rolesValue = form.getParameterValues("roles"); //$NON-NLS-1$ //$NON-NLS-2$

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
        return true;
    }

}