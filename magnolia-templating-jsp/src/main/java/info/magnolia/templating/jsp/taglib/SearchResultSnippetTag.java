/**
 * This file Copyright (c) 2012-2012 Magnolia International
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
package info.magnolia.templating.jsp.taglib;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.jcr.util.NodeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;


/**
 * Output a set of snippets taken from any paragraph in the given page mathing any of the search term.
 * @jsp.tag name="searchResultSnippet" body-content="empty"
 * @jsp.tag-example
 * <pre>
 * &lt;cmsu:simplesearch query="${param.search}" var="results" /&gt;
 * &lt;c:forEach items="${results}" var="page"&gt;
 *   &lt;cmsu:searchResultSnippet query="${param.search}" page="${page}" /&gt;
 * &lt;/c:forEach&gt;
 *</pre>
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
@Tag(name="searchResultSnippet", bodyContent=BodyContent.EMPTY)

public class SearchResultSnippetTag extends TagSupport {

    private static final long serialVersionUID = -2788581584059109175L;

    private static final Pattern HTML_STRIP = Pattern.compile("<.*?>", Pattern.DOTALL);

    private static final Logger log = LoggerFactory.getLogger(SearchResultSnippetTag.class);

    /**
     * Start level.
     */
    private Node page;

    /**
     * Query, natural language.
     */
    private String query;

    /**
     * Number of chars to include in result.
     */
    private int chars = 100;

    /**
     * Maximum number of snippets to include in result.
     */
    private int maxSnippets = 3;

    /**
     * Search query.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Number of characters to include in search snippets. Default is 100.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setChars(int chars) {
        this.chars = chars;
    }

    /**
     * Maximum number of snippets to print out.
     * @jsp.attribute required="false" rtexprvalue="true" type="int"
     */
    public void setMaxSnippets(int maxSnippets) {
        this.maxSnippets = maxSnippets;
    }

    /**
     * A Content node of type mgnl:content (a magnolia page), typically returned by the simpleSearch tag.
     * @jsp.attribute required="true" rtexprvalue="true" type="info.magnolia.cms.core.Content"
     */
    public void setPage(Node page) {
        this.page = page;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        JspWriter out = this.pageContext.getOut();
        try {
            Iterator iterator = getSnippets().iterator();
            while (iterator.hasNext()) {
                out.println(iterator.next());
            }
        }
        catch (IOException e) {
            // should never happen
            throw new NestableRuntimeException(e);
        }
        return EVAL_PAGE;
    }

    /**
     * Extract a collection of snippets from any paragraph in the given page.
     * @return a collection of Strings.
     * @todo avoid overlapping snippets (use regexp insted of simple indexOfs)
     * @todo only extract snippets from user-configured properties
     * @todo abbreviate on whitespace and puntuation, detect start of sentences
     * @todo replace ampersand in regexp
     * @todo break methods and write junits
     */
    public Collection getSnippets() {

        log.debug("collecting snippets");

        Collection snippets = new ArrayList();
        String[] searchTerms = StringUtils.split(this.query);

        try{
        Iterator<Node> iterator = NodeUtil.getNodes(this.page, MgnlNodeType.NT_CONTENTNODE).iterator();

        outer : while (iterator.hasNext()) {
            Node paragraphCollection = iterator.next();

            Iterator<Node> parIterator = NodeUtil.getNodes(paragraphCollection, MgnlNodeType.NT_CONTENTNODE).iterator();
            while (parIterator.hasNext()) {
                Node paragraph = parIterator.next();

                log.debug("Iterating on paragraph {}", paragraph);

                Iterator dataIterator = paragraph.getProperties();

                while (dataIterator.hasNext()) {
                    Property property = (Property) dataIterator.next();
                    if (property.getType() != PropertyType.BINARY) {

                        String resultString = property.getString();

                        log.debug("Iterating on property {}", property.getName());
                        log.debug("Property value is {}", resultString);

                        // a quick and buggy way to avoid configuration properties, we should allow the user to
                        // configure a list of nodeData to search for...
                        if (resultString.length() < 20) {
                            continue;
                        }

                        for (int j = 0; j < searchTerms.length; j++) {
                            String searchTerm = StringUtils.lowerCase(searchTerms[j]);

                            // exclude keywords and words with less than 2 chars
                            if (!ArrayUtils.contains(new String[]{"and", "or"}, searchTerm) && searchTerm.length() > 2) {

                                log.debug("Looking for search term [{}] in [{}]", searchTerm, resultString);

                                // first check, avoid using heavy string replaceAll operations if the search term is not
                                // there
                                if (!StringUtils.contains(resultString.toLowerCase(), searchTerm)) {
                                    continue;
                                }

                                // strips out html tags using a regexp
                                resultString = stripHtmlTags(resultString);

                                // only get first matching keyword
                                int pos = resultString.toLowerCase().indexOf(searchTerm);
                                if (pos > -1) {

                                    int posEnd = pos + searchTerm.length();
                                    int from = (pos - chars / 2);
                                    if (from < 0) {
                                        from = 0;
                                    }

                                    int to = from + chars;
                                    if (to > resultString.length()) {
                                        to = resultString.length();
                                    }

                                    StringBuffer snippet = new StringBuffer();

                                    snippet.append(StringUtils.substring(resultString, from, pos));
                                    snippet.append("<strong>");
                                    snippet.append(StringUtils.substring(resultString, pos, posEnd));
                                    snippet.append("</strong>");
                                    snippet.append(StringUtils.substring(resultString, posEnd, to));

                                    if (from > 0) {
                                        snippet.insert(0, "... ");
                                    }
                                    if (to < resultString.length()) {
                                        snippet.append("... ");
                                    }

                                    log.debug("Search term found, adding snippet {}", snippet);

                                    snippets.add(snippet);
                                    if (snippets.size() >= this.maxSnippets) {

                                        log.debug("Maximum number of snippets ({}) reached, exiting",
                                            Integer.toString(this.maxSnippets));

                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }
        return snippets;
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param resultString
     * @return
     */
    protected String stripHtmlTags(String input) {
        return HTML_STRIP.matcher(input).replaceAll("");
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    @Override
    public void release() {
        this.query = null;
        this.page = null;
        this.chars = 100;
        this.maxSnippets = 3;
        super.release();
    }

}
