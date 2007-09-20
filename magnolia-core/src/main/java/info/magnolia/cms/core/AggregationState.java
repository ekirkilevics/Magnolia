/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import java.util.Locale;

import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.runtime.File;
import info.magnolia.context.MgnlContext;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AggregationState {
    private String characterEncoding;
    private String originalURI;
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

    // TODO : in the original Aggregator code, this actually gets the extension from the current URI, and
    // if none, from the original URI. Here we just get what's been set.
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

    public String getSelector() {
        return selector;
    }

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
}
