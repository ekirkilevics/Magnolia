/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.data.dialogs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Handles the document upload and adds some special properties (for searching)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class TypeDialog extends ConfiguredDialog {

    private static Logger log = Logger.getLogger(TypeDialog.class);

    private boolean create;

    private String version;

    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public TypeDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
        this.version = request.getParameter("mgnlVersion");
    }

    /**
     * Overriden to force creation if the node does not exist
     */
    protected boolean onPreSave(SaveHandler handler) {
        // check if this is a creation
        this.create = handler.getPath().endsWith("/mgnlNew");

        if(this.create){
            handler.setCreate(true);

            String path = StringUtils.substringBeforeLast(handler.getPath(), "/");

            String name = form.getParameter("title");
            name = Path.getValidatedLabel(name);
            if (name.matches("^-*$")) {
                name = "data";
            }

            name = Path.getUniqueLabel(hm, path, name);
            this.path = path + "/" + name;
            handler.setPath(this.path);
            handler.setCreationItemType(ItemType.CONTENT);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see info.magnolia.module.admininterface.DialogMVCHandler#onPostSave(info.magnolia.cms.gui.control.Save)
     */
    protected boolean onPostSave(SaveHandler handler) {
        super.onPostSave(handler);
        Content node = this.getStorageNode();
        try {
            configureTree(node);
			Content dataMenu = hm.getContent("/modules/adminInterface/config/menu/data");
			String subMenuName = node.getName();
			Content subMenu = dataMenu.getChildByName(subMenuName);
			if(subMenu == null){
				subMenu = dataMenu.createContent(subMenuName, ItemType.CONTENTNODE);
				NodeData label = subMenu.createNodeData("label");
				label.setValue(node.getTitle());
				NodeData icon = subMenu.createNodeData("icon");
				icon.setValue("/.resources/icons/16/dot.gif");
				NodeData onclick = subMenu.createNodeData("onclick");
				onclick.setValue("MgnlAdminCentral.showTree('"+subMenuName+"Tree')");
			} else {
				NodeData label = subMenu.getNodeData("label");
				label.setValue(node.getTitle());
			}
			dataMenu.save();
		} catch (AccessDeniedException e) {
			throw new RuntimeException(e);
		} catch (PathNotFoundException e) {
			throw new RuntimeException(e);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
        return true;
    }

	protected void configureTree(Content type) throws AccessDeniedException, PathNotFoundException, RepositoryException {
		if(type.getChildByName("trees") == null){
			Content trees = type.createContent("trees", ItemType.CONTENT);
			String treeName = type.getName()+"Tree";
			Content tree = trees.createContent(treeName, ItemType.CONTENTNODE);
			NodeData clazz = tree.createNodeData("class");
			clazz.setValue("generic tree class");
			NodeData name = tree.createNodeData("name");
			name.setValue(treeName);
			NodeData repository = tree.createNodeData("repository");
			repository.setValue("data");
			type.save();
		}
	}

}
