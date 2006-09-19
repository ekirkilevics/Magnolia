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
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Iterator;


/**
 * Creates a version for the passed path in the website repository.
 * @author Philipp Bracher
 * @version $Id$
 */
public class VersionCommand extends RuleBasedCommand {

    private static Logger log = LoggerFactory.getLogger(VersionCommand.class);

    private boolean recursive;

    /**
     * @see info.magnolia.commands.MgnlCommand#execute(org.apache.commons.chain.Context) 
     */
    public boolean execute(Context ctx) {
        try {
            Content node
                    = MgnlContext.getSystemContext().getHierarchyManager(this.getRepository()).getContent(this.getPath());
            if (isRecursive()) {
                versionRecursively(node);
            } else {
                node.addVersion(getRule());
            }
        }
        catch (Exception e) {
            log.error("can't version", e);
            AlertUtil.setMessage("can't version: " + e.getMessage(), ctx);
            return false;
        }
        return true;
    }

    private void versionRecursively(Content node) throws RepositoryException {
        node.addVersion(getRule());
        Content.ContentFilter filter = new Content.ContentFilter() {
            public boolean accept(Content content) {
                try {
                    return !getRule().isAllowed(content.getNodeTypeName());
                }
                catch (RepositoryException e) {
                    log.error("can't get nodetype", e);
                    return false;
                }
            }
        };

        Iterator children = node.getChildren(filter).iterator();

        while (children.hasNext()) {
            versionRecursively((Content) children.next());
        }

    }

    /**
     * @return is recursive versioning
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * @param recursive
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

}
