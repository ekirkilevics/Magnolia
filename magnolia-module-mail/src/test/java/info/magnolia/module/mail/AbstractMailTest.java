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
package info.magnolia.module.mail;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.content2bean.Content2BeanUtil;
import info.magnolia.module.mail.handlers.MgnlMailHandler;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import org.subethamail.wiser.Wiser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class AbstractMailTest extends RepositoryTestCase {
    public final static String TEST_RECIPIENT = "recipient@example.com";

    public final static String TEST_SENDER = "sender@example.com";

    public static final String TEST_FILE_JPG = "magnolia.jpg";

    public static final String TEST_FILE_PDF = "magnolia.pdf";

    public static int SMTP_PORT = 25013;

    protected MgnlMailFactory factory;

    protected MgnlMailHandler handler;

    protected Map params = new HashMap();

    protected Wiser wiser = new Wiser();

    public File getResourceFile(String filename) {
        return new File(getClass().getResource("/" + filename).getFile());
    }

    public String getResourcePath(String filename) {
        return getResourceFile(filename).getAbsolutePath();
    }

    public void setUp() throws Exception {
        super.setUp();

        ComponentsTestUtil.setImplementation(ServerConfiguration.class, ServerConfiguration.class);
        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);

        bootstrapSingleResource("/mgnl-bootstrap/mail/config.modules.mail.config.factory.xml");
        bootstrapSingleResource("/mgnl-bootstrap/mail/config.modules.mail.config.handler.xml");
        bootstrapSingleResource("/mgnl-bootstrap/mail/config.modules.mail.config.smtp.xml");
        bootstrapSingleResource("/mgnl-bootstrap/mail/config.modules.mail.config.templatesConfiguration.xml");

        Content content = ContentUtil.getContent("config", "/modules/mail/config");

        MailModule mailModule = (MailModule) Content2BeanUtil.toBean(content, true, MailModule.class);
        mailModule.start(null);
        params.put(MailConstants.SMTP_SERVER, "localhost");
        params.put(MailConstants.SMTP_PORT, "" + SMTP_PORT);

        factory = mailModule.getFactory();
        handler = mailModule.getHandler();
        wiser.setPort( SMTP_PORT);
        wiser.start();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        wiser.stop();
        // prevent random failure of the build in ubuntu due to not releasing socket fast enough.
        Thread.sleep(1000);
        super.tearDown();
    }

    /**
     * this test will fail when subject is not US-ASCII TODO: replace with mail parser or handle encoding and improve
     * pattern
     * @param message
     * @param subject
     * @return true is <code>message</code>'s subject equals <code>subject</code>
     */
    protected boolean hasMatchingSubject(String message, String subject) {
        Pattern pattern = Pattern.compile("Subject: " + subject);
        Matcher matcher = pattern.matcher(message);
        return matcher.find();
    }

}
