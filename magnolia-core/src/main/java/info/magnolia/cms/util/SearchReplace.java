/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.cms.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link info.magnolia.cms.util.ContentUtil.Visitor} which allows simple search/replace functionality.
 *
 * For sample usage, see {@link SearchReplaceTest}.
 *
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SearchReplace implements ContentUtil.Visitor {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SearchReplace.class);

    final String propertyNamePattern;
    final String search;
    final String replace;

    private final Pattern searchPattern;

    /**
     * A search and replace which searches for literal occurrences of <code>search</code>.
     * @see Pattern#LITERAL
     */
    public SearchReplace(String propertyNamePattern, String search, String replace) {
        this(propertyNamePattern, search, replace, Pattern.LITERAL);
    }

    /**
     * A search and replace which can use parameterized substitution; use 0 for the default java.util.Pattern behavior.
     */
    public SearchReplace(String propertyNamePattern, String search, String replace, int regexFlags) {
        this.propertyNamePattern = propertyNamePattern;
        this.search = search;
        this.replace = replace;

        this.searchPattern = Pattern.compile(search, regexFlags);
    }

    public void visit(Content node) throws Exception {
        final Collection<NodeData> props = node.getNodeDataCollection(propertyNamePattern);
        for (NodeData prop : props) {
            if (prop.isExist()) {
                final String oldValue = prop.getString();
                final StringBuffer newValue = new StringBuffer();
                final Matcher matcher = searchPattern.matcher(oldValue);
                int count = 0;
                while (matcher.find()) {
                    final String group = matcher.group();
                    matcher.appendReplacement(newValue, replace);
                    count++;
                }
                matcher.appendTail(newValue);
                onPropertyVisit(prop, count, oldValue, newValue);
                prop.setValue(newValue.toString());
            }
        }
    }

    protected void onPropertyVisit(NodeData prop, int count, String oldValue, StringBuffer newValue) {
        log.debug(String.format("Found %d occurences of %s at %s", count, search, prop.getHandle()));
        log.debug(String.format("   old value:\n %s\n\nnew value:\n%s", oldValue, newValue.toString()));
    }

}
