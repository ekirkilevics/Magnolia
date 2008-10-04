/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.templating.paragraphs;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.Renderable;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.renderers.AbstractRenderer;
import info.magnolia.module.templating.renderers.ActionResult;
import info.magnolia.module.templating.renderers.RenderException;


/**
 * @author pbracher
 * @version $Id$
 *
 */
public abstract class AbstractParagraphRenderer extends AbstractRenderer implements ParagraphRenderer{

    private static Logger log = LoggerFactory.getLogger(AbstractParagraphRenderer.class);

    public AbstractParagraphRenderer() {
        super();
    }

    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        try {
            render(content, (Renderable) paragraph, out);
        }
        catch (RenderException e) {
            log.error("", e);
            throw new IOException(e.getMessage());
        }
        finally{
            out.flush();
        }
    }

    protected Map saveContextState(Map ctx) {
        Map state = super.saveContextState(ctx);
        saveAttribute(ctx, state, "paragraphDef");
        return state;
    }

    protected void setupContext(Map ctx, Content content, Renderable renderable, ActionResult actionResult) {
        super.setupContext(ctx, content, renderable, actionResult);
        setContextAttribute(ctx, "paragraphDef", renderable);
    }
}