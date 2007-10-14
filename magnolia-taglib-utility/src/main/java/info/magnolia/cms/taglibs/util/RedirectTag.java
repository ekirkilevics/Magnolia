/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Redirects to the first child page. If the server is an authoring instance or magnolia and the preview mode is not
 * active the tag will simply add to the pagecontext a variable named from the <code>var</code> attribute containing
 * the path of the child page.
 * </p>
 * <p>
 * A typical requirement is to have pages with no content which will simply redirect to a child page: using this tag you
 * can easily build a "redirect" template and use it for empty pages:
 * </p>
 *
 * <pre>
 *                         Title                    Template               Mod. Date
 * -----------------------^----------------^-------^----------------------^---------------
 * - siteroot             -                o       redirect                05-01-01
 *   - it                 -                o       redirect                05-01-01
 *     + home             Home page        o       home                    05-01-01
 *
 * </pre>
 *
 * <p>
 * This tag should be put <strong>before</strong> any other tag or include in the page, since response should not be
 * committed yet for it to work.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;cmsu:redirect var="destpage" />
 *
 * This page has no content and it will redirect to
 * &lt;a href="${pageContext.request.contextPath}${destpage}">${destpage}&lt;/a> in a public instance.
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Id$
 * @since 2.2
 */
public class RedirectTag extends BodyTagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(RedirectTag.class);

    /**
     * Name for the variable which will contain the URL of the page this tag will redirect to.
     */
    private String var;

    /**
     * Setter for the <code>var</code> tag parameter.
     * @param var Name for the variable which will contain the URL of the page this tag will redirect to
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String location = getRedirectLocation(request);

        Content activePage = Resource.getActivePage();

        // on public servers, during preview or when the user can't edit the page, just send the redirect
        if (!Server.isAdmin() || Resource.showPreview() || !activePage.isGranted(Permission.SET)) {
            if (location != null) {
                try {
                    ((HttpServletResponse) pageContext.getResponse()).sendRedirect(request.getContextPath() + location);
                }
                catch (IOException e) {
                    log.error("Could not redirect to first child HTML page: " + e.getMessage()); //$NON-NLS-1$
                }
            }
        }
        else if (StringUtils.isNotBlank(var)) {
            request.setAttribute(var, location);
        }
        return super.doStartTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.var = null;
        super.release();
    }

    /**
     * Returns the locationto which we intend to redirect.
     * @param request The HTTP request.
     * @return A URI if a child page is available, or null.
     */
    private String getRedirectLocation(HttpServletRequest request) {
        Content page = Resource.getActivePage();
        Iterator it = page.getChildren().iterator();
        if (it.hasNext()) {
            Content c = (Content) it.next();
            return c.getHandle() + '.' + Server.getDefaultExtension();
        }

        return null;
    }

}
