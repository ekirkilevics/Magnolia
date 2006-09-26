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
import info.magnolia.cms.util.ExclusiveWrite;
import info.magnolia.cms.util.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private String replyTo;

    private String subject;

    private String redirect;

    private String type;
    
    /**
     * Alternatively to the type you can use a template to render the email dynamically.
     */
    private String template;

    /**
     * log the mails
     */
    private boolean logging;

    /**
     * encoding of the log file 
     */
    private String loggingEncoding = "UTF8";

    /**
     * directory to place the log files
     */
    private String loggingDirectory = "/mailtracking";

    /**
     * The extension for the log files
     */
    private String loggingExtension = "log";
    
    /**
     * Name of the file. Prefix only.
     */
    private String loggingFilename;
    
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
     * Setter for <code>replyTo</code>.
     * @param replyTo The replyTo to set.
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
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
    
    public void setLogging(boolean trackMail) {
        this.logging = trackMail;
    }

    
    public String getTemplate() {
        return template;
    }

    
    public void setTemplate(String template) {
        this.template = template;
    }


    /**
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        StringBuffer body = new StringBuffer(); // build and send email
        
        // tracking mail: Excel friendly csv format
        StringBuffer mailTitles = new StringBuffer();
        StringBuffer mailValues = new StringBuffer();
        // timestamp
        mailValues.append(DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()))).append(';');
        
        if(nodeCollectionName == null){
            this.nodeCollectionName = Resource.getLocalContentNodeCollectionName(request);
        }
        
        Content activePage = Resource.getActivePage(request);
        Content fieldsNode = null;
        Iterator it;
        try {
            Content localContentNode = Resource.getLocalContentNode(request);
            if(localContentNode != null && localContentNode.hasContent(nodeCollectionName)){
                fieldsNode = localContentNode.getContent(nodeCollectionName);
            }
            else{
                fieldsNode = activePage.getContent(nodeCollectionName);
            }
            it = fieldsNode.getChildren().iterator();
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
                body.append(node.getNodeData("title").getString()).append('\n');
                mailTitles.append(excelCSVFormat(node.getNodeData("title").getString())).append(';');
                StringBuffer csvValue = new StringBuffer();
                for (int i = 0; i < values.length; i++) {
                    body.append(values[i]).append('\n');
                    csvValue.append(values[i]);
                    if(i < values.length-1){
                    	csvValue.append('\n');
                    }
                }
                mailValues.append(excelCSVFormat(csvValue.toString())).append(';');
                body.append("\n");
            }
        }

        if(logging){
        	trackMail(request, activePage.getHandle(), mailTitles, mailValues);
        }
        
        String mailType = type;
        if (StringUtils.isEmpty(mailType)) {
            mailType = MailConstants.MAIL_TEMPLATE_TEXT;
        }

        MgnlEmail email;
        try {
            // TODO: avoid those kinds of redundacies in the mail system
            if(StringUtils.isEmpty(template)){
                email = MgnlMailFactory.getInstance().getEmailFromType(mailType);
                email.setBody(body.toString(), null);
            }
            else{
                Map parameters = new HashMap(request.getParameterMap());
                parameters.put("all", body.toString());
                email = MgnlMailFactory.getInstance().getEmailFromTemplate(template, parameters);
            }
            email.setToList(to);
            email.setCcList(cc);
            email.setBccList(bcc);
            email.setReplyToList(replyTo);
            email.setFrom(from);
            email.setSubject(subject);
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
        release();
        return super.doEndTag();
    }

    protected String excelCSVFormat(String str) {
        if(!StringUtils.containsNone(str, "\n;")){
            return "\"" + StringUtils.replace(str, "\"","\"\"") + "\"";
        }
        return str;
    }
    
    protected void trackMail(HttpServletRequest request, String activePagePath, StringBuffer titles, StringBuffer values){
        String fileName =  this.getLoggingFilename();
        if(StringUtils.isEmpty(fileName)){
            activePagePath = StringUtils.removeStart(activePagePath, "/");
            fileName = StringUtils.replace(activePagePath, "/", "_");
        }
        
    	fileName = fileName + "_" + new GregorianCalendar().get(GregorianCalendar.WEEK_OF_YEAR) + "." + getLoggingExtension();
    	String folder = pageContext.getServletContext().getRealPath(this.getLoggingDirectory()); 
    	
    	synchronized (ExclusiveWrite.getInstance()) {
	    	new File(folder).mkdirs();
	    	
	    	File file = new File(folder + File.separator + fileName);
	    	boolean exists = file.exists();
	    	
	    	
	    	try {
	    		FileOutputStream out = new FileOutputStream(file, true);
	    		if(!exists){
	        		out.write("Timestamp;".toString().getBytes(this.getLoggingEncoding()));
                    titles.replace(titles.length()-1, titles.length(), "\n");
	        		out.write(titles.toString().getBytes(this.getLoggingEncoding()));
	        	}
	    		values.replace(values.length()-1, values.length(), "\n");
	        	out.write(values.toString().getBytes(this.getLoggingEncoding()));
	        	out.flush();
	        	out.close();
	        	
			} catch (Exception e) {
				log.error("Exception while tracking mail", e);
			} 
		} 
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
        this.template = null;
        super.release();
    }

    
    public String getLoggingDirectory() {
        return loggingDirectory;
    }

    
    public void setLoggingDirectory(String loggingDirectory) {
        this.loggingDirectory = loggingDirectory;
    }

    
    public String getLoggingEncoding() {
        return loggingEncoding;
    }

    
    public void setLoggingEncoding(String loggingEncoding) {
        this.loggingEncoding = loggingEncoding;
    }

    
    public boolean isLogging() {
        return logging;
    }

    public void setLoggingExtension(String loggingExtension) {
        this.loggingExtension = loggingExtension;
    }

    public String getLoggingExtension() {
        return loggingExtension;
    }

    public void setLoggingFilename(String loggingFilename) {
        this.loggingFilename = loggingFilename;
    }

    public String getLoggingFilename() {
        return loggingFilename;
    }


}
