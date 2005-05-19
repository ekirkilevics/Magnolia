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
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PropertyType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;


/**
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class SearchResultSnippetTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(SearchResultSnippetTag.class);

    /**
     * Start level.
     */
    private Content page;

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
     * Setter for <code>query</code>.
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Setter for <code>chars</code>.
     * @param chars The chars to set.
     */
    public void setChars(int chars) {
        this.chars = chars;
    }

    /**
     * Setter for <code>maxSnippets</code>.
     * @param maxSnippets The maxSnippets to set.
     */
    public void setMaxSnippets(int maxSnippets) {
        this.maxSnippets = maxSnippets;
    }

    /**
     * Setter for <code>page</code>.
     * @param page The page to set.
     */
    public void setPage(Content page) {
        this.page = page;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
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

        if (log.isDebugEnabled()) {
            log.debug("collecting snippets");
        }

        Collection snippets = new ArrayList();
        String[] searchTerms = StringUtils.split(this.query);

        Collection paragraphCollections = this.page.getChildren(ItemType.CONTENTNODE);

        Iterator iterator = paragraphCollections.iterator();
        outer : while (iterator.hasNext()) {
            Content paragraphCollection = (Content) iterator.next();

            Collection paragraphs = paragraphCollection.getChildren();

            Iterator parIterator = paragraphs.iterator();
            while (parIterator.hasNext()) {
                Content paragraph = (Content) parIterator.next();

                if (log.isDebugEnabled()) {
                    log.debug("Iterating on paragraph " + paragraph);
                }

                Collection properties = paragraph.getNodeDataCollection();

                Iterator dataIterator = properties.iterator();
                while (dataIterator.hasNext()) {
                    NodeData property = (NodeData) dataIterator.next();
                    if (property.getType() != PropertyType.BINARY) {

                        String resultString = property.getString();

                        if (log.isDebugEnabled()) {
                            log.debug("Iterating on property " + property.getName());
                            log.debug("Property value is " + resultString);
                        }

                        // a quick and buggy way to avoid configuration properties, we should allow the user to
                        // configure a list of nodeData to search for...
                        if (resultString.length() < 20) {
                            continue;
                        }

                        for (int j = 0; j < searchTerms.length; j++) {
                            String searchTerm = StringUtils.lowerCase(searchTerms[j]);

                            // exclude keywords and words with less than 2 chars
                            if (!ArrayUtils.contains(SimpleSearchTag.KEYWORDS, searchTerm) && searchTerm.length() > 2) {

                                if (log.isDebugEnabled()) {
                                    log.debug("Looking for search term [" + searchTerm + "] in [" + resultString + "]");
                                }

                                // first check, avoid using heavy string replaceAll operations if the search term is not
                                // there
                                if (!StringUtils.contains(resultString, searchTerm)) {
                                    continue;
                                }

                                // strips out html tags using a regexp
                                resultString = resultString.replaceAll("\\<.*?\\>", "");

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
                                    snippet.append(searchTerm);
                                    snippet.append("</strong>");
                                    snippet.append(StringUtils.substring(resultString, posEnd, to));

                                    if (from > 0) {
                                        snippet.insert(0, "... ");
                                    }
                                    if (to < resultString.length()) {
                                        snippet.append("... ");
                                    }

                                    if (log.isDebugEnabled()) {
                                        log.debug("Search term found, adding snippet " + snippet);
                                    }

                                    snippets.add(snippet);
                                    if (snippets.size() >= this.maxSnippets) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Maximum number of snippets ("
                                                + this.maxSnippets
                                                + ") reached, exiting");
                                        }
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
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.query = null;
        this.page = null;
        this.chars = 100;
        this.maxSnippets = 3;
        super.release();
    }

}
