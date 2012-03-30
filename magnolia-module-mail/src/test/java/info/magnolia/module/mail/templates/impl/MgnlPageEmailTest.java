package info.magnolia.module.mail.templates.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.module.mail.MailConstants;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MailTemplate;

import org.junit.Before;
import org.junit.Test;

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
