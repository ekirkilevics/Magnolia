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

/**
 * Renders a column value based on the outcome of the 
 * {@link #evaluate(Content)} method.
 *
 * @author vsteller
 *
 */
public abstract class ConditionalTreeColumnHtmlRenderer implements TreeColumnHtmlRenderer {
    protected final TreeColumnHtmlRenderer interceptedRenderer;

    public ConditionalTreeColumnHtmlRenderer(TreeColumnHtmlRenderer interceptedRenderer) {
        this.interceptedRenderer = interceptedRenderer;
    }

    public String renderHtml(TreeColumn treeColumn, Content content) {
        return (evaluate(content) ? interceptedRenderer.renderHtml(treeColumn, content) : " "); // space avoids underscore
    }

    public abstract boolean evaluate(Content content);
}
