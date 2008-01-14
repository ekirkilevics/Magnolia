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
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.util.Resource;
import info.magnolia.context.MgnlContext;

import java.text.MessageFormat;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p/> A simple tag which allows searching in all the site content with a "natural language" query. It simply strips
 * all the reserved chars from input string, build an xpath query and feed Magnolia QueryManager.
 * </p>
 * <p/> By defaults search terms are ANDed, but it also supports using the AND or OR keywords in the query string.
 * Search is not case sensitive and it's performed on any non-binary property.
 * </p>
 * <p/> A collection on Content (page) objects is added to the specified scope with the specified name.
 * </p>
 * <p/> Tipical usage:
 * </p>
 * <p/>
 *
 * <pre>
 *   &lt;cmsu:simplesearch query="${param.search}" startLevel="3" var="results" />
 *   &lt;c:forEach items="${results}">
 *     &lt;a href="${pageContext.request.contextPath}${node.handle}.html">${node.title}&lt;/a>
 *   &lt;/c:forEach>
 * </pre>
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
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
    private static Logger log = LoggerFactory.getLogger(SimpleSearchTag.class);

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
     * The repository we search in. Default is the website repository
     */
    private String repository = ContentRepository.WEBSITE;


    private String itemType = ItemType.CONTENT.getSystemName();

    /**
     * Seach for substring. Means contains(. '*str*'). This will decrease performance.
     */
    private boolean supportSubstringSearch = false;

    /**
     * The path we search in.
     */
    private String startPath;

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
            if (log.isDebugEnabled()) {
                log.debug("A valid query could not be built, skipping"); //$NON-NLS-1$
            }
            return EVAL_PAGE;
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing xpath query " + queryString); //$NON-NLS-1$
        }

        Query q;
        try {
            q = MgnlContext.getQueryManager(repository).createQuery(queryString, "xpath"); //$NON-NLS-1$

            QueryResult result = q.execute();

            pageContext.setAttribute(var, result.getContent(itemType), scope);
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

        // search only in a specific subtree
        if (this.startLevel != 0) {
            try {
                Content activePage = Resource.getActivePage();
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
        xpath.append("//*[@jcr:primaryType=\'mgnl:content\']//*["); //$NON-NLS-1$

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
                if(supportSubstringSearch){
                    xpath.append("*");
                    xpath.append(tkn);
                    xpath.append("*");
                }
                else{
                    xpath.append(tkn);
                }

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


    public String getRepository() {
        return this.repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public boolean isSupportSubstringSearch() {
        return this.supportSubstringSearch;
    }

    public void setSupportSubstringSearch(boolean supportSubstringSearch) {
        this.supportSubstringSearch = supportSubstringSearch;
    }


    /**
     * @return the itemType
     */
    public String getItemType() {
        return this.itemType;
    }


    /**
     * @param itemType the itemType to set
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }


    public String getStartPath() {
        return this.startPath;
    }


    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

}
