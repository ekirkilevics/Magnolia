/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.templating.elements;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.rendering.engine.RenderException;

/**
 * Helper class for building markup in templating elements.
 *
 * @version $Id$
 */
public class MarkupHelper implements Appendable {

    private static final String EQUALS = "=";
    private static final String SPACE = " ";
    private static final String QUOTE = "\"";

    private static final String LINE_BREAK = "\n";
    private static final String GREATER_THAN = ">";
    private static final String LESS_THAN = "<";
    private static final String SLASH = "/";

    private static final String XML_BEGIN_COMMENT = "<!-- ";
    private static final String XML_END_COMMENT = " -->";

    private static final String CMS_BEGIN_TAG = "cms:begin";
    private static final String CMS_END_TAG = "cms:end";

    private static final String CONTENT_ATTRIBUTE = "cms:content";
    private static final String TYPE_ATTRIBUTE = "cms:type";

    private final Appendable appendable;

    public MarkupHelper(Appendable appendable) {
        this.appendable = appendable;
    }

    public MarkupHelper attribute(String name, String value) throws IOException {
        // TODO we need to do html attribute escaping on the value
        if (value != null) {
            appendable.append(SPACE).append(name).append(EQUALS).append(QUOTE).append(value).append(QUOTE);
        }
        return this;
    }

    public MarkupHelper startContent(Node node) throws IOException, RenderException {
        appendable.append(XML_BEGIN_COMMENT);
        append(CMS_BEGIN_TAG);
        attribute(CONTENT_ATTRIBUTE, getNodeId(node));
        attribute(TYPE_ATTRIBUTE, getNodeType(node));
        appendable.append(XML_END_COMMENT).append(LINE_BREAK);
        return this;
    }

    public MarkupHelper endContent(Node node) throws IOException, RenderException {
        appendable.append(XML_BEGIN_COMMENT);
        append(CMS_END_TAG);
        attribute(CONTENT_ATTRIBUTE, getNodeId(node));
        appendable.append(XML_END_COMMENT).append(LINE_BREAK);
        return this;
    }

    public MarkupHelper openTag(String tagName) throws IOException {
        appendable.append(LESS_THAN).append(tagName);
        return this;
    }

    public MarkupHelper closeTag(String tagName) throws IOException {
        appendable.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(tagName).append(GREATER_THAN).append(LINE_BREAK);
        return this;
    }

    public MarkupHelper openComment(String tagName) throws IOException {
        appendable.append(XML_BEGIN_COMMENT).append(tagName);
        return this;
    }

    public MarkupHelper closeComment(String tagName) throws IOException {
        appendable.append(XML_BEGIN_COMMENT).append(SLASH).append(tagName).append(XML_END_COMMENT).append(LINE_BREAK);
        return this;
    }

    protected String getNodeId(Node node) throws RenderException {
        if(node == null){
            return "";
        }
        try {
            return node.getSession().getWorkspace().getName() + ":" + node.getPath();
        } catch (RepositoryException e) {
            throw new RenderException("Can't construct node path for node " + node);
        }
    }

    protected String getNodeType(Node node) throws RenderException {
        if(node == null){
            return "";
        }
        try {
            return node.getPrimaryNodeType().getName();
        }
        catch (RepositoryException e) {
            throw new RenderException("Can't read node type for node " + node);        }
    }

    @Override
    public Appendable append(CharSequence charSequence) throws IOException {
        return appendable.append(charSequence);
    }

    @Override
    public Appendable append(CharSequence charSequence, int i, int i1) throws IOException {
        return appendable.append(charSequence, i, i1);
    }

    @Override
    public Appendable append(char c) throws IOException {
        return appendable.append(c);
    }
}
