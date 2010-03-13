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
package info.magnolia.module.mail.pages;

import java.util.Iterator;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MailTemplate;
import info.magnolia.module.mail.commands.MailCommand;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMailPage extends TemplatedMVCHandler {
    private static final Logger log = LoggerFactory.getLogger(SendMailPage.class);

    public SendMailPage(String name, HttpServletRequest request,
            HttpServletResponse response) {
        super(name, request, response);
    }

    public String send() {

        // Retrieve the context
        Context ctx = MgnlContext.getInstance();
        MailCommand command = new MailCommand();
        if(command.execute(ctx)) {
            AlertUtil.setMessage(getMessages().get("page.form.success"));
        } else {
            //message already set in the command
        }

        return VIEW_SHOW;
    }

    public Iterator<MailTemplate> getTemplates() {
        return MailModule.getInstance().getTemplatesConfiguration().iterator();
    }

    public Iterator<String> getTypes() {
        return MailModule.getInstance().getFactory().getRenderers().keySet().iterator();
    }

    public Messages getMessages() {
        return MessagesManager.getMessages("info.magnolia.module.mail.messages");
    }

}
