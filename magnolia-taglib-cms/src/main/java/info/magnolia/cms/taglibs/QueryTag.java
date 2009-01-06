/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.context.MgnlContext;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.exception.NestableRuntimeException;


/**
 * Executes a query on a Magnolia repository.
 * @jsp.tag name="query" body-content="empty"
 *
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
     * The query.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Query type: "sql" or "xpath". Defaults to xpath.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * The result for this query (Collection of content objects) will be added to the pageContext with this name.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * The repository to execute this query on. Defaults to "website".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * Nodetype for nodes to return. Defaults to "mgnl:content".
     * @jsp.attribute required="false" rtexprvalue="true"
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
