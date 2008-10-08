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

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import info.magnolia.cms.beans.config.ActionBasedRenderable;
import info.magnolia.cms.beans.config.Renderable;
import info.magnolia.cms.core.Content;
import info.magnolia.context.MgnlContext;


/**
 * @author pbracher
 * @version $Id$
 *
 */
public abstract class AbstractRenderer {

    public AbstractRenderer() {
        super();
    }

    protected void render(Content content, Renderable renderable, Writer out) throws RenderException {
        final Map ctx = newContext();
        Map state = saveContextState(ctx);

        ActionResult actionResult = executeAction(content, renderable);

        setupContext(ctx, content, renderable, actionResult);

        String templatePath = renderable.getTemplatePath();
        if(actionResult != null && actionResult.getTemplatePath() != null){
            templatePath = actionResult.getTemplatePath();
        }

        if (templatePath == null) {
            throw new IllegalStateException("Unable to render " + renderable.getName() + " in page " + content.getHandle() + ": templatePath not set.");
        }

        callTemplate(templatePath, renderable, ctx, out);

        restoreContext(ctx, state);
    }

    protected ActionResult executeAction(Content content, Renderable renderable) throws RenderException {
        ActionResult actionResult;
        if(renderable instanceof ActionBasedRenderable){
             actionResult = ActionExecutor.getInstace().execute(content, (ActionBasedRenderable)renderable);
        }
        else{
            actionResult = null;
        }
        return actionResult;
    }

    protected Map saveContextState(final Map ctx) {
        Map state = new HashMap();
        // save former values
        saveAttribute(ctx, state, "content");
        saveAttribute(ctx, state, "result");
        saveAttribute(ctx, state, "action");
        // TODO should be page in freemarker?
        saveAttribute(ctx, state, "actpage");
        return state;
    }

    protected void saveAttribute(final Map ctx, Map state, String name) {
        state.put(name, ctx.get(name));
    }

    protected void restoreContext(final Map ctx, Map state) {
        for (Iterator iterator = state.keySet().iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            setContextAttribute(ctx, name, state.get(name));
        }
    }

    protected void setupContext(final Map ctx, Content content, Renderable renderable, ActionResult actionResult){
        setContextAttribute(ctx, "content", content);
        setContextAttribute(ctx, "aggregationState", MgnlContext.getAggregationState());
        // TODO should be page for freemarker?
        setContextAttribute(ctx, "actpage", MgnlContext.getAggregationState().getMainContent());

        if (actionResult != null) {
            setContextAttribute(ctx, "result", actionResult.getResult());
            setContextAttribute(ctx, "action", actionResult.getActionBean());
        }
    }

    protected Object setContextAttribute(final Map ctx, final String name, Object value) {
        return ctx.put(name, value);
    }

    protected abstract Map newContext();

    protected abstract void callTemplate(String templatePath, Renderable renderable, Map ctx, Writer out) throws RenderException;

}