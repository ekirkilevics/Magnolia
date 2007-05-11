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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;

import javax.servlet.jsp.tagext.BodyTagSupport;


/**
 * @author Marcel Salathe
 * @version $Revision$ ($Author$)
 */
public class UnloadPage extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final Content mainContent = aggregationState.getMainContent();
        aggregationState.setCurrentContent(mainContent);
        return EVAL_PAGE;
    }
}
