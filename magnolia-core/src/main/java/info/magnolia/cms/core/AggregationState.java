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
package info.magnolia.cms.core;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * Aggregates the necessary information to render content. Filled-in progressively by various filters.
 *
 * @version $Revision: $ ($Author: $)
 */
public class AggregationState {
    private String characterEncoding;
    private String originalURI;
    private String originalURL;
    private String originalBrowserURI;
    private String originalBrowserURL;
    private String currentURI;
    private String queryString;
    private String extension;
    private File file;
    private String handle;
    private Content mainContent;
    private Content currentContent;
    private String repository;
    private String selector;
    private String[] selectors = new String[0];
    private String templateName;
    private Locale locale;
    private boolean isPreviewMode;
    private String channel;

    public void setOriginalURI(String originalURI) {
        final String strippedURI = stripContextPathIfExists(originalURI);
        if (this.originalURI != null && !this.originalURI.equals(strippedURI)) {
            throw new IllegalStateException("Original URI can only be set once ! Existing value is \"" + this.originalURI + "\", tried to replace it with \"" + strippedURI + "\"");
        }
        this.originalURI = strippedURI;
    }

    public void setOriginalBrowserURI(String originalBrowserURI) {
        final String strippedURI = stripContextPathIfExists(originalBrowserURI);
        if (this.originalBrowserURI != null && !this.originalBrowserURI.equals(strippedURI)) {
            throw new IllegalStateException("Original URI can only be set once ! Existing value is \"" + this.originalURI + "\", tried to replace it with \"" + strippedURI + "\"");
        }
        this.originalBrowserURI= strippedURI;
    }

    public void setCurrentURI(String currentURI) {
        this.currentURI = stripContextPathIfExists(currentURI);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Returns the original request query string.
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * @return the URI of the current request, decoded and without the context path.
     * This URI might have been modified by various filters.
     */
    public String getCurrentURI() {
        return currentURI;
    }

    public String getCharacterEncoding() {
        if (characterEncoding == null) {
            throw new IllegalStateException("Character encoding hasn't been setup in AggregationState yet !");
        }
        return characterEncoding;
    }


    // -- just plain getters and setters below:
    /**
     * Returns the original request URI, decoded and without the context path.
     * Can never be modified.
     */
    public String getOriginalURI() {
        return originalURI;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public String getOriginalBrowserURI() {
        return originalBrowserURI;
    }

    public String getOriginalBrowserURL() {
        return originalBrowserURL;
    }

    public void setOriginalBrowserURL(String originalBrowserURL) {
        this.originalBrowserURL = originalBrowserURL;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Content getMainContent() {
        return mainContent;
    }

    public void setMainContent(Content mainContent) {
        this.mainContent = mainContent;
    }

    public Content getCurrentContent() {
        return currentContent;
    }

    public void setCurrentContent(Content currentContent) {
        this.currentContent = currentContent;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * A selector is the part between the first {@link info.magnolia.cms.core.Path#SELECTOR_DELIMITER} and the extension of an URI.
     * I.e. given a URI like {@code http://myserver/mypage~x~foo=bar~.html} the entire selector is {@code ~x~foo=bar~}. A selector can be split in turn into several
     * selectors separated from each other by the {@link info.magnolia.cms.core.Path#SELECTOR_DELIMITER}. In the above example, single selectors are x and foo=bar.
     * The latter is a {@code name=value} selector which is set in the MgnlContext as an attribute with scope {@code Context.LOCAL_SCOPE}. You can retrieve its value via {@code MgnlContext.getAttribute("foo")}.
     * <p>You can get and iterate over a full selector with the {@link #getSelectors()} method.<p>
     * <strong>Warning - this might change in the future - see MAGNOLIA-2343 for details.</strong>
     */
    public String getSelector() {
        return selector;
    }

    /**
     * <strong>Warning - this might change in the future - see MAGNOLIA-2343 for details.</strong>
     * The provided selector value is decoded upon settings according to rules described in {@link java.net.URLDecoder#decode(java.lang.String, java.lang.String)}
     */
    public void setSelector(String selector) {
        try {
            this.selector = URLDecoder.decode(selector, getCharacterEncoding());
        }
        catch (UnsupportedEncodingException e) {
            this.selector = selector;
        }

        if(StringUtils.isNotEmpty(selector)) {
            selectors = this.selector.split(Path.SELECTOR_DELIMITER);
        }
        for(String sel : selectors) {
            final String[] splitSelector = sel.split("=");
            if(splitSelector.length == 2) {
                MgnlContext.setAttribute(splitSelector[0], splitSelector[1]);
            }
        }
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * If the aggregation state local is not set explicitly the contexts locale is returned.
     * @return The aggregation state level locale, i.e. the locale that should be used for contents
     */
    public Locale getLocale() {
        if (locale == null) {
            return MgnlContext.getLocale();
        }

        return locale;
    }

    /**
     * @param locale The aggregation state level locale, i.e. the locale that should be used for contents
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isPreviewMode() {
        return isPreviewMode;
    }

    public void setPreviewMode(boolean previewMode) {
        isPreviewMode = previewMode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * WARNING: If passing URI without context path but it starts with same text as the context path it will be stripped off as well!!!
     * @param uri with contextPath (maybe)
     * @return uri stripped of the prefix matching the contextPath
     */
    protected String stripContextPathIfExists(String uri) {
        // MAGNOLIA-2064 & others ... remove context path only when it is actually present not when page name starts with context path
        String contextPath = MgnlContext.getContextPath();
        if (uri != null && uri.startsWith(contextPath + "/")) {
            return StringUtils.removeStart(uri, contextPath);
        }
        return uri;
    }

    /**
     * The original URI/URL can only be set once. A call to this methods resets the original URI/URL and allows to set them freshly.
     */
    public void resetURIs() {
        this.originalURI = null;
        this.originalURL = null;
        this.originalBrowserURI = null;
        this.originalBrowserURL = null;
        this.currentURI = null;
    }
    /**
     * @return an array containing the selectors found in the URI. The array is empty if no selector is in the current aggregation state.
     * Given a URI like this {@code http://www.magnolia-cms.com/node~value1~value2~.html?someparam=booo}, the entire selector is {@code ~value1~value2~}, whereas the
     * single selectors are <code>value1</code> and <code>value2</code>. Selectors are delimited by {@link Path#SELECTOR_DELIMITER}.
     * <p>
     * <strong>Warning - this might change in the future - see MAGNOLIA-2343 for details.</strong>
     */
    public String[] getSelectors() {
        return selectors;
    }
}
