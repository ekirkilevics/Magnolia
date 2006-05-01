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
 *
 */
public class SimpleSearchUtil {
    
    public static SearchQuery getSimpleSearchQuery(String searchStr, SearchConfig config) {
        SearchQuery query = new SearchQuery();
        query.setRootExpression(getSimpleSearchExpression(searchStr, config));
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
        else{
            return null;
        }
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
