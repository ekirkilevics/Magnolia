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
import info.magnolia.cms.security.Permission;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Id: $
 */
public class UserRolesEditDialog extends ConfiguredDialog {

    protected static Logger log = Logger.getLogger("roles dialog");

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    // todo: permission global available somewhere
    private static final long PERMISSION_ALL = Permission.ALL;

    private static final long PERMISSION_READ = Permission.READ;

    private static final long PERMISSION_NO = 0;

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

        dialog.setJavascriptSources(request.getContextPath() + "/admindocroot/js/dialogs/DynamicTable.js");
        dialog.setJavascriptSources(request.getContextPath()
            + "/admindocroot/js/dialogs/pages/userRolesEditDialogPage.js");
        dialog.setCssSources(request.getContextPath() + "/admindocroot/css/dialogs/pages/userRolesEditDialogPage.css");
        dialog.setConfig("height", 600);

        dialog.setLabel(msgs.get("roles.edit.edit"));

        DialogTab tab0 = dialog.addTab(msgs.get("roles.edit.properties"));

        DialogTab tab1 = dialog.addTab(msgs.get("roles.edit.accessControlList"));

        DialogStatic spacer = DialogFactory.getDialogStaticInstance(request, response, null, null);
        spacer.setConfig("line", false);

        DialogStatic name = DialogFactory.getDialogStaticInstance(request, response, null, null);
        // name.setConfig("line",false);
        name.setLabel("<strong>" + msgs.get("roles.edit.rolename") + "</strong>");
        name.setValue("<strong>" + storageNode.getName() + "</strong>");
        tab0.addSub(name);

        tab0.addSub(spacer);

        DialogEdit title = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        title.setName("title");
        title.setLabel(msgs.get("roles.edit.fullname"));
        tab0.addSub(title);

        tab0.addSub(spacer);

        DialogEdit desc = DialogFactory.getDialogEditInstance(request, response, storageNode, null);
        desc.setName("description");
        desc.setLabel(msgs.get("roles.edit.description"));
        desc.setConfig("rows", 6);
        tab0.addSub(desc);

        DialogInclude acl = DialogFactory.getDialogIncludeInstance(request, response, storageNode, null);
        acl.setBoxType(DialogBox.BOXTYPE_1COL);
        acl.setName("aclRolesRepository");
        acl.setConfig("file", "/admintemplates/adminCentral/dialogs/userRolesEdit/includeAcl.jsp");
        tab1.addSub(acl);

        dialog.setConfig("saveOnclick", "aclFormSubmit();");
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
        for (int x = 0; x < ContentRepository.ALL_REPOSITORIES.length; x++) {
            String repository = ContentRepository.ALL_REPOSITORIES[x];

            // ######################
            // # acl
            // ######################
            // remove existing
            try {
                role.delete("acl_" + repository);
            }
            catch (RepositoryException re) {
            }
            // rewrite
            try {
                Content acl = role.createContent("acl_" + repository, ItemType.CONTENTNODE);
                String aclValueStr = form.getParameter("acl" + repository + "List");
                if (StringUtils.isNotEmpty(aclValueStr)) {
                    String[] aclEntries = aclValueStr.split(";");
                    for (int i = 0; i < aclEntries.length; i++) {
                        String path = "";
                        long accessRight = 0;
                        String accessType = "";

                        String[] aclValuePairs = aclEntries[i].split(",");
                        for (int j = 0; j < aclValuePairs.length; j++) {
                            String[] aclValuePair = aclValuePairs[j].split(":");
                            String aclName = aclValuePair[0].trim();
                            String aclValue = "";
                            if (aclValuePair.length > 1) {
                                aclValue = aclValuePair[1].trim();
                            }

                            if (aclName.equals("path")) {
                                path = aclValue;
                            }
                            else if (aclName.equals("accessType")) {
                                accessType = aclValue;
                            }
                            else if (aclName.equals("accessRight")) {
                                accessRight = Long.parseLong(aclValue);
                            }
                        }

                        if (StringUtils.isNotEmpty(path)) {
                            if (path.equals("/")) {
                                // needs only one entry: "/*"
                                accessType = "sub";
                                path = "";
                            }

                            if (accessType.equals("self")) {
                                try {
                                    String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                                    Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                    r.createNodeData("path").setValue(path);
                                    r.createNodeData("permissions").setValue(accessRight);
                                }
                                catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                }
                            }
                            try {
                                String newLabel = Path.getUniqueLabel(hm, acl.getHandle(), "0");
                                Content r = acl.createContent(newLabel, ItemType.CONTENTNODE);
                                r.createNodeData("path").setValue(path + "/*");
                                r.createNodeData("permissions").setValue(accessRight);
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