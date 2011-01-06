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
package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.query.SearchQuery;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.gui.query.SearchQueryOperator;
import info.magnolia.cms.gui.query.StringSearchQueryParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 */
public class SimpleSearchUtil {

    /**
     * Creates an orified query based on the passed config
     * @param searchStr
     * @param config
     * @return
     */
    public static SearchQuery getSimpleSearchQuery(String searchStr, SearchConfig config) {
        SearchQuery query = new SearchQuery();
        query.setRootExpression(getSimpleSearchExpression(searchStr, config));
        return query;
    }

    /**
     * Creates a query using the special search field name '*'
     * @param searchStr
     * @return
     */
    public static SearchQuery getSimpleSearchQuery(String searchStr) {
        SearchQuery query = new SearchQuery();
        if (StringUtils.isNotEmpty(searchStr)) {
            SearchQueryExpression exp = new StringSearchQueryParameter(
                "*",
                searchStr,
                StringSearchQueryParameter.CONTAINS);
            query.setRootExpression(exp);
        }
        return query;
    }
    
    /**
     * Return the expression build by a simple search
     */
    public static SearchQueryExpression getSimpleSearchExpression(String searchStr, SearchConfig config) {
        if (StringUtils.isNotEmpty(searchStr)) {
            List expressions = new ArrayList();
            for (Iterator iter = config.getControlDefinitions().iterator(); iter.hasNext();) {
                SearchControlDefinition def = (SearchControlDefinition) iter.next();
                expressions.add(new StringSearchQueryParameter(
                    def.getColumn(),
                    searchStr,
                    StringSearchQueryParameter.CONTAINS));
            }
            return chainExpressions(expressions, SearchQueryOperator.OR);
        }

        return null;
    }

    public static SearchQueryExpression chainExpressions(Collection expressions, String operator) {
        SearchQueryExpression expr = null;
        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
            SearchQueryExpression newExpr = (SearchQueryExpression) iter.next();
            if (expr == null) {
                expr = newExpr;
            }
            else {
                SearchQueryOperator opt = new SearchQueryOperator(operator);
                opt.setleft(newExpr);
                opt.setRight(expr);
                expr = opt;
            }
        }
        return expr;
    }



}
