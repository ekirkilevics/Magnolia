/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BaseConditionalContentTag extends BaseContentTag {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BaseConditionalContentTag.class);

    /**
     * <p>Subclasses implement this method to compute the boolean result
     * of the conditional action. This method is invoked once per tag invocation
     * by <tt>doStartTag()</tt>.
     *
     * @return a boolean representing the condition that a particular subclass
     *   uses to drive its conditional logic.
     */
    protected abstract boolean condition() throws JspTagException;

    /**
     * Includes its body if <tt>condition()</tt> evaluates to true.
     */
    public int doStartTag() throws JspException {

        // execute our condition() method once per invocation
        boolean result = condition();

        // handle conditional behavior
        if (result)
            return EVAL_BODY_INCLUDE;
        else
            return SKIP_BODY;
    }

}
