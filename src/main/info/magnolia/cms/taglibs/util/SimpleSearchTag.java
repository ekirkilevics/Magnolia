/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.util.Resource;

import java.text.MessageFormat;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * <p>
 * A simple tag which allows searching in all the site content with a "natural language" query. It simply strips all the
 * reserved chars from input string, build an xpath query and feed Magnolia QueryManager.
 * </p>
 * <p>
 * By defaults search terms are ANDed, but it also supports using the AND or OR keywords in the query string. Search is
 * not case sensitive and it's performed on any non-binary property.
 * </p>
 * <p>
 * A collection on Content (page) objects is added to the specified scope with the specified name.
 * </p>
 * <p>
 * Tipical usage:
 * </p>
 *
 * <pre>
 *   &lt;cmsu:simplesearch query="${param.search}" startLevel="3" var="results" />
 *   &lt;c:forEach items="${results}">
 *     &lt;a href="${pageContext.request.contextPath}${node.handle}.html">${node.title}&lt;/a>
 *   &lt;/c:forEach>
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class SimpleSearchTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Reserved chars, stripped from query.
     */
    private static final String RESERVED_CHARS = "()[]{}<>:/\\@*?=\"'&"; //$NON-NLS-1$

    /**
     * keywords.
     */
    static final String[] KEYWORDS = new String[]{"and", "or"}; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SimpleSearchTag.class);

    /**
     * Start level.
     */
    private int startLevel;

    /**
     * Query, natural language.
     */
    private String query;

    /**
     * Variable name for results.
     */
    private String var;

    /**
     * Tag attribute. Scope for the declared variable. Can be <code>page</code>, <code>request</code>,
     * <code>session</code> or <code>application</code><code></code>.
     */
    private int scope = PageContext.PAGE_SCOPE;

    /**
     * Setter for <code>query</code>.
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Setter for <code>var</code>.
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Setter for <code>scope</code>.
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        if ("request".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.REQUEST_SCOPE;
        }
        else if ("session".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.SESSION_SCOPE;
        }
        else if ("application".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.APPLICATION_SCOPE;
        }
        else {
            // default
            this.scope = PageContext.PAGE_SCOPE;
        }
    }

    /**
     * Setter for <code>startLevel</code>.
     * @param startLevel The startLevel to set.
     */
    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    public int doStartTag() throws JspException {

        String queryString = generateXPathQuery();

        if (queryString == null) {
            log.debug("A valid query could not be built, skipping"); //$NON-NLS-1$
            return EVAL_PAGE;
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing xpath query " + queryString); //$NON-NLS-1$
        }

        Query q;
        try {
            q = SessionAccessControl.getQueryManager((HttpServletRequest) this.pageContext.getRequest()).createQuery(
                queryString,
                "xpath"); //$NON-NLS-1$

            QueryResult result = q.execute();

            pageContext.setAttribute(var, result.getContent(), scope);
        }
        catch (Exception e) {
            log.error(MessageFormat.format(
                "{0} caught while parsing query for search term [{1}] - query is [{2}]: {3}", //$NON-NLS-1$
                new Object[]{e.getClass().getName(), this.query, queryString, e.getMessage()}), e);
        }

        return EVAL_PAGE;
    }

    /**
     * Split search terms and build an xpath query in the form:
     * <code>//*[@jcr:primaryType='mgnl:content']/\*\/\*[jcr:contains(., 'first') or jcr:contains(., 'second')]</code>
     * @return valid xpath expression or null if the given query doesn't contain at least one valid search term
     */
    protected String generateXPathQuery() {

        String startPath = null;

        // search only in a specific subtree
        if (this.startLevel != 0) {
            try {
                Content activePage = Resource.getActivePage((HttpServletRequest) this.pageContext.getRequest());
                if (activePage != null) {
                    startPath = StringUtils.strip(activePage.getAncestor(this.startLevel).getHandle(), "/"); //$NON-NLS-1$
                }
            }
            catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            }
        }

        // strip reserved chars and split
        String[] tokens = StringUtils.split(StringUtils.lowerCase(StringUtils.replaceChars(
            this.query,
            RESERVED_CHARS,
            null)));

        // null input string?
        if (tokens == null) {
            return null;
        }

        StringBuffer xpath = new StringBuffer(tokens.length * 20);
        if (StringUtils.isNotEmpty(startPath)) {
            xpath.append(startPath);
        }
        xpath.append("//*[@jcr:primaryType=\'mgnl:content\']/*/*["); //$NON-NLS-1$

        String joinOperator = "and"; //$NON-NLS-1$
        boolean emptyQuery = true;

        for (int j = 0; j < tokens.length; j++) {
            String tkn = tokens[j];
            if (ArrayUtils.contains(KEYWORDS, tkn)) {
                joinOperator = tkn;
            }
            else {
                if (!emptyQuery) {
                    xpath.append(" "); //$NON-NLS-1$
                    xpath.append(joinOperator);
                    xpath.append(" "); //$NON-NLS-1$
                }
                xpath.append("jcr:contains(., '"); //$NON-NLS-1$
                xpath.append(tkn);
                xpath.append("')"); //$NON-NLS-1$
                emptyQuery = false;
            }

        }

        xpath.append("]"); //$NON-NLS-1$

        // if no valid search terms are added don't return a catch-all query
        if (emptyQuery) {
            return null;
        }

        return xpath.toString();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.query = null;
        this.var = null;
        this.scope = PageContext.PAGE_SCOPE;
        this.startLevel = 0;
        super.release();
    }

}
