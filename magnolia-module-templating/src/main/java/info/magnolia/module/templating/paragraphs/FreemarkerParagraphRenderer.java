/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.templating.paragraphs;

import freemarker.template.TemplateException;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.module.templating.RenderException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Renders a paragraph using freemarker. Optionally supports the execution of an action
 * class whose constructor can either be empty or take exactly one Content parameter and
 * one ActionBasedParagraph parameter.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class FreemarkerParagraphRenderer extends AbstractParagraphRenderer {

    private static Logger log = LoggerFactory.getLogger(FreemarkerParagraphRenderer.class);

    private final FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerParagraphRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     * @deprecated since 5.0, use {@link #FreemarkerParagraphRenderer(info.magnolia.freemarker.FreemarkerHelper)}
     */
    public FreemarkerParagraphRenderer() {
        this(FreemarkerHelper.getInstance());
    }

    public FreemarkerParagraphRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    @Override
    protected void onRender(Content content, RenderableDefinition definition, Writer out, final Map ctx, String templatePath) throws RenderException {
        final Locale locale = MgnlContext.getAggregationState().getLocale();

        boolean wrap = false;
        try {
            if (out instanceof JspWriter) {
                // when FM wraps writer in a parent tag it gets confused and doesn't wrap the inner one again
                wrap = true;
                out = new PrintWriter(out);
            }
            log.debug ("About to call FM renderer with {}wrapped writer: {}", wrap? "" : "un", out );
            fmHelper.render(templatePath, locale, definition.getI18nBasename(), ctx, out);


        } catch (TemplateException e) {
            // TODO: handle exception
            // exception is logged by freemarker and yellow message in the template inserted
        }catch (Exception e) {
            throw new RenderException("Can't render paragraph template " + templatePath + ": " + ExceptionUtils.getRootCauseMessage(e), e);
        } finally {
            if (wrap) {
                try {
                    out.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected Map saveContextState(Map ctx) {
        Map state = super.saveContextState(ctx);
        saveAttribute(ctx, state, "params");
        return state;
    }

    @Override
    protected void setupContext(Map ctx, Content content, RenderableDefinition definition, RenderingModel state, Object actionResult) {
        super.setupContext(ctx, content, definition, state, actionResult);
        setContextAttribute(ctx, "params", MgnlContext.getParameters());
    }

    @Override
    protected Map newContext() {
        return new HashMap();
    }

}
