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
package info.magnolia.cms.mail;


import info.magnolia.cms.mail.templates.MgnlEmail;
import org.subethamail.wiser.WiserMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * Date: Mar 31, 2006
 * Time: 9:14:00 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailFactoryTest extends AbstractMailTest {

    public void testSimpleMail() throws Exception {

        params.put(MailTemplate.MAIL_TYPE, MailConstants.MAIL_TEMPLATE_SIMPLE);
        params.put(MailTemplate.MAIL_BODY, "test message");

        MgnlEmail email = factory.getEmail(params);
        String subject = "Test simple";
        email.setSubject(subject);
        email.setToList(TEST_RECIPIENT);
        email.setFrom(TEST_SENDER);

        handler.prepareAndSendMail(email);

        assertTrue(wiser.getMessages().size() == 1);
        Iterator emailIter = wiser.getMessages().iterator();
        WiserMessage message = (WiserMessage) emailIter.next();
        assertTrue(message.getMimeMessage().getSubject().equals(subject));

    }

}
