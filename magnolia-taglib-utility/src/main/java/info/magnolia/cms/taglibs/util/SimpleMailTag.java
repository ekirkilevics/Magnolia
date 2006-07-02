/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.taglibs.util;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.util.Resource;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Quick way to send a mail
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SimpleMailTag extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    private String nodeCollectionName = "mainColumnParagraphs";

    private String from;

    private String to;

    private String cc;

    private String bcc;

    private String subject;

    private String redirect;

    private String type;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SimpleMailTag.class);

    /**
     * Setter for <code>bcc</code>.
     * @param bcc The bcc to set.
     */
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    /**
     * Setter for <code>cc</code>.
     * @param cc The cc to set.
     */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /**
     * Setter for <code>from</code>.
     * @param from The from to set.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Setter for <code>nodeCollectionName</code>.
     * @param nodeCollectionName The nodeCollectionName to set.
     */
    public void setNodeCollectionName(String nodeCollectionName) {
        this.nodeCollectionName = nodeCollectionName;
    }

    /**
     * Setter for <code>to</code>.
     * @param to The to to set.
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Setter for <code>subject</code>.
     * @param subject The subject to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Setter for <code>redirect</code>.
     * @param redirect The redirect to set.
     */
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    /**
     * Setter for <code>type</code>.
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        StringBuffer body = new StringBuffer(); // build and send email

        Content activePage = Resource.getActivePage(request);
        Iterator it;
        try {
            it = activePage.getContent(nodeCollectionName).getChildren().iterator();
        }
        catch (RepositoryException e) {
            throw new NestableRuntimeException(e);
        }

        while (it.hasNext()) {
            Content node = (Content) it.next();
            String[] values = request.getParameterValues("field_" + node.getName());
            if (values == null) {
                values = request.getParameterValues(node.getName());
            }
            if (values != null) {
                body.append(node.getNodeData("title").getString() + "\n");
                for (int i = 0; i < values.length; i++) {
                    body.append(values[i] + "\n");
                }
                body.append("\n");
            }
        }

        String mailType = type;
        if (mailType == null) {
            mailType = MailConstants.MAIL_TEMPLATE_TEXT;
        }

        MgnlEmail email;
        try {
            email = MgnlMailFactory.getInstance().getEmailFromType(mailType);
            email.setToList(to);
            email.setCcList(cc);
            email.setBccList(bcc);
            email.setFrom(from);
            email.setSubject(subject);
            email.setBody(body.toString(), null);
            MgnlMailFactory.getInstance().getEmailHandler().prepareAndSendMail(email);
        }
        catch (Exception e) {
            // you may want to warn the user redirecting him to a different page...
            log.error(e.getMessage(), e);
        }

        if (StringUtils.isNotEmpty(redirect)) {
            HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
            try {
                response.sendRedirect(request.getContextPath() + redirect);
            }
            catch (IOException e) {
                // should never happen
                log.error(e.getMessage(), e);
            }
        }

        return super.doEndTag();
    }

    /**
     * @see javax.servlet.jsp.tagext.TagSupport#release()
     */
    public void release() {
        this.nodeCollectionName = null;
        this.to = null;
        this.from = null;
        this.cc = null;
        this.bcc = null;
        this.subject = null;
        this.type = null;
        super.release();
    }

}
