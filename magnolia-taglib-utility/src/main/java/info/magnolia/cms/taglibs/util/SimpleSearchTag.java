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
 * A simple tag which allows searching in all the site content with a "natural language" query.
 * It simply strips all the reserved chars from input string, build an xpath query and feed Magnolia QueryManager.
 * By defaults search terms are ANDed, but it also supports using the AND or OR keywords in the query string.
 * Search is not case sensitive and it's performed on any non-binary property.
 * A collection on Content (page) objects is added to the specified scope with the specified name.
 *
 * @jsp.tag name="simpleSearch" body-content="empty"
 * @jsp.tag-example
 * <cmsu:simplesearch query="${param.search}" var="results" />
 *   <c:forEach items="${results}" var="page">
 *   <a href="${pageContext.request.contextPath}${page.handle}.html">${page.title}</a>
 * </c:forEach>
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

    private static final Logger log = LoggerFactory.getLogger(SimpleSearchTag.class);

    private int startLevel;
    private String query;
    private String var;
    private String repository = ContentRepository.WEBSITE;
    private String itemType = ItemType.CONTENT.getSystemName();
    private boolean supportSubstringSearch = false;
    private boolean useSimpleJcrQuery = true;
    private String startPath;
    private int scope = PageContext.PAGE_SCOPE;

    /**
     * Query to execute (e.g. "magnolia AND cms OR info")
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * The search results (a collection of Content nodes (pages)) will be added to the pagecontext using this name.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Scope for the variable. Can be "page" (default), "request", "session", "application".
     * @jsp.attribute required="false" rtexprvalue="true"
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
     * The start level for search, defaults to 0. Can be used to limit the search only to the current website tree.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public int doStartTag() throws JspException {

        final String queryString = useSimpleJcrQuery ? generateSimpleQuery(query) : generateComplexXPathQuery();

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
     * This generates a simple jcr:contains query.
     *
     * @see "6.6.5.2 jcr:contains Function" from the JCR Spec (pages 110-111) for details.
     */
    protected String generateSimpleQuery(String input) {
        // jcr and xpath escaping :
        final String escapedQuery = input.replaceAll("'", "\\\\''");
        return "//*[@jcr:primaryType='mgnl:content']//*[jcr:contains(., '"+ escapedQuery +"')]";
    }

    /**
     * @deprecated as from 3.5.5, this query is deemed to complex and not properly working, since it
     * forces a search on non-indexed word. The better generateSimpleQuery() method is recommened.
     */
    protected String generateComplexXPathQuery() {
        return generateXPathQuery();
    }

    /**
     * Split search terms and build an xpath query in the form:
     * <code>//*[@jcr:primaryType='mgnl:content']/\*\/\*[jcr:contains(., 'first') or jcr:contains(., 'second')]</code>
     * @return valid xpath expression or null if the given query doesn't contain at least one valid search term
     *
     * @deprecated as from 3.5.5, this query is deemed to complex and not properly working, since it
     * forces a search on non-indexed word. The better generateSimpleQuery() method is recommened.
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

    /**
     * The repository we search in. Default is website repository.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    public boolean isSupportSubstringSearch() {
        return this.supportSubstringSearch;
    }

    /**
     * Search for substrings too. This can decrease performance. Default value is false.
     * @deprecated not used when useSimpleJcrQuery is set to true.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setSupportSubstringSearch(boolean supportSubstringSearch) {
        this.supportSubstringSearch = supportSubstringSearch;
    }

    /**
     * Set this attribute to false to generate the search query as it was generated until Magnolia 3.5.4
     * (which will force a search on non-indexed word, which usually leads in less good results).
     * As from 3.5.5, this is true by default, and generates simpler and better queries.
     * See "6.6.5.2 jcr:contains Function" from the JCR Spec (pages 110-111) for details.
     * @jsp.attribute required="false" rtexprvalue="true" type="boolean"
     */
    public void setUseSimpleJcrQuery(boolean useSimpleJcrQuery) {
        this.useSimpleJcrQuery = useSimpleJcrQuery;
    }

    /**
     * @return the itemType
     */
    public String getItemType() {
        return this.itemType;
    }

    /**
     * The itemTypes search/returned by this tag. Default is mgnl:content which is used for pages.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }


    public String getStartPath() {
        return this.startPath;
    }

    /**
     * The path we search in.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

}
