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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * Tags that executes a query in a Magnolia repository.
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class QueryTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * The query result will be set in the pageContext using this name.
     */
    private String var;

    /**
     * The query to run.
     */
    private String query;

    /**
     * The repository. Defaults to ContentRepository.WEBSITE
     */
    private String repository = ContentRepository.WEBSITE;

    /**
     * Node type. Defaults to mgnl:content.
     */
    private String nodeType = ItemType.CONTENT.getSystemName();

    /**
     * Query type: SQL or XPATH. Defaults to Query.XPATH.
     */
    private String type = Query.XPATH;

    /**
     * Setter for <code>query</code>.
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Setter for <code>type</code>.
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Setter for <code>var</code>.
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * Setter for <code>repository</code>.
     * @param repository The repository to set.
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Setter for <code>nodeType</code>.
     * @param nodeType The nodeType to set.
     */
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {

        Query q;
        try {
            q = MgnlContext.getQueryManager(repository).createQuery(query, type);
        }
        catch (InvalidQueryException e) {
            throw new NestableRuntimeException(e);
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }

        QueryResult queryResult;
        try {
            queryResult = q.execute();
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }
        Collection result = queryResult.getContent(nodeType);
        pageContext.setAttribute(var, result);

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        super.release();
        this.var = null;
        this.query = null;
        this.type = Query.XPATH;
        this.repository = ContentRepository.WEBSITE;
        this.nodeType = ItemType.CONTENT.getSystemName();
    }
}
