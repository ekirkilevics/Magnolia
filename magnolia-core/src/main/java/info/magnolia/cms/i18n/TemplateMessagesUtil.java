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
package info.magnolia.cms.i18n;

/**
 * This class helps to get the messages used in templates (paragraphs, dialogs, ..). First it make a lookup in
 * messages_templating_custom and then in messages_templating..<br>
 * If a string is not found it returns directly the key without ?
 *
 * This class is deprecated use the fallBackMessages property of the Message class to make chains.
 * @author philipp
 * @deprecated officially since 4.0 - informally since 3.0
 */
public final class TemplateMessagesUtil {

    /**
     * Use this basename if the string is not found in the custom basename.
     */
    public static final String DEFAULT_BASENAME = "info.magnolia.module.admininterface.messages_templating"; //$NON-NLS-1$

    /**
     * Name of the custom basename.
     */
    public static final String CUSTOM_BASENAME = "info.magnolia.module.admininterface.messages_templating_custom"; //$NON-NLS-1$

    /**
     * Util has no public constructor.
     */
    private TemplateMessagesUtil() {
    }

    /**
     * Get the messages for the templates.
     * @return
     */
    public static Messages getMessages() {
        return MessagesUtil.chain(new String[]{
            TemplateMessagesUtil.CUSTOM_BASENAME,
            TemplateMessagesUtil.DEFAULT_BASENAME});
    }
}
