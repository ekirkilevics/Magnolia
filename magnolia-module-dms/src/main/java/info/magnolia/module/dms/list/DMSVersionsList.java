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
package info.magnolia.module.dms.list;

import info.magnolia.module.admininterface.lists.VersionsList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Different on show js script than the default implementation.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class DMSVersionsList extends VersionsList {

    /**
     * @param name
     * @param request
     * @param response
     * @throws Exception
     */
    public DMSVersionsList(String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
        super(name, request, response);
    }

    /**
     * @see info.magnolia.module.admininterface.lists.VersionsList#getOnShowFunction(java.lang.String)
     */
    public String getOnShowFunction() {
        return "function(versionLabel){mgnl.dms.DMS.showVersion('" + path + "', versionLabel);}";
    }

}
