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

import java.util.Locale;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;

import javax.jcr.RepositoryException;


/**
 * Support for i18n content. Used to rewrite uris and getting nodedatas based on the current language.
 * @author philipp
 * @version $Id$
 */
public interface I18nContentSupport {

    /**
     * Get the current locale used for rendering content
     */
    public Locale getLocale();

    public void setLocale(Locale locale);

    /**
     * The default locale to be used. Can be used to fix the system to a certain language.
     */
    public Locale getFallbackLocale();

    public void setFallbackLocale(Locale fallbackLocale);

    /**
     * Transform the internal uri to a i18n uri. This method may add a prefix or suffix
     */
    public String toI18NURI(String uri);

    /**
     * Remove all i18n tokens form the uri. The uri can then be used to get the content.
     */
    public String toRawURI(String i18nURI);

    /**
     * This method uses the current agregation state to set the locale used for the rendering process 
     */
    public Locale determineLocale();

    /**
     * Returns the NodeData object based on the language passes.
     */
    public NodeData getNodeData(Content node, String name, Locale locale) throws RepositoryException;

    /**
     * Returns the NodeData object based on the current language.
     */
    public NodeData getNodeData(Content node, String name);

}