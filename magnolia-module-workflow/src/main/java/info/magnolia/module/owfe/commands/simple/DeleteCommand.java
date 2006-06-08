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

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the command to delete one node
 * @author jackie
 */
public class DeleteCommand extends MgnlCommand {

    private static Logger log = LoggerFactory.getLogger(DeleteCommand.class);
    
    public boolean execute(Context ctx) {
        String path = (String) ctx.get(Context.ATTRIBUTE_PATH);
        try {
            deleteNode(ctx, path);
        }
        catch (Exception e) {
            log.error("cannot do delete", e);
            return true;
        }
        return false;
    }

    private void deleteNode(Context context, String parentPath, String label) throws RepositoryException {
        Content parentNode = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(parentPath);
        String path;
        if (!parentPath.equals("/")) {
            path = parentPath + "/" + label;
        }
        else {
            path = "/" + label;
        }
        context.put(Context.ATTRIBUTE_PATH, path);
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
