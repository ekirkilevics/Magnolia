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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.admininterface.DialogHandlerManager;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.TreeHandlerManager;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;
import info.magnolia.module.data.GenericDataTree;
import info.magnolia.module.data.save.UUIDConversionSaveHandler;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;

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
        Content type = this.getStorageNode();
        try {
        	final String dialogName = configureDialog(type);
            configureTree(type, dialogName);

            Content node;
            // register the dialogs
            node = ContentUtil.getCaseInsensitive(type, "dialogs");
            if (node != null) {
                DialogHandlerManager.getInstance().register(node);
            }
			// register trees
            node = ContentUtil.getCaseInsensitive(type, "trees");
            if (node != null) {
                TreeHandlerManager.getInstance().register(node);
            }
            
			Content dataMenu = hm.getContent("/modules/adminInterface/config/menu/data");
			String subMenuName = type.getName();
			Content subMenu = dataMenu.getChildByName(subMenuName);
			if(subMenu == null){
				subMenu = dataMenu.createContent(subMenuName, ItemType.CONTENTNODE);
				NodeData label = subMenu.createNodeData("label");
				label.setValue(type.getTitle());
				NodeData icon = subMenu.createNodeData("icon");
				icon.setValue("/.resources/icons/16/dot.gif");
				NodeData onclick = subMenu.createNodeData("onclick");
				onclick.setValue("MgnlAdminCentral.showTree('"+subMenuName+"Tree')");
			} else {
				NodeData label = subMenu.getNodeData("label");
				label.setValue(type.getTitle());
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

	protected String configureDialog(Content type) throws RepositoryException {
		final String dialogName = type.getName()+"Dialog";
		if(!type.hasContent("dialogs")) {
			final Content dialog = type.createContent("dialogs").createContent(dialogName);
			//here add the dialog fields
			dialog.createNodeData("class").setValue(DataDialog.class.getName());
			dialog.createNodeData("saveHandler").setValue(UUIDConversionSaveHandler.class.getName());
			dialog.createNodeData("name").setValue(dialogName);
			dialog.createNodeData("i18nBasename").setValue("info.magnolia.module.data.messages");
			dialog.createNodeData("itemType").setValue(type.getName());
			ContentRepository.getRepositoryProvider(getRepository()).registerNodeTypes(new ByteArrayInputStream(NODE_TYPE_DEF_TEMPLATE.format(new String[]{type.getName()}).getBytes()));
			type.save();
		} else {
			// update dialog fields
		}
		return dialogName;
	}

	protected void configureTree(Content type, String dialogName) throws AccessDeniedException, PathNotFoundException, RepositoryException {
		if(type.getChildByName("trees") == null){
			Content trees = type.createContent("trees", ItemType.CONTENT);
			String treeName = type.getName()+"Tree";
			Content tree = trees.createContent(treeName, ItemType.CONTENTNODE);
			NodeData clazz = tree.createNodeData("class");
			clazz.setValue(GenericDataTree.class.getName());
			NodeData name = tree.createNodeData("name");
			name.setValue(treeName);
			NodeData repository = tree.createNodeData("repository");
			repository.setValue("data");
			tree.createNodeData("dialogName").setValue(dialogName);
			type.save();
		}
	}

	private static final String NODE_TYPE_DEFINITION = 

		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ 	"<nodeTypes"
		+ 		" xmlns:rep=\"internal\""
		+ 		" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\""
		+ 		" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\""
		+ 		" xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\""
		+ 		" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">"
		+		"<nodeType name=\"{0}\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
		+			"<supertypes>"
		+				"<supertype>nt:hierarchyNode</supertype>"
		+			"</supertypes>"
		+			"<childNodeDefinition name=\"MetaData\" defaultPrimaryType=\"mgnl:metaData\" autoCreated=\"true\" mandatory=\"true\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"false\">"
		+				"<requiredPrimaryTypes>"
		+					"<requiredPrimaryType>mgnl:metaData</requiredPrimaryType>"
		+				"</requiredPrimaryTypes>"
		+			"</childNodeDefinition>"
		+			"<childNodeDefinition name=\"*\" defaultPrimaryType=\"\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"true\">"
		+				"<requiredPrimaryTypes>"
		+					"<requiredPrimaryType>nt:base</requiredPrimaryType>"
		+				"</requiredPrimaryTypes>"
		+			"</childNodeDefinition>"
		+			"<propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\"/>"
		+		"</nodeType>"
		+	"</nodeTypes>";
	
	private static final MessageFormat NODE_TYPE_DEF_TEMPLATE = new MessageFormat(NODE_TYPE_DEFINITION);
}
