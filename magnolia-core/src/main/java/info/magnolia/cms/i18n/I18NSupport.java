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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import javax.jcr.RepositoryException;


/**
 * Support for i18n content. Used to rewrite uris and getting nodedatas based on the current language.
 * @author philipp
 * @version $Id$
 */
public interface I18NSupport {

    public String getCurrentLanguage();

    public void setCurrentLanguage(String currentLanguage);

    public void setSessionLanguage(String currentLanguage);

    public String getFallbackLanguage();

    public void setFallbackLanguage(String fallbackLanguage);

    public String toI18NURI(String uri);

    public String toURI(String i18nURI);

    public String languageFromURI(String i18nURI);

    /**
     * Returns the NodeData object based on the language passes.
     */
    public NodeData getNodeData(Content node, String name, String lang) throws RepositoryException;

    /**
     * Returns the NodeData object based on the current language.
     */
    public NodeData getNodeData(Content node, String name);

}