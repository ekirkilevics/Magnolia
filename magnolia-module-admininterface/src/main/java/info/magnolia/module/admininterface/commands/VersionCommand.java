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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;


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
            Content node;
            if (StringUtils.isNotEmpty(getUuid())) {
                node = ctx.getHierarchyManager(this.getRepository()).getContentByUUID(this.getUuid());
            } else {
                node = ctx.getHierarchyManager(this.getRepository()).getContent(this.getPath());
            }
            if (isRecursive()) {
                // set versionMap and version name for this node
                Map versionMap = new HashMap();
                versionRecursively(node, ctx, versionMap);
                ctx.setAttribute(Context.ATTRIBUTE_VERSION_MAP, versionMap, Context.LOCAL_SCOPE);
            } else {
                Version version = node.addVersion(getRule());
                ctx.setAttribute(Context.ATTRIBUTE_VERSION, version.getName(), Context.LOCAL_SCOPE);
            }
        }
        catch (Exception e) {
            log.error("can't version", e);
            AlertUtil.setMessage("can't version: " + e.getMessage(), ctx);
            return false;
        }
        return true;
    }

    private void versionRecursively(Content node, Context ctx, Map versionMap) throws RepositoryException {
        Version version = node.addVersion(getRule());
        versionMap.put(node.getUUID(), version.getName());
        if(StringUtils.isEmpty((String)ctx.getAttribute(Context.ATTRIBUTE_VERSION))){
            ctx.setAttribute(Context.ATTRIBUTE_VERSION, version.getName(), Context.LOCAL_SCOPE);
        }
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
            versionRecursively((Content) children.next(), ctx, versionMap);
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
