/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.cms.i18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This implementation support a language prefix like /en/*. To resolve the nodedatas it checks if a nodedata with the
 * following pattern exists on the content node: &lt;name&gt;_&lt;language&gt;
 * @author philipp
 * @version $Id$
 */
public class DefaultI18NSupport implements I18NSupport {

    private static Logger log = LoggerFactory.getLogger(DefaultI18NSupport.class);

    /**
     * Used to save the current language in the context
     */
    protected static final String CURRENT_LANGUAGE = "info.magnolia.cms.i18n.support.current";

    /**
     * English is the default fallback language
     */
    private String fallbackLanguage = "en";

    private boolean enabled = false;

    /**
     * The active languages
     */
    private Map languages = new HashMap();

    public String getCurrentLanguage() {
        return (String) MgnlContext.getAttribute(CURRENT_LANGUAGE);
    }

    public void setCurrentLanguage(String currentLanguage) {
        MgnlContext.setAttribute(CURRENT_LANGUAGE, currentLanguage);
    }

    public void setSessionLanguage(String currentLanguage) {
        MgnlContext.setAttribute(CURRENT_LANGUAGE, currentLanguage, Context.SESSION_SCOPE);
    }

    public String getFallbackLanguage() {
        return this.fallbackLanguage;
    }

    public void setFallbackLanguage(String fallbackLanguage) {
        this.fallbackLanguage = fallbackLanguage;
    }

    /**
     * Adds the language prefix to the uri.
     */
    public String toI18NURI(String uri) {
        if (!isEnabled()) {
            return uri;
        }
        String lang = getCurrentLanguage();
        if (languageSupported(lang)) {
            // nothing to do for relative links
            if(uri.startsWith("/")){
                return "/" + lang + uri;
            }
        }
        return uri;
    }

    /**
     * Removes the prefix
     */
    public String toURI(String i18nURI) {
        if (!isEnabled()) {
            return i18nURI;
        }

        String lang = languageFromURI(i18nURI);
        if (languageSupported(lang)) {
            return StringUtils.removeStart(i18nURI, "/" + lang);
        }
        return i18nURI;
    }

    /**
     * Extracts the language from the uri
     */
    public String languageFromURI(String i18nURI) {
        String lang = StringUtils.substringBetween(i18nURI, "/", "/");
        if(languageSupported(lang)){
            return lang;
        }
        return null;
    }

    public NodeData getNodeData(Content node, String name, String lang) throws RepositoryException {
        String nodeDataName = name + "_" + lang;
        if (node.hasNodeData(nodeDataName)) {
            return node.getNodeData(nodeDataName);
        }
        return null;
    }

    /**
     * Returns the nodedata with the name &lt;name&gt;_&lt;current language&gt; or &lt;name&gt;_&lt;fallback language&gt otherwise returns &lt;name&gt;.
     */
    public NodeData getNodeData(Content node, String name) {
        NodeData nd = null;

        if (isEnabled()) {
            try {
                // test for the current language
                String lang = getCurrentLanguage();
                if(languageSupported(lang)){
                    nd = getNodeData(node, name, lang);
                    if (!isEmpty(nd)) {
                        return nd;
                    }
                }

                // fallback
                lang = getFallbackLanguage();

                nd = getNodeData(node, name, lang);
                if (!isEmpty(nd)) {
                    return nd;
                }
            }
            catch (RepositoryException e) {
                log.error("can't read i18n nodeData " + name + " from node " + node, e);
            }
        }

        // return the node data
        return node.getNodeData(name);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Collection getLanguages() {
        return this.languages.values();
    }

    public void addLanguages(LanguageDefinition ld) {
        if (ld.isEnabled()) {
            this.languages.put(ld.getId(), ld.getLocale());
        }
    }

    protected boolean languageSupported(String lang) {
        return StringUtils.isNotEmpty(lang) && languages.containsKey(lang);
    }

    /**
     * Checks if the nodedata field is empty
     */
    protected boolean isEmpty(NodeData nd) {
        if (nd != null && nd.isExist()) {
            // TODO use a better way to find out if it is empty
            return StringUtils.isEmpty(NodeDataUtil.getValueString(nd));
        }
        return true;
    }

}
