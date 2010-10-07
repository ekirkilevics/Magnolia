/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.freemarker;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;

import java.io.Writer;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Exception handler providing different output based on the configuration of the current instance. Full stacktrace gets rendered on the author instance only, while ignoring the stacktraces in preview mode and on public instances.
 * @author gjoseph
 * @version $Id: $
 */
public class ModeDependentTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
        final boolean isAuthorInstance = ServerConfiguration.getInstance().isAdmin();
        final boolean isPreviewMode = MgnlContext.getAggregationState().isPreviewMode();
        if (isAuthorInstance && !isPreviewMode) {
            inEditMode(te, env, out);
        } else {
            inPublicMode(te, env, out);
        }
    }

    protected void inPublicMode(TemplateException te, Environment env, Writer out) throws TemplateException {
        TemplateExceptionHandler.IGNORE_HANDLER.handleTemplateException(te, env, out);
    }

    protected void inEditMode(TemplateException te, Environment env, Writer out) throws TemplateException {
        TemplateExceptionHandler.HTML_DEBUG_HANDLER.handleTemplateException(te, env, out);
    }
}
