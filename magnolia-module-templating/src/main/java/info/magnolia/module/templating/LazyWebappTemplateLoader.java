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
package info.magnolia.module.templating;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.ServletContext;

import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

public class LazyWebappTemplateLoader implements TemplateLoader {
    
    private WebappTemplateLoader loader;

    public void closeTemplateSource(Object templateSource) throws IOException {
        if (loader != null) {
            loader.closeTemplateSource(templateSource);
        }
    }

    public Object findTemplateSource(String name) throws IOException {
        final WebContext webCtx = getWebContextOrNull();
        if (webCtx != null) {
            ServletContext sc = webCtx.getServletContext();
            if (sc != null) {
                loader = new WebappTemplateLoader(sc, "");               
            }
        }
        return loader == null ? null : loader.findTemplateSource(name);
    }

    public long getLastModified(Object templateSource) {
        return loader == null ? 0 : loader.getLastModified(templateSource);
    }

    public Reader getReader(Object templateSource, String encoding)
            throws IOException {
        return loader == null ? null : loader.getReader(templateSource, encoding);
    }

    private WebContext getWebContextOrNull() {
        if (MgnlContext.hasInstance()) {
            final Context mgnlCtx = MgnlContext.getInstance();
            if (mgnlCtx instanceof WebContext) {
                return (WebContext) mgnlCtx;
            }
        }
        return null;
    }
}
