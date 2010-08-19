/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.module.admincentral.dialog;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.i18n.MessagesUtil;

/**
 * Provides functionality for hierarchical inheritance of i18n awareness of various UI components.
 * @author had
 * @version $Id: $
 */
public abstract class I18nAwareComponent {

    // could also use static import if we want do depend on m-gui (DialogControlImpl)
    protected static final String I18N_BASENAME_PROPERTY = "i18nBasename";

    private Messages messages;
    private String i18nBasename;

    /**
     * Get i18n aware component or null if this is the top aware component.
     */
    abstract public I18nAwareComponent getI18nAwareParent();

    /**
     * Get the AbstractMessagesImpl object for this dialog/control. It checks first if there was a bundle defined
     * <code>i18nBasename</code>, then it tries to find the parent with the first definition.
     * @return
     * @origin m-gui DialogControlImpl
     */
    public Messages getMessages() {
        if (messages == null) {
            // if this is the root
            if (this.getI18nAwareParent() == null) {
                messages = MessagesManager.getMessages();
            }
            else {
                // try to get it from the control nearest to the root
                messages = this.getI18nAwareParent().getMessages();
            }
            // if this control defines a bundle (basename in the terms of jstl)
            String basename = this.getI18nBasename();
            if (StringUtils.isNotEmpty(basename)) {
                // extend the chain with this bundle
                messages = MessagesUtil.chain(basename, messages);
            }
        }
        return messages;
    }


    private String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }

}
