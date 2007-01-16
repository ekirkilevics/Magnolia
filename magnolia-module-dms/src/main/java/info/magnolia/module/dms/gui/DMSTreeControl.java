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
package info.magnolia.module.dms.gui;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.module.dms.beans.Document;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Special tree control to handle the contentNodes (documents)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSTreeControl extends Tree {

    private static Logger log = Logger.getLogger(DMSTreeControl.class);

    /**
     * @deprecated
     */
    HttpServletResponse response;
    
    /**
     * @deprecated don't pass the response
     * @param repository
     * @param request
     */
    public DMSTreeControl(String repository, HttpServletRequest request, HttpServletResponse response) {
        super(repository, repository, request);
        this.response = response;
    }
    
    public DMSTreeControl(String name, String repository, HttpServletRequest request) {
        super(name, repository, request);
    }

    protected String getIcon(Content node, NodeData nodedata, String itemType) {
        if (itemType.equals(ItemType.CONTENT.getSystemName())) {
            return super.getIcon(node, nodedata, itemType);
        }
        return (new Document(node)).getMimeTypeIcon();
    }

    protected boolean hasSub(Content c, String type) {
        try {
            if (c.getNodeType().getName().equals(ItemType.CONTENTNODE.getSystemName())) {
                return false;
            }
        }
        catch (RepositoryException e) {
            // should not happen;
        }
        return super.hasSub(c, type);
    }

}
