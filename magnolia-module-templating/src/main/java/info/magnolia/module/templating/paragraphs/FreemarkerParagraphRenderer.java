/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import freemarker.template.TemplateException;
import info.magnolia.cms.beans.config.ActionBasedParagraph;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;
import info.magnolia.freemarker.FreemarkerHelper;
import org.apache.commons.beanutils.BeanUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class FreemarkerParagraphRenderer implements ParagraphRenderer {

    private final FreemarkerHelper fmHelper;

    /**
     * Constructs a FreemarkerParagraphRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public FreemarkerParagraphRenderer() {
        this(FreemarkerHelper.getInstance());
    }

    FreemarkerParagraphRenderer(FreemarkerHelper fmRenderer) {
        this.fmHelper = fmRenderer;
    }

    public void render(Content content, Paragraph paragraph, Writer out) throws IOException {
        final String templatePath = paragraph.getTemplatePath();

        if (templatePath == null) {
            throw new IllegalStateException("Unable to render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }

        final ActionResult actionResult;
        if (paragraph instanceof ActionBasedParagraph) {
            ActionBasedParagraph abp = (ActionBasedParagraph) paragraph;
            final Class actionClass = abp.getActionClass();
            if (actionClass == null) {
                throw new IllegalStateException("Can't render paragraph " + paragraph.getName() + " in page " + content.getHandle() + ": actionClass not set.");
            }
            actionResult = execute(actionClass, content, abp, abp.getAllowedParametersList());
        } else {
            actionResult = null;
        }

        final String template = determineTemplatePath(templatePath, actionResult);

        final Map freemarkerCtx = new HashMap();
        freemarkerCtx.put("content", content);
        freemarkerCtx.put("actpage", MgnlContext.getAggregationState().getMainContent());
        freemarkerCtx.put("paragraphConfig", paragraph);
        if (actionResult != null) {
            freemarkerCtx.put("result", actionResult.getResult());
            freemarkerCtx.put("action", actionResult.getActionBean());
        }

        final Locale locale = MgnlContext.getAggregationState().getLocale();

        try {
            fmHelper.render(template, locale, paragraph.getI18nBasename(), freemarkerCtx, out);
        } catch (TemplateException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected ActionResult execute(Class actionClass, Content content, ActionBasedParagraph paragraph, String[] allowedParametersList) {
        // see MVCServletHandlerImpl.init() if we need to populate the action bean

        // TODO : refactoring w/ Pages ?

        try {
            final Object actionBean = instanciate(actionClass, content, paragraph);
            final Map params = MgnlContext.getParameters();
            if (params != null && allowedParametersList != null) {
                final Map filteredParams = new HashMap();
                for (int i = 0; i < allowedParametersList.length; i++) {
                    final String param = allowedParametersList[i];
                    filteredParams.put(param, params.get(param));
                }
                BeanUtils.populate(actionBean, filteredParams);
            }

            final Method method = actionClass.getMethod("execute", null);
            final Object result = method.invoke(actionBean, null);
            return new ActionResult(result, actionBean);
        } catch (InstantiationException e) {
            throw new RuntimeException(e); // TODO
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e); // TODO
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected Object instanciate(Class actionClass, Content content, ActionBasedParagraph paragraph) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        final Constructor[] constructors = actionClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            final Constructor c = constructors[i];
            final Class[] params = c.getParameterTypes();
            if (params.length == 2 && params[0].equals(Content.class) && params[1].equals(ActionBasedParagraph.class)) {
                return c.newInstance(new Object[]{content, paragraph});
            }
        }
        return actionClass.newInstance();
    }

    /**
     * Override this method if you need specific templates depending on the action result.
     */
    protected String determineTemplatePath(String originalTemplateName, ActionResult actionResult) {
        return originalTemplateName;
    }

    protected static final class ActionResult {
        private final Object result;
        private final Object actionBean;

        public ActionResult(Object result, Object actionBean) {
            this.result = result;
            this.actionBean = actionBean;
        }

        public Object getResult() {
            return result;
        }

        public Object getActionBean() {
            return actionBean;
        }
    }

}
