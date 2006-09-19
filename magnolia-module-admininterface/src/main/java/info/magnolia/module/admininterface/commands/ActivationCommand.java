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
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the activation command which do real activation
 * @author jackie
 */
public class ActivationCommand extends BaseActivationCommand {

    /**
     * Log
     */
    private static Logger log = LoggerFactory.getLogger(ActivationCommand.class);

    private boolean recursive;

    /**
     * Execute the activation
     */
    public boolean execute(Context ctx) {
        if (log.isDebugEnabled()) {
            log.debug("recursive = " + recursive);
            log.debug("user = " + ctx.getUser().getName());
        }

        try {
            String parentPath = StringUtils.substringBeforeLast(getPath(), "/");
            if (StringUtils.isEmpty(parentPath)) {
                parentPath = "/";
            }
            // make multiple activations instead of a big bulp
            if (recursive) {
                activateRecursive(parentPath, getPath());
            }
            else {
                getSyndicator().activate(parentPath, getPath());
            }
        }
        catch (Exception e) {
            AlertUtil.setException(MessagesManager.get("tree.error.deactivate"), e, ctx);
            return false;
        }
        log.info("exec successfully.");
        return true;
    }

    /**
     * Activate recursively. This is done one by one to send only small peaces (memory friendly).
     * @param parentPath
     * @param path
     * @throws ExchangeException
     * @throws RepositoryException
     */
    protected void activateRecursive(String parentPath, String path) throws ExchangeException, RepositoryException {
        // activate this node using the rules
        getSyndicator().activate(parentPath, path);

        // proceed recursively
        Content node = MgnlContext.getHierarchyManager(this.getRepository()).getContent(path);

        Iterator children = node.getChildren(new Content.ContentFilter() {

            public boolean accept(Content content) {
                try {
                    return !getRule().isAllowed(content.getNodeTypeName());
                }
                catch (RepositoryException e) {
                    log.error("can't get nodetype", e);
                    return false;
                }
            }
        }).iterator();

        while (children.hasNext()) {
            this.activateRecursive(path, ((Content) children.next()).getHandle());
        }
    }

    /**
     * @return the recursive
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * @param recursive the recursive to set
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

}
