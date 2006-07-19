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
import info.magnolia.cms.core.Path;
import info.magnolia.module.admininterface.SaveHandler;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * Handles the document upload and adds some special properties (for searching)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DataDialog extends ConfiguredDialog {

    private boolean create;


    /**
     * @param name
     * @param request
     * @param response
     * @param configNode
     */
    public DataDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
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

        	final String itemType;
			try {
				itemType = getConfigNode().getParent().getParent().getName();
			} catch (Exception e) {
				throw new RuntimeException("cannot get the type of the data item to create.", e);
			}

            String name = form.getParameter("title");
            if(name == null || name.trim().length() < 1){
            	name = itemType;
            }
            name = Path.getValidatedLabel(name);

            name = Path.getUniqueLabel(hm, path, name);
            this.path = path + "/" + name;
            handler.setPath(this.path);
        	handler.setCreationItemType(new ItemType(itemType));
        }
        return true;
    }
}
