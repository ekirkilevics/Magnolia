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
package info.magnolia.cms.core;

import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;

import java.util.Locale;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AggregationState {
    private String characterEncoding;
    private String originalURI;
    private String originalURL;
    private String currentURI;
    private String extension;
    private File file;
    private String handle;
    private Content mainContent;
    private Content currentContent;
    private String repository;
    private String selector;
    private String templateName;
    private Locale locale;
    private boolean isPreviewMode;

    public void setOriginalURI(String originalURI) {
        final String strippedURI = stripContextPathIfExists(originalURI);
        if (this.originalURI != null && !this.originalURI.equals(strippedURI)) {
            throw new IllegalStateException("Original URI can only be set once ! Existing value is \"" + this.originalURI + "\", tried to replace it with \"" + strippedURI + "\"");
        }
        this.originalURI = strippedURI;
    }

    public void setCurrentURI(String currentURI) {
        this.currentURI = stripContextPathIfExists(currentURI);
    }

    /**
     * Returns the URI of the current request, decoded and without the context path.
     * This URI might have been modified by various filters.
     */
    public String getCurrentURI() {
        if (currentURI == null) {
            return originalURI;
        }
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
        } else {
            return uri;
        }
    }
}
