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
package info.magnolia.module.data.gui;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.Tree;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * Special tree control to handle the contentNodes (documents)
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DataTreeControl extends Tree {

    Logger log = Logger.getLogger(DataTreeControl.class);

    /**
     * @param repository
     * @param request
     */
    public DataTreeControl(String repository, HttpServletRequest request, HttpServletResponse response) {
        super(repository, repository, request);
        setIndentionWidth(0);  
    }

    protected String getIcon(Content node, NodeData nodedata, String itemType) {
    	return super.getIcon(node, nodedata, itemType);
    }

    public String getPath() {
    	return "/modules/data/config/types";
    }
    
    protected boolean hasSub(Content c, String type) {
        try {
			return c.getLevel() < 5;
		} catch (PathNotFoundException e) {
			throw new RuntimeException(e);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
    }

}
