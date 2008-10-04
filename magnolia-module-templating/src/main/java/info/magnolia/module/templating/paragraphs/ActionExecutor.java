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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import info.magnolia.cms.beans.config.ActionBasedParagraph;
import info.magnolia.cms.beans.config.ActionBasedRenderable;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.runtime.ParagraphRenderer;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.renderers.RenderException;


/**
 * Abstract paragraph which optionally supports the execution of an action
 * class whose constructor can either be empty or take exactly one Content parameter and
 * one ActionBasedParagraph parameter.
 *
 * @author pbracher
 * @version $Id$
 */
public class ActionExecutor {

    public static ActionExecutor getInstace(){
        return (ActionExecutor) FactoryUtil.getSingleton(ActionExecutor.class);
    }

    public ActionResult execute(Content content, ActionBasedRenderable abp) throws RenderException {
        final Class actionClass = abp.getActionClass();
        if (actionClass == null) {
            return null;
        }
        return execute(actionClass, content, abp);
    }

    protected ActionResult execute(Class actionClass, Content content, ActionBasedRenderable renderable) throws RenderException {
        // see MVCServletHandlerImpl.init() if we need to populate the action bean

        // TODO : refactoring w/ Pages ?

        try {
            final Object actionBean = instantiate(actionClass, content, renderable);
            final Map params = MgnlContext.getParameters();
            if (params != null) {
                BeanUtils.populate(actionBean, params);
            }

            final Method method = actionClass.getMethod("execute", null);
            final Object result = method.invoke(actionBean, null);
            String templatePath = renderable.determineTemplatePath(result, actionBean);
            return new ActionResult(result, actionBean, templatePath);
        } catch (Exception e) {
            throw new RenderException("Can't execute action " + actionClass.getName(), e);
        }
    }

    protected Object instantiate(Class actionClass, Content content, ActionBasedRenderable renderable) throws InstantiationException,
        IllegalAccessException, InvocationTargetException {
            final Constructor[] constructors = actionClass.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                final Constructor c = constructors[i];
                final Class[] params = c.getParameterTypes();
                if (params.length == 2 && params[0].equals(Content.class) && params[1].equals(ActionBasedParagraph.class)) {
                    return c.newInstance(new Object[]{content, renderable});
                }
            }
            return actionClass.newInstance();
        }

}