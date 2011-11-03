/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.rendering.renderer;

import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.lang.exception.ExceptionUtils;


/**
 * Uses FreeMarker to render the content.
 *
 * @version $Id$
 */
public class FreemarkerRenderer extends AbstractRenderer {

    private final FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerTemplateRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public FreemarkerRenderer() {
        this(FreemarkerHelper.getInstance());
    }

    FreemarkerRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    @Override
    protected void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException {
        final Locale locale = MgnlContext.getAggregationState().getLocale();

        try {
            // FIXME we should not buffer, this is just to facilitate the STK migration
            StringWriter buffer = new StringWriter();
            AppendableWriter out = renderingCtx.getAppendable();
            try {
                fmHelper.render(templateScript, locale, definition.getI18nBasename(), ctx, buffer);
                out.append(buffer.toString());
            }
            catch (Throwable e) {
                // FIXME we don't throw exceptions to not block the rendering
                // the 'normal' exception handler should actually not throw exceptions so that we can re-throw them
                // throw new RenderException("Can't render template " + templateScript + ": " + ExceptionUtils.getRootCauseMessage(e), e);
                out.append("ERROR: ").append(ExceptionUtils.getRootCauseMessage(e)).append("<br/>");
            }
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    @Override
    protected Map<String, Object> newContext() {
        return new HashMap<String, Object>();
    }


    public FreemarkerHelper getFmHelper() {
        return fmHelper;
    }

}
