/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.filters;

import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.SimpleUrlPattern;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class ServletDispatchingFilter extends AbstractMgnlFilter {

    private String servletName;
    private String servletClass;
    private Collection mappings;
    private Map parameters;
    private String comment;
    
    private HttpServlet servlet;
    private Collection mappingPatterns;
    
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ServletDispatchingFilter.class);
    
    public ServletDispatchingFilter() {
        mappings = new LinkedList();
    }
    
    /**
     * Initializes the servlet and its mappings. ServletConfig is wrapped to take init parameters into account.
     */
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        
        if (servletClass != null) {
            try {
                servlet = (HttpServlet) ClassUtil.newInstance(servletClass);
                servlet.init(new ServletConfig() {

                    public String getInitParameter(String name) {
                        return (String) parameters.get(name);
                    }
                            
                    public Enumeration getInitParameterNames() {
                        return new Enumeration() {
                            
                            private Iterator iter = parameters.keySet().iterator();
                            
                            public boolean hasMoreElements() {
                                return iter.hasNext();
                            }

                            public Object nextElement() {
                                return iter.next();
                            }
                        };
                    }

                    public ServletContext getServletContext() {
                        return filterConfig.getServletContext();
                    }

                    public String getServletName() {
                        return servletName;
                    }
                    
                });
            }
            catch (Throwable e) {
                log.error("Unable to load servlet "
                    + servletClass
                    + " due to a "
                    + e.getClass().getName()
                    + " exception", e);
            }
            
            servlet.init();
        }

        if (mappings != null) {
            mappingPatterns = CollectionUtils.collect(mappings, new Transformer() {
                    public Object transform(Object input) {
                        if (input instanceof MappingDefinition) {
                            final String mapping = StringUtils.removeEnd(((MappingDefinition) input).getValue(), "*");
                            final String encodedString = SimpleUrlPattern.getEncodedString(mapping);
                            
                            return Pattern.compile(encodedString);
                        }
                        return null;
                    }
                });
        }
    }
    
    /**
     * Bypasses if the current request does not match any of the mappings of the servlet.
     * Explicit bypasses defined in the bypasses content node of this filter are taken into account as well.
     */
    public boolean bypasses(HttpServletRequest request) {
        final String uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
        return determineMatchingEnd(uri) < 0 || super.bypasses(request);
    }
    
    /**
     * Determines the index of the first pathInfo character. If the uri does not match any mapping this method returns -1.
     * @param uri
     * @return
     */
    protected int determineMatchingEnd(String uri) {
        for (Iterator iter = mappingPatterns.iterator(); iter.hasNext();) {
            final Matcher matcher = ((Pattern) iter.next()).matcher(uri);
            
            if (matcher.find())
                return matcher.end();
        }
        
        return -1;
    }

    /**
     * Dispatches the request to the servlet if not already bypassed. The request is wrapped for properly setting the pathInfo.
     */
    public void doFilter(final HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        
        log.debug("Dispatching to servlet " + getServletClass());
        servlet.service(new HttpServletRequestWrapper(request) {
                public String getPathInfo() {
                    final String uri = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
                    final String pathInfo = StringUtils.substring(uri, determineMatchingEnd(uri));
                    
                    // according to the servlet spec the pathInfo should contain a leading slash
                    return (pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo);
                }
            }, response);
    }
    
    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getServletClass() {
        return servletClass;
    }
    
    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }
    
    public Collection getMappings() {
        return mappings;
    }
    
    public void setMappings(Collection mappings) {
        this.mappings = mappings;
    }

    public void addMapping(MappingDefinition mapping) {
        getMappings().add(mapping);
    }
    
    public Map getParameters() {
        return parameters;
    }
    
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    public static class MappingDefinition {
    
        private String value;
        
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
    
}
