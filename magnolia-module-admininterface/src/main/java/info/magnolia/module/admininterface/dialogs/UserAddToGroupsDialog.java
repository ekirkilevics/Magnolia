package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
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
import info.magnolia.module.admininterface.SaveHandler;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Fabrizio Giustina
 * @version $Id: UserEditDialog.java 2192 2006-03-03 11:38:00Z philipp $
 */
public class UserAddToGroupsDialog extends ConfiguredDialog {

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
	 * 
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
	public UserAddToGroupsDialog(String name, HttpServletRequest request,
			HttpServletResponse response, Content configNode) {
		super(name, request, response, configNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.magnolia.module.admininterface.DialogMVCHandler#createDialog(info.magnolia.cms.core.Content,
	 *      info.magnolia.cms.core.Content)
	 */
	protected DialogDialog createDialog(Content configNode, Content storageNode)
			throws RepositoryException {
		DialogDialog dialog = super.createDialog(configNode, storageNode);
		dialog.setJavascriptSources("/admindocroot/js/dialogs/acl.js"); //$NON-NLS-1$

		// opener.document.location.reload();window.close();

		dialog.setConfig("width", DialogDialog.DIALOGSIZE_SLIM_WIDTH); //$NON-NLS-1$
		dialog.setConfig("height", DialogDialog.DIALOGSIZE_SLIM_HEIGHT); //$NON-NLS-1$

		dialog.setLabel("add user to groups"); //$NON-NLS-1$

		DialogTab tab = dialog.addTab();

		DialogStatic spacer = DialogFactory.getDialogStaticInstance(request,
				response, null, null);
		spacer.setConfig("line", false); //$NON-NLS-1$

		DialogStatic lineHalf = DialogFactory.getDialogStaticInstance(request,
				response, null, null);
		lineHalf.setConfig("line", false); //$NON-NLS-1$

		DialogStatic nameEdit = DialogFactory.getDialogStaticInstance(request,
				response, null, null);
		nameEdit
				.setLabel("<strong>" + msgs.get("users.edit.username") + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		nameEdit.setValue("<strong>" + storageNode.getName() + "</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
		tab.addSub(nameEdit);
		tab.addSub(spacer);
		tab.addSub(spacer);

		DialogInclude groups = DialogFactory.getDialogIncludeInstance(request,
				response, storageNode, null);
		groups.setLabel("Groups"); //$NON-NLS-1$
		groups.setName("aclRolesRepository"); //$NON-NLS-1$
		groups.setConfig(
				"file", "/.magnolia/dialogpages/userAddToGroupsInclude.html"); //$NON-NLS-1$ //$NON-NLS-2$
		tab.addSub(groups);

		DialogButton add = DialogFactory.getDialogButtonInstance(request,
				response, null, null);
		add.setConfig("buttonLabel", msgs.get("buttons.add")); //$NON-NLS-1$ //$NON-NLS-2$
		add.setConfig("lineSemi", true); //$NON-NLS-1$
		add.setConfig("onclick", "mgnlAclAdd(true,-1);"); //$NON-NLS-1$ //$NON-NLS-2$
		tab.addSub(add);

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
			} catch (RepositoryException re) {
				// new user
			}
		}

		// rewrite
		try {

			// recreate groups node
			Content groups = null;
			try{
				 groups = user.getContent("groups");
			}catch(Exception e){
				log.info("User does not have groups node", e);
			}
			if (groups != null)
				groups.delete();
			groups = user.createContent("groups");

			String[] groupsValue = form.getParameter("aclList").split(";"); //$NON-NLS-1$ //$NON-NLS-2$

			for (int i = 0; i < groupsValue.length; i++) {
				// create <groupid> under node "groups"
	            String newLabel = Path.getUniqueLabel(hm, groups.getHandle(), "0");
	            Content r = groups.createContent(newLabel, ItemType.CONTENTNODE);
	            r.createNodeData("path").setValue(groupsValue[i]);
	            log.info("added to group " + groupsValue[i]);
	        }
			
			hm.save();
			
			log.info("add user to group ok. repo = " + this.getRepository() + ", hm = " + hm.getWorkspace().getName());
			
		} catch (RepositoryException re) {
			log.error(re.getMessage(), re);
		}
	}

}