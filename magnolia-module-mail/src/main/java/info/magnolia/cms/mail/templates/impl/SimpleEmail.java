package info.magnolia.cms.mail.templates.impl;

import info.magnolia.cms.mail.templates.MgnlEmail;

import java.util.Map;

import javax.mail.Session;


/**
 * Date: Mar 30, 2006 Time: 1:01:01 PM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class SimpleEmail extends MgnlEmail {

    public SimpleEmail(Session session) throws Exception {
        super(session);
        this.setHeader(CONTENT_TYPE, TEXT_PLAIN_UTF);
    }

    public void setBody(String body, Map parameters) throws Exception {
        this.setContent(body, TEXT_PLAIN_UTF);
    }
}
