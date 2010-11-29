/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.LocaleUtils;


/**
 *
 * @author pbracher
 * @version $Revision$ ($Author$)
 */
public class JavascriptMessagesPage extends JavascriptIncludePage {

    private String localeStr = MessagesManager.getInstance().getDefaultLocale().toString();

    private Collection<String> bundles = new ArrayList<String>();

    public JavascriptMessagesPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public void renderHtml(String view) throws IOException {
        final Locale locale = LocaleUtils.toLocale(localeStr);
        //adminInterface or default messages
        Messages messages = MessagesManager.getMessages(locale);
        MessagesUtil.generateJavaScript(response.getWriter(), messages);

        for (String bundleName : this.bundles) {
            if(MessagesManager.DEFAULT_BASENAME.equals(bundleName)){
                //skip adminInterface bundle to avoid duplication
                continue;
            }
            messages = MessagesManager.getMessages(bundleName);
            MessagesUtil.generateJavaScript(response.getWriter(), messages);
        }
    }

    public String getLocale() {
        return localeStr;
    }

    public void setLocale(String language) {
        this.localeStr = language;
    }

    public Collection<String> getBundles() {
        return bundles;
    }

    public void setBundles(Collection<String> bundles) {
        this.bundles = bundles;
    }

    public void addBundle(String bundleName){
        this.bundles.add(bundleName);
    }

}
