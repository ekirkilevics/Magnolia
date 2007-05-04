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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;

import java.io.IOException;
import java.io.Writer;

/**
 * The central component to call when rendering paragraphs.
 *
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
     * Renders the given node to the given Writer, using the appropriate
     * Paragraph (and the appropriate ParagraphRender as mandated by this Paragraph).
     */
    public void render(Content content, Writer out) throws IOException {
        final String paragraphName = content.getMetaData().getTemplate();
        final Paragraph paragraph = paragraphManager.getInfo(paragraphName);
        if (paragraph == null) {
            throw new IllegalStateException("Paragraph " + paragraphName + " not found for page " + content.getHandle());
        }
        render(content, paragraph, out);

    }

    /**
     * Renders the given node to the given Writer, using the given
     * Paragraph (and the appropriate ParagraphRender as mandated by this Paragraph).
     * Use with care.
     */
    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String paragraphType = paragraph.getType();
        final ParagraphRenderer renderer = rendererManager.getRenderer(paragraphType);

        renderer.render(content, paragraph, out);
    }

}
