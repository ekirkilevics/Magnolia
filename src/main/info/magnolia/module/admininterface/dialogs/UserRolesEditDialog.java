package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.gui.control.Save;
import info.magnolia.cms.gui.dialog.DialogBox;
import info.magnolia.cms.gui.dialog.DialogDialog;
import info.magnolia.cms.gui.dialog.DialogEdit;
import info.magnolia.cms.gui.dialog.DialogFactory;
import info.magnolia.cms.gui.dialog.DialogInclude;
import info.magnolia.cms.gui.dialog.DialogStatic;
import info.magnolia.cms.gui.dialog.DialogTab;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id$
 */
public class UserRolesEditDialog extends ConfiguredDialog {

    protected static Logger log = Logger.getLogger("roles dialog"); //$NON-NLS-1$

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public UserRolesEditDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    public String getRepository() {
        String repository = super.getRepository();
        if (repository == null) {
            repository = ContentRepository.USER_ROLES;
        }
        return repository;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
     * info.magnolia.cms.core.Content)
     */
    protected DialogDialog createDialog(Content configNode, Content storageNode) throws RepositoryException {

        DialogDialog dialog = super.createDialog(configNode, storageNode);

        dialog.setJavascriptSources(request.getContextPath() + "/admindocroot/js/dialogs/DynamicTable.js"); //$NON-NLS-1$
        dialog.setJavascriptSources(request.getContextPath()
            + "/admindocroot/js/dialogs/pages/userRolesEditDialogPage.js"); //$NON-NLS-1$
        dialog.setCssSources(request.getContextPath() + "/admindocroot/css/dialogs/pages/userRolesEditDialogPage.css"); //$NON-NLS-1$
        dialog.setConfig("height", 600); //$NON-NLS-1$

        dialog.setLabel(msgs.get("roles.edit.edit")); //$NON-NLS-1$

        DialogTab tab0 = dialog.addTab(msgs.get("roles.edit.properties")); //$NON-NLS-1$

        DialogTab tab1 = dialog.addTab(msgs.get("roles.edit.accessControlList")); //$NON-NLS-1$

        DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
        spacer.setConfig("line", false); //$NON-NLS-1$

        DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
        // name.setConfig("line",false);
        name.setLabel("<strong>" + msgs.get("roles.edit.rolename") + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        name.setValue("<strong>" + storageNode.getName() + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
        tab0.addSub(name);

        tab0.addSub(spacer);

        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title"); //$NON-NLS-1$
        title.setLabel(msgs.get("roles.edit.fullname")); //$NON-NLS-1$
        tab0.addSub(title);

        tab0.addSub(spacer);

        DialogEdit desc = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        desc.setName("description"); //$NON-NLS-1$
        desc.setLabel(msgs.get("roles.edit.description")); //$NON-NLS-1$
        desc.setConfig("rows", 6); //$NON-NLS-1$
        tab0.addSub(desc);

        DialogInclude acl = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
        acl.setBoxType(DialogBox.BOXTYPE_1COL);
        acl.setName("aclRolesRepository"); //$NON-NLS-1$
        acl.setConfig("file", "/.magnolia/dialogpages/userRolesEditAclInclude.html"); //$NON-NLS-1$ //$NON-NLS-2$
        tab1.addSub(acl);

        dialog.setConfig("saveOnclick", "aclFormSubmit();"); //$NON-NLS-1$ //$NON-NLS-2$
        return dialog;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#onPreSave()
     */
    protected Save onPreSave() {
        Save control = super.onPreSave();
        control.setPath(path);
        return control;
    }

    protected void onPostSave(Save saveControl) {
        Content role = this.getStorageNode();

        // for each repository
        for (int x = 0; x < ContentRepository.getAllRepositoryNames().size(); x++) {
            String repository = (String) ContentRepository.getAllRepositoryNames().get(x);

            // ######################
            // # acl
            // ######################
            // remove existing
            try {
                role.delete("acl_" + repository); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                // ignore, not existing
            }
            // rewrite
            try {
                Content acl = role.createContent("acl_" + repository, ItemType.CONTENTNODE); //$NON-NLS-1$
                String aclValueStr = form.getParameter("acl" + repository + "List"); //$NON-NLS-1$ //$NON-NLS-2$
                if (StringUtils.isNotEmpty(aclValueStr)) {
                    String[] aclEntries = aclValueStr.split(";"); //$NON-NLS-1$
                    for (int i = 0; i < aclEntries.length; i++) {
                        String path = StringUtils.EMPTY;
                        long accessRight = 0;
                        String accessType = StringUtils.EMPTY;

                        String[] aclValuePairs = aclEntries[i].split(","); //$NON-NLS-1$
                        for (int j = 0; j < aclValuePairs.length; j++) {
                            String[] aclValuePair = aclValuePairs[j].split(":"); //$NON-NLS-1$
                            String aclName = aclValuePair[0].trim();
                            String aclValue = StringUtils.EMPTY;
                            if (aclValuePair.length > 1) {
                                aclValue = aclValuePair[1].trim();
                            }

                            if (aclName.equals("path")) { //$NON-NLS-1$
                                path = aclValue;
                            }
                            else if (aclName.equals("accessType")) { //$NON-NLS-1$
                                accessType = aclValue;
                            }
                            else if (aclName.equals("accessRight")) { //$NON-NLS-1$
                            		try{
                            			accessRight = Long.parseLong(aclValue);
                            		}
                            		catch(NumberFormatException e){
                            			accessRight = 0;
                            		}
                            }
                        }

                        if (StringUtils.isNotEmpty(path)) {
                            if (path.equals("/")) { //$NON-NLS-1$
                                // needs only one entry: "/*"
                                accessType = "sub"; //$NON-NLS-1$
                                path = StringUtils.EMPTY;
                            }

                            if (accessType.equals("self")) { //$NON-NLS-1$
                                try {
                                    String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0"); //$NON-NLS-1$
                                    Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                    r.createNodeData("path").setValue(path); //$NON-NLS-1$
                                    r.createNodeData("permissions").setValue(accessRight); //$NON-NLS-1$
                                }
                                catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                            try {
                                String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0"); //$NON-NLS-1$
                                Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                r.createNodeData("path").setValue(path + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
                                r.createNodeData("permissions").setValue(accessRight); //$NON-NLS-1$
                            }
                            catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                hm.save();
            }
            catch (RepositoryException re) {
                log.error(re.getMessage(), re);
            }
        }
    }

}