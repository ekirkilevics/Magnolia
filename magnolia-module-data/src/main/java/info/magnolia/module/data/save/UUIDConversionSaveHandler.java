/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.data.save;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.dialog.Dialog;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.DialogAwareSaveHandler;
import info.magnolia.module.admininterface.SaveHandlerImpl;
import info.magnolia.module.data.controls.UUIDConversionControl;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author pintopile (BeagleSoft GmbH)
 * @version $Revision$ ($Author$)
 * 
 */
public class UUIDConversionSaveHandler extends SaveHandlerImpl implements DialogAwareSaveHandler {

	protected Dialog dialog;
	
	public Dialog getDialog() {
		return dialog;
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}

	protected void processMultiple(Content node, String name, int type, String[] values) throws RepositoryException, PathNotFoundException, AccessDeniedException {
        // remove entire content node and (re-)write each
        try {
            node.delete(name);
        }
        catch (PathNotFoundException e) {
            if (log.isDebugEnabled())
                log.debug("Exception caught: " + e.getMessage(), e); //$NON-NLS-1$
        }
        if (values != null && values.length != 0) {
            Content multiNode = node.createContent(name, ItemType.CONTENTNODE);
            try {
                // MetaData.CREATION_DATE has private access; no method to delete it so far...
                multiNode.deleteNodeData("creationdate"); //$NON-NLS-1$
            }
            catch (RepositoryException re) {
                if (log.isDebugEnabled())
                    log.debug("Exception caught: " + re.getMessage(), re); //$NON-NLS-1$
            }
            for (int j = 0; j < values.length; j++) {
                String valueStr = values[j];
                if(StringUtils.isNotEmpty(valueStr)) {
                    if (dialog.getSub(name) instanceof UUIDConversionControl && type == PropertyType.STRING) {
                        try {
                            HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
                            Content referencedNode = hm.getContent(valueStr);

                            valueStr = referencedNode.getUUID();
                        }
                        catch (RepositoryException re) {
                            if (log.isDebugEnabled())
                                log.debug("Cannot retrieve the referenced node by UUID: " + valueStr, re);
                        }
                    }
                    Value value = this.getValue(valueStr, type);
                    multiNode.createNodeData(Integer.toString(j)).setValue(value);
                }
            }
        }
	}

}
