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
package info.magnolia.link;

import java.util.Locale;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.Components;

/**
 * Creates links with the absolute path and prefixes it with selected locale if this is supported,
 * falls back to current one otherwise.
 */
public class I18nPathTransformer extends AbsolutePathTransformer{
    
    Locale locale;
    
    public I18nPathTransformer(){
    }
    
    public I18nPathTransformer(boolean addContextPath, boolean useURI2RepositoryMapping, boolean useI18N, Locale locale) {
        super(addContextPath, useURI2RepositoryMapping, useI18N);
        this.locale = locale;
    }

    @Override
    public String transform(Link uuidLink) {
        String linkStr;
        if(useURI2RepositoryMapping){
            linkStr = getURI2RepositoryManager().getURI(uuidLink);
        }
        else{
            linkStr = getURI2RepositoryManager().getDefaultMapping().getURI(uuidLink);
        }
        linkStr += getURISuffix(uuidLink);
        if(useI18N){
            linkStr = localizeURI(linkStr, locale);
        }
        linkStr = prefixLink(linkStr);
        return linkStr;
    }
    
    protected String localizeURI(String linkStr, Locale locale){
        I18nContentSupport i18n = Components.getComponent(I18nContentSupport.class);
        if(!i18n.getLocales().contains(locale.toString())){
            //not supported locale - use current one
            return i18n.toI18NURI(linkStr);
        }
        if(locale.equals(i18n.getDefaultLocale())){
            return linkStr;
        }
        if(linkStr.startsWith("/")){
            return "/" + locale.toString() + linkStr;
        }
        return linkStr;
    } 
}
