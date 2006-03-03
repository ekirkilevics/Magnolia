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
import info.magnolia.cms.security.Permission;
import info.magnolia.module.admininterface.SaveHandler;

import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class GroupAssignRolesDialog extends ConfiguredDialog{

	  /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(UserEditDialog.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private static final String NODE_ACLGROUPS = "acl_groups"; //$NON-NLS-1$

 //   private static final String NODE_ACLROLES = "acl_userroles"; //$NON-NLS-1$
    
    private static final String NODE_GROUPROLES = "acl_grouproles"; //$NON-NLS-1$

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
    public GroupAssignRolesDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
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

        dialog.setLabel("Assign roles for Group"); //$NON-NLS-1$

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

        //  I have to put the edit control here, otherewise, there is no 
        //  <input type="hidden" name="mgnlSaveInfo"../> on the page, which will cause the 
        // getForm().getParameterValues("mgnlSaveInfo") return null in SaveHandlerImpl.save() on on line 129.
        // and throw null pointer exception on in SaveHandlerImpl.save() on on line 201
        // for (int i = 0; i < saveInfos.length; i++) {...
        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title"); //$NON-NLS-1$
        title.setLabel("Full name"); //$NON-NLS-1$
        tab.addSub(title);
        tab.addSub(spacer);
        
        DialogInclude roles = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
        roles.setLabel("roles"); //$NON-NLS-1$
        roles.setName("aclRolesRepository"); //$NON-NLS-1$
        roles.setConfig("file", "/.magnolia/dialogpages/groupEditRolesInclude.html"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(roles);

        DialogButton addRole = DialogFactory.getDialogButtonInstance(request, response, null, null);
        addRole.setConfig("buttonLabel", "add"); //$NON-NLS-1$ //$NON-NLS-2$
        addRole.setConfig("lineSemi", true); //$NON-NLS-1$
        addRole.setConfig("onclick", "mgnlAclAdd(true,-1);"); //$NON-NLS-1$ //$NON-NLS-2$
        tab.addSub(addRole);
        
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
    
    protected void onPostSave(SaveHandler saveControl) {
        Content group = this.getStorageNode();

        // ######################
        // # write users and roles acl
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

            Content aclGroups;

            // create node "acl_groups" under node "group"
            aclGroups = group.createContent(NODE_ACLGROUPS, ItemType.CONTENTNODE);

            group.createContent(NODE_GROUPROLES, ItemType.CONTENTNODE);
            group.createContent(NODE_ACLCONFIG, ItemType.CONTENTNODE);

            // give user permission to read and edit himself
            // create node "0" under acl_groups
            Content u3 = aclGroups.createContent("0", ItemType.CONTENTNODE); //$NON-NLS-1$
            u3.createNodeData("path").setValue(group.getHandle() + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
            u3.createNodeData("permissions").setValue(Permission.ALL); //$NON-NLS-1$

            // ######################
            // # roles acl
            // ######################
            // remove existing
            try {
            	group.delete(NODE_ROLES);
            }
            catch (RepositoryException re) {
                // roles node did not exist yet
            }

            // rewrite
            // create node "roles" under node group
            Content roles = group.createContent(NODE_ROLES, ItemType.CONTENTNODE);

            String[] rolesValue = form.getParameter("aclList").split(";"); //$NON-NLS-1$ //$NON-NLS-2$

            for (int i = 0; i < rolesValue.length; i++) {
                try {
                	// create node <new label> with atttribute path="<role>" under node "groups"
                    String newLabel = Path.getUniqueLabel(hm, roles.getHandle(), "0"); //$NON-NLS-1$
                    Content r = roles.createContent(newLabel, ItemType.CONTENTNODE);
                    r.createNodeData("path").setValue(rolesValue[i]); //$NON-NLS-1$
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            hm.save();
//            try{
//            File outputFile = new File("d:\\export.xml");
//				FileOutputStream out = new FileOutputStream(outputFile);
//				hm.getWorkspace().getSession().exportSystemView("/", out,
//						false, false);
//			}
//            catch(Exception e)
//            {
//            	log.error(e);
//            }

        }
        catch (RepositoryException re) {
            log.error(re.getMessage(), re);
        }
    }



}
