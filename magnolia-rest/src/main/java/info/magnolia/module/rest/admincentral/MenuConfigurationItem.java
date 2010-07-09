/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.rest.admincentral;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class MenuConfigurationItem {

    private List<MenuItem> subs = new ArrayList<MenuItem>();
    private String i18nBaseName;
    private Messages messages;

    public List<MenuItem> getSubs() {
        return subs;
    }

    public void setSubs(List<MenuItem> subs) {
        this.subs = subs;
    }

    public boolean addSub(MenuItem menuItem) {
        return subs.add(menuItem);
    }

    public void setI18nBaseName(String i18nBaseName) {
        this.i18nBaseName = i18nBaseName;
    }

    public void initMessages() {
        initMessages(MessagesManager.getMessages());
    }

    public void initMessages(Messages parentMessages) {
        if (StringUtils.isEmpty(i18nBaseName))
            messages = parentMessages;
        else
            messages = MessagesUtil.chain(i18nBaseName, parentMessages);
        for (MenuItem menuItem : this.subs) {
            menuItem.initMessages(messages);
        }
    }

    public String getMessage(String key) {
        if (messages == null)
            return key;
        return messages.getWithDefault(key, key);
    }
}
