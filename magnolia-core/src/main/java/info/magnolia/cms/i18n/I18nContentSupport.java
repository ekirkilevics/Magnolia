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
package info.magnolia.cms.i18n;

import java.util.Collection;
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
     * Get the current locale used for rendering content.
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
     * This method uses the current agregation state to set the locale used for the rendering process .
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

    /**
     * Available locales
     */
    public Collection<Locale> getLocales();

    public boolean isEnabled();

}
