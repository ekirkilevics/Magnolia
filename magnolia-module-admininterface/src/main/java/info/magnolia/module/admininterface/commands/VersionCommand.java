/**
 * This file Copyright (c) 2003-2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.context.Context;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Creates a version for the passed path in the website repository.
 * @author Philipp Bracher
 * @version $Id$
 */
public class VersionCommand extends RuleBasedCommand {

    private static Logger log = LoggerFactory.getLogger(VersionCommand.class);

    private boolean recursive;

    private String comment;

    /**
     * @see info.magnolia.commands.MgnlCommand#execute(org.apache.commons.chain.Context)
     */
    public boolean execute(Context ctx) {
        try {
            final Content node = getNode(ctx);
            if (isRecursive()) {
                // set versionMap and version name for this node
                List versionMap = new ArrayList();
                versionRecursively(node, ctx, versionMap);
                ctx.setAttribute(Context.ATTRIBUTE_VERSION_MAP, versionMap, Context.LOCAL_SCOPE);
            } else {
                addComment(node);
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

    protected void addComment(final Content node) throws AccessDeniedException, RepositoryException {
        if(getComment() != null){
            synchronized (ExclusiveWrite.getInstance()) {
                node.getMetaData().setProperty(Context.ATTRIBUTE_COMMENT, getComment());
                node.save();
            }
        }
    }

    private void versionRecursively(Content node, Context ctx, List versionMap) throws RepositoryException {
        addComment(node);
        Version version = node.addVersion(getRule());
        Map entry = new HashMap();
        entry.put("version", version.getName());
        entry.put("uuid", node.getUUID());
        versionMap.add(entry);
        if(StringUtils.isEmpty((String)ctx.getAttribute(Context.ATTRIBUTE_VERSION))){
            ctx.setAttribute(Context.ATTRIBUTE_VERSION, version.getName(), Context.LOCAL_SCOPE);
        }

        Iterator children = node.getChildren(getFilter()).iterator();
        while (children.hasNext()) {
            versionRecursively((Content) children.next(), ctx, versionMap);
        }
    }

    protected Content.ContentFilter getFilter() {
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
        return filter;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void release() {
        super.release();
        this.recursive = false;
    }
}
