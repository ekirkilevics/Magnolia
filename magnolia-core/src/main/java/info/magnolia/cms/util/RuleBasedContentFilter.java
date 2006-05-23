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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision: 1633 $ ($Author: scharles $)
 */
public class RuleBasedContentFilter implements Content.ContentFilter {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(RuleBasedContentFilter.class);

    /**
     * Rule on which this filter works
     */
    private Rule rule;

    /**
     * @param rule
     */
    public RuleBasedContentFilter(Rule rule) {
        this.rule = rule;
    }

    /**
     * Test if this content should be included in a resultant collection
     * @param content
     * @return if true this will be a part of collection
     */
    public boolean accept(Content content) {
        String nodeType = "";
        try {
            nodeType = content.getNodeTypeName();
        }
        catch (RepositoryException re) {
            if (log.isDebugEnabled()) {
                log.debug("failed to retrieve node type : " + re.getMessage(), re);
            }
        }
        return this.rule.isAllowed(nodeType);
    }

}
