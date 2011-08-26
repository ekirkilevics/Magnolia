/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.elements;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

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

    private static final String LINE_BREAK = "\r\n";
    private static final String GREATER_THAN = ">";
    private static final String LESS_THAN = "<";
    private static final String SLASH = "/";

    private static final String XML_BEGIN_COMMENT = "<!-- ";
    private static final String XML_END_COMMENT = " -->";

    private static final String CMS_BEGIN_CONTENT = "cms:begin cms:content";
    private static final String CMS_END_CONTENT = "cms:end cms:content";

    private static final String CMS_BEGIN_CONTENT_COMMENT = XML_BEGIN_COMMENT + CMS_BEGIN_CONTENT + EQUALS + QUOTE;

    private static final String CMS_END_CONTENT_COMMENT = XML_BEGIN_COMMENT + CMS_END_CONTENT + EQUALS + QUOTE;

    private final Appendable appendable;

    public MarkupHelper(Appendable appendable) {
        this.appendable = appendable;
    }

    public MarkupHelper attribute(String name, String value) throws IOException {
        // TODO we need to do html attribute escaping on the value
        if (StringUtils.isNotEmpty(value)) {
            appendable.append(SPACE).append(name).append(EQUALS).append(QUOTE).append(value).append(QUOTE);
        }
        return this;
    }

    public MarkupHelper startContent(Node node) throws IOException, RenderException {
        appendable.append(CMS_BEGIN_CONTENT_COMMENT).append(getNodePath(node)).append(QUOTE).append(XML_END_COMMENT).append(LINE_BREAK);
        return this;
    }

    public MarkupHelper endContent(Node node) throws IOException, RenderException {
        appendable.append(CMS_END_CONTENT_COMMENT).append(getNodePath(node)).append(QUOTE).append(XML_END_COMMENT).append(LINE_BREAK);
        return this;
    }

    public MarkupHelper openTag(String tagName) throws IOException, RenderException {
        appendable.append(LESS_THAN).append(tagName);
        return this;
    }

    public MarkupHelper closeTag(String tagName) throws IOException {
        appendable.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(tagName).append(GREATER_THAN).append(LINE_BREAK);
        return this;
    }

    protected String getNodePath(Node node) throws RenderException {
        try {
            return node.getSession().getWorkspace().getName() + ":" + node.getPath();
        } catch (RepositoryException e) {
            throw new RenderException("Can't construct node path for node " + node);
        }
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
