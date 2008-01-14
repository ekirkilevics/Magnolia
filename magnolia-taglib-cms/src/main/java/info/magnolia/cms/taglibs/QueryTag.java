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
