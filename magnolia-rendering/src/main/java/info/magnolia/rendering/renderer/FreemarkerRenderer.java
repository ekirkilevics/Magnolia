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

import freemarker.template.TemplateException;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Uses FreeMarker to render the content.
 *
 * @version $Id$
 */
public class FreemarkerRenderer extends AbstractRenderer {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerTemplateRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public FreemarkerRenderer() {
        this(Components.getComponent(FreemarkerHelper.class));
    }

    FreemarkerRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    @Override
    protected void onRender(Node content, RenderableDefinition definition, RenderingContext renderingCtx, Map<String, Object> ctx, String templateScript) throws RenderException {
        final Locale locale = MgnlContext.getAggregationState().getLocale();

        try {
            AppendableWriter out = renderingCtx.getAppendable();
            fmHelper.render(templateScript, locale, definition.getI18nBasename(), ctx, out);
        }
        catch (TemplateException e) {
            throw new RenderException(e);
        }
        catch (IOException e) {
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
