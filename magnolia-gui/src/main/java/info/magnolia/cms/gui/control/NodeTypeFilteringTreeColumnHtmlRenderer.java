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
package info.magnolia.cms.gui.control;

import info.magnolia.cms.core.Content;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

/**
 * A tree column renderer which delegates to another one if the node's type is one
 * of the allows ones.
 *
 * @author vsteller
 */
public class NodeTypeFilteringTreeColumnHtmlRenderer implements TreeColumnHtmlRenderer {
    private static final Logger log = LoggerFactory.getLogger(NodeTypeFilteringTreeColumnHtmlRenderer.class);

    private final TreeColumnHtmlRenderer delegate;
    private final String[] allowedNodeTypeNames;

    public NodeTypeFilteringTreeColumnHtmlRenderer(TreeColumnHtmlRenderer delegate, String[] allowedNodeTypeNames) {
        this.delegate = delegate;
        this.allowedNodeTypeNames = allowedNodeTypeNames;
    }

    public String renderHtml(TreeColumn treeColumn, Content content) {
        final boolean shouldRender = shouldRender(content);
        return shouldRender ? delegate.renderHtml(treeColumn, content) : " "; // space avoids underscore
    }

    protected boolean shouldRender(Content content) {
        try {
            return ArrayUtils.contains(allowedNodeTypeNames, content.getNodeTypeName());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return false;
    }

}
