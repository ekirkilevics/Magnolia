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
package info.magnolia.module.admininterface.dialogs;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.dialog.DialogStatic;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class UserNameControl extends DialogStatic {

    /**
     * Use a bold label
     */
    public String getLabel() {
        return "<strong>" + this.getMessage(super.getLabel()) + "</strong>";
    }

    /**
     * Set the nodes name as value
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode)
        throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        if (websiteNode != null) {
            this.setValue(websiteNode.getName());
        }
    }
}
