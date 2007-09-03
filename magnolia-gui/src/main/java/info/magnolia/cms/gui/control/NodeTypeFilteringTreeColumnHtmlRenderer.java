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
 * @author vsteller
 *
 */
public class NodeTypeFilteringTreeColumnHtmlRenderer extends ConditionalTreeColumnHtmlRenderer {
    private static final Logger log = LoggerFactory.getLogger(ConditionalTreeColumnHtmlRenderer.class);

    protected final String[] allowedNodeTypeNames;

    public NodeTypeFilteringTreeColumnHtmlRenderer(TreeColumnHtmlRenderer interceptedRenderer, String[] allowedNodeTypeNames) {
        super(interceptedRenderer);
        this.allowedNodeTypeNames = allowedNodeTypeNames;
    }

    public boolean evaluate(Content content) {
        try {
            return ArrayUtils.contains(allowedNodeTypeNames, content.getNodeTypeName());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return false;
    }

}
