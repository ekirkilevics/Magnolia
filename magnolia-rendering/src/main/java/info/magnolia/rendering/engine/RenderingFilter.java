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
package info.magnolia.rendering.engine;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter responsible for rendering the current aggregation state,
 * by delegating to the appropriate TemplateRenderer or by serving
 * binary content.
 *
 * @version $Id$
 *
 */
public class RenderingFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(RenderingFilter.class);

    private final RenderingEngine renderingEngine;

    private final TemplateDefinitionRegistry templateDefinitionRegistry;

    public RenderingFilter(RenderingEngine renderingEngine, TemplateDefinitionRegistry templateDefinitionRegistry) {
        this.renderingEngine = renderingEngine;
        this.templateDefinitionRegistry = templateDefinitionRegistry;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        final AggregationState aggregationState = MgnlContext.getAggregationState();

        String templateName = aggregationState.getTemplateName();
        if (StringUtils.isNotEmpty(templateName)) {
            try {
                // don't reset any existing status code, see MAGNOLIA-2005
                // response.setStatus(HttpServletResponse.SC_OK);
                if (response != MgnlContext.getWebContext().getResponse()) {
                    log.warn("Context response not synced. This may lead to discrepancies in rendering.");
                }

                Node content = aggregationState.getMainContent().getJCRNode();

                render(content, templateName, response);

                try {
                    response.flushBuffer();
                }
                catch (IOException e) {
                    // don't log at error level since tomcat typically throws a
                    // org.apache.catalina.connector.ClientAbortException if the user stops loading the page
                    log.debug("Exception flushing response " + e.getClass().getName() + ": " + e.getMessage(), e);
                }

            }
            catch (RenderException e) {
                // TODO better handling of rendering exception
                // TODO dlipp: why not move this section up to the actual call to render() -> that's the only place where a RenderException could occur...
                log.error(e.getMessage(), e);
                throw new ServletException(e);
            }
            catch (Exception e) {
                // TODO dlipp: there's no other checked exceptions thrown in the code above - is it correct to react like that???
                log.error(e.getMessage(), e);
                if (!response.isCommitted()) {
                    response.setContentType("text/html");
                }
                throw new RuntimeException(e);
            }
        }
        else {
            // direct request
            handleResourceRequest(aggregationState, request, response);
        }

        // TODO don't make it a dead end
        //      currently we can't process the chain because there is no content/nop servlet
        // chain.doFilter(request, response);
    }

    protected void render(Node content, String templateName, HttpServletResponse response) throws RenderException {

        TemplateDefinition templateDefinition;
        try {
            templateDefinition = templateDefinitionRegistry.getTemplateDefinition(templateName);
        }
        catch (RegistrationException e) {
            throw new RenderException(e);
        }
        renderingEngine.render(content, templateDefinition, Collections.<String, Object> emptyMap(), new ResponseOutputProvider(response));
    }


    /**
     * Get the requested resource and copy it to the ServletOutputStream, bit by bit.
     * @param request HttpServletRequest as given by the servlet container
     * @param response HttpServletResponse as given by the servlet container
     * @throws IOException standard servlet exception
     */
    protected void handleResourceRequest(AggregationState aggregationState, HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String resourceHandle = aggregationState.getHandle();

        log.debug("handleResourceRequest, resourceHandle=\"{}\"", resourceHandle);

        if (StringUtils.isNotEmpty(resourceHandle)) {


            InputStream is = null;
            try {
                Session session = MgnlContext.getJCRSession(aggregationState.getRepository());
                is = getNodedataAsStream(resourceHandle, session, response);
                if (null != is) {
                    // don't reset any existing status code, see MAGNOLIA-2005
                    // response.setStatus(HttpServletResponse.SC_OK);
                    sendUnCompressed(is, response);
                    IOUtils.closeQuietly(is);
                    return;
                }
            }
            catch (IOException e) {
                // don't log at error level since tomcat tipically throws a
                // org.apache.catalina.connector.ClientAbortException if the user stops loading the page
                log.debug("Exception while dispatching resource " + e.getClass().getName() + ": " + e.getMessage(), e);
                return;
            }
            catch (Exception e) {
                log.error("Exception while dispatching resource  " + e.getClass().getName() + ": " + e.getMessage(), e);
                return;
            }
            finally {
                IOUtils.closeQuietly(is);
            }
        }
        log.debug("Resource not found, redirecting request for [{}] to 404 URI", request.getRequestURI());

        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            log.info("Unable to redirect to 404 page for {}, response is already committed", request.getRequestURI());
        }

    }

    /**
     * Send data as is.
     * @param is Input stream for the resource
     * @param response HttpServletResponse as received by the service method
     * @throws IOException standard servlet exception
     */
    private void sendUnCompressed(InputStream is, HttpServletResponse response) throws IOException {
        ServletOutputStream os = response.getOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        IOUtils.closeQuietly(os);
    }

    private InputStream getNodedataAsStream(String path, Session session, HttpServletResponse res) {

        log.debug("getNodedataAstream for path \"{}\"", path);

        try {
            Node atom = session.getNode(path);
            if (atom != null) {
                if (atom.hasProperty(MgnlNodeType.JCR_DATA)) {
                    Property sizeProperty = atom.getProperty("size");
                    String sizeString = sizeProperty == null ? "" : sizeProperty.getString();
                    if (NumberUtils.isNumber(sizeString)) {
                        res.setContentLength(Integer.parseInt(sizeString));
                    }
                }
                Property streamProperty = atom.getProperty(MgnlNodeType.JCR_DATA);
                return streamProperty.getStream();
            }

            log.warn("Resource not found: [{}]", path);

        }
        catch (PathNotFoundException e) {
            log.warn("Resource not found: [{}]", path);
        }
        catch (RepositoryException e) {
            log.error("RepositoryException while reading Resource [" + path + "]", e);
        }
        return null;
    }

}
