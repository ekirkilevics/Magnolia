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
package info.magnolia.link;

import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;

/**
 * Creates links with the absolute path. Alternatively can prefix links with the webapp context path as well.
 * Transformer is also capable of applying URI2Repository mapping and i18n (mapping links to different path
 * based on the current locale value). See {@link AbstractI18nContentSupport} for details.
 * @author had
 * @version $Id:$
 */
public class AbsolutePathTransformer implements LinkTransformer {

    boolean addContextPath = true;

    boolean useURI2RepositoryMapping = true;

    boolean useI18N = true;

    /**
     * Creates new transformer.
     * @param addContextPath Flag to specify whether or not to prepend context path to created links.
     * @param useURI2RepositoryMapping Flag specifying whether or not to apply any URI2Repository mapping.
     * @param useI18N Flag specifying whether or not to translate link path based on current locale value.
     */
    public AbsolutePathTransformer(boolean addContextPath, boolean useURI2RepositoryMapping, boolean useI18N) {
        this.addContextPath = addContextPath;
        this.useURI2RepositoryMapping = useURI2RepositoryMapping;
        this.useI18N = useI18N;
    }

    /**
     * {@inheritDoc}
     */
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
            linkStr = I18nContentSupportFactory.getI18nSupport().toI18NURI(linkStr);
        }
        linkStr = prefixLink(linkStr);
        return linkStr;
    }

    protected URI2RepositoryManager getURI2RepositoryManager() {
        return URI2RepositoryManager.getInstance();
    }

    protected String prefixLink(String linkStr) {
        if(addContextPath){
            return MgnlContext.getContextPath() + linkStr;
        }
        return linkStr;
    }

    /**
     * Gets reminder of the URI after the path (i.e. anchor and parameters).
     */
    public String getURISuffix(Link uuidLink) {
        String anchor = uuidLink.getAnchor();
        String parameters = uuidLink.getParameters();

        return "" + (StringUtils.isNotEmpty(anchor)? "#" + anchor : "") +
        (StringUtils.isNotEmpty(parameters)? "?" + parameters : "");

    }

    public boolean isAddContextPath() {
        return this.addContextPath;
    }


    public void setAddContextPath(boolean addContextPath) {
        this.addContextPath = addContextPath;
    }


    public boolean isUseI18N() {
        return this.useI18N;
    }


    public void setUseI18N(boolean useI18N) {
        this.useI18N = useI18N;
    }


    public boolean isUseURI2RepositoryMapping() {
        return this.useURI2RepositoryMapping;
    }


    public void setUseURI2RepositoryMapping(boolean useURI2RepositoryMapping) {
        this.useURI2RepositoryMapping = useURI2RepositoryMapping;
    }

}

