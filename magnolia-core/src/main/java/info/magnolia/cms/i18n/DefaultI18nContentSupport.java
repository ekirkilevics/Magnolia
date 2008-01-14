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
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This implementation support a language prefix like /en/*. To resolve the nodedatas it checks if a nodedata with the
 * following pattern exists on the content node: &lt;name&gt;_&lt;language&gt;
 * @author philipp
 * @version $Id$
 */
public class DefaultI18nContentSupport implements I18nContentSupport {

    private static Logger log = LoggerFactory.getLogger(DefaultI18nContentSupport.class);

    /**
     * English is the default fallback language
     */
    private Locale fallbackLocale = new Locale("en");

    private boolean enabled = false;

    /**
     * The active locales
     */
    private Map locales = new HashMap();

    public Locale getLocale() {
        return MgnlContext.getAggregationState().getLocale();
    }

    public void setLocale(Locale locale) {
        MgnlContext.getAggregationState().setLocale(locale);
    }

    public Locale getFallbackLocale() {
        return this.fallbackLocale;
    }

    public void setFallbackLocale(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
    }

    /**
     * Adds the language prefix to the uri.
     */
    public String toI18NURI(String uri) {
        if (!isEnabled()) {
            return uri;
        }
        Locale locale = getLocale();
        if (isLocaleSupported(locale)) {
            // nothing to do for relative links
            if(uri.startsWith("/")){
                return "/" + locale.toString() + uri;
            }
        }
        return uri;
    }

    /**
     * Removes the prefix
     */
    public String toRawURI(String i18nURI) {
        if (!isEnabled()) {
            return i18nURI;
        }

        Locale locale = getLocale();
        if (isLocaleSupported(locale)) {
            return StringUtils.removeStart(i18nURI, "/" + locale.toString());
        }
        return i18nURI;
    }

    /**
     * Extracts the language from the uri
     */
    public Locale determineLocale() {
        final String i18nURI = MgnlContext.getAggregationState().getCurrentURI();
        Locale locale = getFallbackLocale();

        String localeStr = StringUtils.substringBetween(i18nURI, "/", "/");
        if(StringUtils.isNotEmpty(localeStr)){
            String[] localeArr = StringUtils.split(localeStr, "_");
            if(localeArr.length ==1){
                locale = new Locale(localeArr[0]);
            }
            else if(localeArr.length == 2){
                locale = new Locale(localeArr[0],localeArr[1]);
            }
        }
        if(!isLocaleSupported(locale)){
            locale = getFallbackLocale();
        }
        return locale;
    }

    public NodeData getNodeData(Content node, String name, Locale locale) throws RepositoryException {
        String nodeDataName = name + "_" + locale;
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
                Locale locale = getLocale();
                nd = getNodeData(node, name, locale);
                if (!isEmpty(nd)) {
                    return nd;
                }

                // fallback
                locale = getFallbackLocale();

                nd = getNodeData(node, name, locale);
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

    public Collection getLocales() {
        return this.locales.values();
    }

    public void addLocale(LocaleDefinition ld) {
        if (ld.isEnabled()) {
            this.locales.put(ld.getId(), ld.getLocale());
        }
    }

    protected boolean isLocaleSupported(Locale locale) {
        return locale != null && locales.containsKey(locale.toString());
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
