/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.rendering.engine;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.context.RenderingContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler providing different output based on the configuration of the current instance. Full stacktrace gets
 * rendered on the author instance only, while ignoring the stacktraces in preview mode and on public instances.
 *
 * @version $Id$
 */
public class ModeDependentRenderExceptionHandler implements RenderExceptionHandler {

    private static Logger log = LoggerFactory.getLogger(ModeDependentRenderExceptionHandler.class);

    public static final String RENDER_ERROR_MESSAGE_BEGIN =
            "<!-- ERROR MESSAGE STARTS HERE --><script language=javascript>//\"></script><script language=javascript>//\'></script><script language=javascript>//\"></script><script language=javascript>//\'></script></title></xmp></script></noscript></style></object></head></pre></table></form></table></table></table></a></u></i></b><div align=left style='background-color:#FFFF00; color:#FF0000; display:block; border-top:double; padding:2pt; font-size:medium; font-family:Arial,sans-serif; font-style: normal; font-variant: normal; font-weight: normal; text-decoration: none; text-transform: none'><b style='font-size:medium'>Template Error!</b><pre><xmp>";

    public static final String RENDER_ERROR_MESSAGE_END = "</xmp></pre></div></html>";

    private ServerConfiguration serverConfiguration;

    @Inject
    public ModeDependentRenderExceptionHandler(ServerConfiguration config) {
        this.serverConfiguration = config;
    }

    @Override
    public void handleException(RenderException renderException, RenderingContext renderingContext) {
        String path;
        try {
            path = renderingContext.getCurrentContent().getPath();
        }
        catch (RepositoryException e) {
            path = "Can't read content";
        }
        String id = renderingContext.getRenderableDefinition().getId();
        PrintWriter out;
        try {
            out = getPrintWriterFor(renderingContext.getAppendable());
            String msg = "Error while rendering [" + path + "] with template ["+id+"]: " + ExceptionUtils.getMessage(renderException);
            if ( serverConfiguration.isAdmin() && !MgnlContext.getAggregationState().isPreviewMode()) {
                inEditMode(msg, renderException, out);
            } else {
                inPublicMode(msg, renderException, out);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Can't log template exception.", e);
        }
        out.flush();
    }

    private PrintWriter getPrintWriterFor(Writer out) {
        return (out instanceof PrintWriter) ? (PrintWriter) out : new PrintWriter(out);
    }

    protected void inPublicMode(String msg, RenderException renderException, PrintWriter out) {
        log.error(msg, renderException);
    }

    protected void inEditMode(String msg, RenderException renderException, PrintWriter out) {
        log.error(msg, renderException);
        out.println(RENDER_ERROR_MESSAGE_BEGIN);
        out.println(msg);
        out.println(RENDER_ERROR_MESSAGE_END);
    }
}
