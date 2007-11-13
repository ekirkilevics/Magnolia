/**
 * This file Copyright (c) 2003-2007 Magnolia International
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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.jsp.PageContext;


/**
 * The central component to call when rendering paragraphs.
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ParagraphRenderingFacade {

    private final ParagraphRendererManager rendererManager;

    private final ParagraphManager paragraphManager;

    public static ParagraphRenderingFacade getInstance() {
        return (ParagraphRenderingFacade) FactoryUtil.getSingleton(ParagraphRenderingFacade.class);
    }

    public ParagraphRenderingFacade() {
        this(ParagraphRendererManager.getInstance(), ParagraphManager.getInstance());
    }

    // currently only used for tests, but one day, we'll go IOC instead of using singletons !
    ParagraphRenderingFacade(ParagraphRendererManager rendererManager, ParagraphManager paragraphManager) {
        this.rendererManager = rendererManager;
        this.paragraphManager = paragraphManager;
    }

    /**
     * Renders the given node to the given Writer, using the appropriate Paragraph (and the appropriate ParagraphRender
     * as mandated by this Paragraph).
     */
    public void render(Content content, Writer out) throws IOException {
        render(content, out, null);
    }

    /**
     * Renders the given node to the given Writer, using the appropriate Paragraph (and the appropriate ParagraphRender
     * as mandated by this Paragraph).
     */
    public void render(Content content, Writer out, PageContext pageContext) throws IOException {
        final String paragraphName = content.getMetaData().getTemplate();
        final Paragraph paragraph = paragraphManager.getInfo(paragraphName);
        if (paragraph == null) {
            throw new IllegalStateException("Paragraph " + paragraphName + " not found for page " + content.getHandle());
        }
        render(content, paragraph, out, pageContext);
    }

    /**
     * Renders the given node to the given Writer, using the given Paragraph (and the appropriate ParagraphRender as
     * mandated by this Paragraph). Use with care.
     */
    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        render(content, paragraph, out, null);
    }

    /**
     * Renders the given node to the given Writer, using the given Paragraph (and the appropriate ParagraphRender as
     * mandated by this Paragraph). Use with care.
     */
    public void render(Content content, Paragraph paragraph, Writer out, PageContext pageContext) throws IOException {
        setupPageContext(pageContext);
        final String paragraphType = paragraph.getType();
        final ParagraphRenderer renderer = rendererManager.getRenderer(paragraphType);

        renderer.render(content, paragraph, out);
    }

    /**
     * @param pageContext
     */
    private void setupPageContext(PageContext pageContext) {
        if (pageContext != null && MgnlContext.getInstance() instanceof WebContext) {
            ((WebContext) MgnlContext.getInstance()).setPageContext(pageContext);
        }
    }

}
