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
package info.magnolia.module.templating.renderers;

import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.Renderable;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.module.templating.RenderException;

import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Simple freemarker template renderer, mapped to template type <code>freemarker</code>. The valid attributes of freemarker templates are
 * <code>path</code>, which specify the template to be processed and <code>actionClass</code> specifying executor for any necessary code
 * execution. Instance of this class is then available in the template as <code>templateAction</code> variable.
 *
 * @version $Revision: 14052 $ ($Author: gjoseph $)
 */
public class FreemarkerTemplateRenderer extends AbstractTemplateRenderer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FreemarkerTemplateRenderer.class);

    private FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerTemplateRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public FreemarkerTemplateRenderer() {
        this(FreemarkerHelper.getInstance());
    }

    FreemarkerTemplateRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    protected void callTemplate(String templatePath, Renderable renderable, Map ctx, Writer out) throws RenderException {
        final Locale locale = MgnlContext.getAggregationState().getLocale();

        try {
            fmHelper.render(templatePath, locale, renderable.getI18nBasename(), ctx, out);
        }
        catch (TemplateException e) {
            // TODO should be thrown?
        }
        catch (Exception e) {
            //log.error("Failed to process Freemarker template with " + e.getMessage(), e);
            throw new RenderException("Can't render template " + templatePath + ": " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    protected Map newContext() {
        return new HashMap();
    }
}
