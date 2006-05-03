/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.module.owfe.MgnlConstants;

import java.util.HashMap;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;


/**
 * the command to delete one node
 * @author jackie
 */
public class DeleteCommand extends MgnlCommand {

    static final String[] expectedParameters = {MgnlConstants.P_PATH};

    /**
     * List of the parameters that this command needs to run
     * @return a list of string describing the parameters needed. The parameters should have a mapping in this class.
     */
    public String[] getExpectedParameters() {
        return expectedParameters;
    }

    public boolean exec(HashMap params, Context ctx) {
        String path = (String) params.get(MgnlConstants.P_PATH);
        try {
            deleteNode(ctx, path);
        }
        catch (Exception e) {
            log.error("cannot do delete", e);
            return false;
        }
        return true;
    }

    private void deleteNode(Context context, String parentPath, String label) throws RepositoryException {
        Content parentNode = MgnlContext.getHierarchyManager(MgnlConstants.WEBSITE_REPOSITORY).getContent(parentPath);
        String path;
        if (!parentPath.equals("/")) {
            path = parentPath + "/" + label;
        }
        else {
            path = "/" + label;
        }
        ((HashMap) context.get(PARAMS)).put(MgnlConstants.P_PATH, path);
        new DeactivationCommand().execute(context);
        parentNode.delete(label);
        parentNode.save();
    }

    private void deleteNode(Context context, String path) throws Exception {
        String parentPath = StringUtils.substringBeforeLast(path, "/");
        String label = StringUtils.substringAfterLast(path, "/");
        deleteNode(context, parentPath, label);
    }

}
