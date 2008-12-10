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
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;

import java.util.Locale;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AggregationState {
    private String characterEncoding;
    private String originalURI;
    private String originalURL;
    private String currentURI;
    private Content currentContent;
    private String extension;
    private File file;
    private String handle;
    private Content mainContent;
    private String repository;
    private String selector;
    private Template template;
    private Locale locale;

    public void setOriginalURI(String originalURI) {

        String decodedURI = Path.decodedURI(originalURI, getCharacterEncoding());
        if (this.originalURI != null && !this.originalURI.equals(decodedURI)) {
            throw new IllegalStateException("Original URI can only be set once ! Existing value is \"" + this.originalURI + "\", tried to replace it with \"" + decodedURI + "\"");
        }
        this.originalURI = decodedURI;
    }

    public void setCurrentURI(String currentURI) {
        this.currentURI = Path.decodedURI(currentURI, getCharacterEncoding());
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

    public String getExtension() {
        return extension;
    }

    // -- just plain getters and setters below:
    /**
     * Returns the original request URI, decoded and without the context path.
     * Can never be modified.
     */
    public String getOriginalURI() {
        return originalURI;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public Content getCurrentContent() {
        return currentContent;
    }

    public void setCurrentContent(Content currentContent) {
        this.currentContent = currentContent;
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
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
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

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }
}
