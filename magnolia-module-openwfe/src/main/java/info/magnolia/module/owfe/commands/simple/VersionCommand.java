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
package info.magnolia.module.owfe.commands.simple;

import java.util.HashMap;

import org.apache.commons.chain.Context;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.module.owfe.MgnlConstants;


/**
 * Creates a version for the passed path in the website repository.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class VersionCommand extends MgnlCommand{

    /**
     * @see info.magnolia.cms.beans.commands.MgnlCommand#getExpectedParameters()
     */
    public String[] getExpectedParameters() {
        return new String[]{MgnlConstants.P_PATH};
    }

    /**
     * @see info.magnolia.cms.beans.commands.MgnlCommand#exec(java.util.HashMap, org.apache.commons.chain.Context)
     */
    public boolean exec(HashMap param, Context ctx) {
        String path = (String) param.get(MgnlConstants.P_PATH);
        try{
            Content node = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(path);
            node.addVersion();
        }
        catch(Exception e){
            AlertUtil.setMessage("can't version: " + e.getMessage());
            return false;
        }
        return true;
    }

}
