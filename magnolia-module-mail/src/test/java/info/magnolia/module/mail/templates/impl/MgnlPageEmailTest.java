/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.module.mail.templates.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.module.mail.MailConstants;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MailTemplate;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for email template MgnlPageEmail.
 * 
 * @version $Id$
 */
public class MgnlPageEmailTest {

    MgnlPageEmail pageEmail;

    @Before
    public void setUp(){
        MailModule mailModule = new MailModule();
        Map<String, String> smtp = new HashMap<String, String>();
        smtp.put(MailConstants.SMTP_SERVER, "localhost");
        smtp.put(MailConstants.SMTP_PORT, "25");
        mailModule.setSmtp(smtp);

        MailTemplate mt= new MailTemplate();

        pageEmail = new MgnlPageEmail(mt);
    }

    @Test
    public void testCleanupHtmlCode(){
        String html = "<div cms:edit>";
        String cleanCode = pageEmail.cleanupHtmlCode(html);
        cleanCode = cleanCode.replaceAll("\n", "");

        assertEquals("<div></div>", cleanCode);

        html = "<div cms:edit />";
        cleanCode = pageEmail.cleanupHtmlCode(html);
        cleanCode = cleanCode.replaceAll("\n", "");

        assertEquals("<div />", cleanCode);
    }

    @Test
    public void testCleanupHtmlCode1(){
        String html = "<div class=\"nav\" cms:edit/>";
        String cleanCode = pageEmail.cleanupHtmlCode(html);
        cleanCode = cleanCode.replaceAll("\n", "");

        assertEquals("<div class=\"nav\" />", cleanCode);

        html = "<div class=\"nav\" cms:edit> </div>";
        cleanCode = pageEmail.cleanupHtmlCode(html);
        cleanCode = cleanCode.replaceAll("\n", "");

        assertEquals("<div class=\"nav\"></div>", cleanCode);
    }

    @Test
    public void testCleanupHtmlCode3(){
        String html = "<div cms:edit style=\"\"> </div> <div class=\"lvl1\"> <div id=nav> </div> <div class=\"lvl2\" cms:edit /> </div>";
        String cleanCode = pageEmail.cleanupHtmlCode(html);
        cleanCode = cleanCode.replaceAll("\n", "");

        assertEquals("<div style=\"\"></div><div class=\"lvl1\">  <div id=\"nav\"></div>  <div class=\"lvl2\" /></div>", cleanCode);
    }
}
