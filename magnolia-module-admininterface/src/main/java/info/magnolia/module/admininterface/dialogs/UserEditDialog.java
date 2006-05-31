package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.security.Permission;
import info.magnolia.module.admininterface.SaveHandler;

import java.util.Iterator;

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

            hm.save();
        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
        return true;
    }

}