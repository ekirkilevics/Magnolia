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
package info.magnolia.cms.mail.servlets;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.commands.MailCommand;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.servlets.ContextSensitiveServlet;
import info.magnolia.cms.util.RequestFormUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Servlet to send email using the email module. Date: Mar 31, 2006 Time: 10:12:45 AM
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailServlet extends ContextSensitiveServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    static Logger log = LoggerFactory.getLogger(MgnlMailServlet.class);

    private static final String TYPE = "type";

    private static final String SUBJECT = "subject";

    private static final String FROM = "from";

    private static final String TEXT = "text";

    private static final String TORECIPIENTS = "torecipients";

    private static final String CCRECIPIENTS = "ccrecipients";

    private static final String PARAMETERS = "parameters";

    private static final String FILE = "file";

    private static final String TEMPLATE = "template";

    private static final String ATT_ID = "att";

    private static final String ACTION = "action";

    private static final String HTML_TEXT_AREA = "textarea";

    private static final String HTML_SMALL_TEXT_AREA = "smalltextarea";

    private static final String HTML_BIG_TEXT_AREA = "bigtextarea";

    private static final String HTML_TEXT = "text";

    private static final String HTML_SELECT = "select";

    private static final String HTML_FILE = "file";

    private static final String NONE = "<none>";

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        super.doPost(httpServletRequest, httpServletResponse);

        RequestFormUtil request = new RequestFormUtil(httpServletRequest);

        if (request.getParameter(ACTION) == null) {
            this.doGet(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            // Retrieve the context
            Context ctx = MgnlContext.getInstance();

            // get any possible attachment
            MultipartForm form = (MultipartForm) httpServletRequest.getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME);
            Document doc = form.getDocument("file");

            if (doc != null) {
                MailAttachment attachment = new MailAttachment(doc.getFile().toURL(), ATT_ID);
                ctx.put(MailConstants.ATTRIBUTE_ATTACHMENT, attachment);
            }

            // add all possible parameters to the context
            String template = request.getParameter(TEMPLATE);
            String parameters = request.getParameter(PARAMETERS);
            String to = request.getParameter(TORECIPIENTS);
            String cc = request.getParameter(CCRECIPIENTS);

            if (to != null) {
                ctx.put(MailConstants.ATTRIBUTE_TO, to);
            }
            if (cc != null) {
                ctx.put(MailConstants.ATTRIBUTE_CC, cc);
            }

            // convert optional parameters and add them to the context
            if (log.isDebugEnabled()) {
                log.info(ctx.getAttributes().toString());
            }
            HashMap map = convertToMap(parameters);
            ctx.putAll(map);

            if (isTemplate(template)) {
                // this is a template based email, just add the template
                ctx.put(MailConstants.ATTRIBUTE_TEMPLATE, template);
            }
            else {
                // this is a static email, add all parameters
                String type = request.getParameter(TYPE);
                String subject = request.getParameter(SUBJECT);
                String from = request.getParameter(FROM);
                String text = request.getParameter(TEXT);

                if (type != null) {
                    ctx.put(MailConstants.ATTRIBUTE_TYPE, type);
                }
                if (subject != null) {
                    ctx.put(MailConstants.ATTRIBUTE_SUBJECT, subject);
                }
                if (from != null) {
                    ctx.put(MailConstants.ATTRIBUTE_FROM, from);
                }
                if (text != null) {
                    ctx.put(MailConstants.ATTRIBUTE_TEXT, text);
                }
            }

            // execute the command
            MailCommand command = new MailCommand();
            command.execute(ctx);

            setMessage(false, null, "Mail was sent to " + to, httpServletResponse);
        }
        catch (Exception e) {
            setMessage(true, e, "Error while sending email ", httpServletResponse);
        }
        finally {
            doGet(httpServletRequest, httpServletResponse);
        }
    }

    private boolean isTemplate(String template) {
        return StringUtils.isNotEmpty(template) && (!template.trim().equalsIgnoreCase(NONE));
    }

    private void setMessage(boolean error, Exception e, String message, HttpServletResponse response)
        throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write("<h1>Message</h1>");
        if (!error) {
            writer.write("<p class=\"success\">");
        }
        else {
            writer.write("<p class=\"error\">");
            e.printStackTrace(writer);
        }
        writer.write(message);
        writer.write("</p>");
        writer.flush();
    }

    /**
     * Convert the string of parameters retrieved from the form into a HashMap.
     * @param parameters string from the text area of the form
     * @return <code>HashMap</code>
     * @throws IOException if fails
     */
    private HashMap convertToMap(String parameters) throws IOException {
        HashMap map = new HashMap();
        ByteArrayInputStream string = new ByteArrayInputStream(parameters.getBytes());
        Properties props = new Properties();
        props.load(string);

        Iterator iter = props.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            map.put(key, props.get(key));
        }
        log.info(map.toString());
        return map;
    }

    /**
     * CSS portion of the form
     * @return <code>String</code> containing the CSS portion of the form
     */
    private String getServletStyle() {
        StringBuffer buffer = new StringBuffer();
        buffer
            .append(""
                + "<style type=\"text/css\">\n"
                + "<!--\n "
                + "body    { background-color: rgb(255,255,255); color: rgb(0,0,0); margin: 0px 0px; font-size: 10pt ; font-family: verdana}"
                + "p.error       { font-size: 8pt; color: red; }"
                + "p.success { font-size: 8pt; color: green; }"
                + "p.comments { font-size: 7pt; }"
                + "textArea {color: blue }"
                + "input {background-color:white ; font-size: 10pt ; width: 400px}"
                + "textarea {background-color:white ; font-size: 10pt ; width: 400px}"
                + "table {font-size: 10pt ; width: 90%}"
                + "tr {font-size: 10pt}"
                + "td {font-size: 10pt}"
                + "h1 {font-family: verdana ; font-size : 12pt}"
                + "-->\n "
                + "</style>"
                + "");
        return buffer.toString();
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws ServletException, IOException {
        super.doGet(httpServletRequest, httpServletResponse);
        StringBuffer sb = displayForm();

        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(sb.toString());
        writer.flush();
    }

    /**
     * Display the mail form
     */
    private StringBuffer displayForm() {
        StringBuffer sb = new StringBuffer();

        sb.append(getServletStyle());
        sb.append("<center><h1>Email Servlet</h1></center>");

        sb.append("<form method=\"post\" enctype=\"multipart/form-data\">");
        sb.append("<input type=\"hidden\"  width=\"80%\" name=\"" + ACTION + "\" value=\"action\"/>");

        sb.append("<h1>Options for all emails</h1>");

        sb.append("<table>");
        addSection("To", TORECIPIENTS, HTML_TEXT, "The main recipients of the email", sb);
        addSection(
            "Parameters",
            PARAMETERS,
            HTML_TEXT_AREA,
            "Parameters that can be used when processing the email",
            sb);
        addSection(
            "Template",
            TEMPLATE,
            HTML_SELECT,
            "If a template is set, the type of the mail, the content and subject are retrieved from the repository.<br>"
                + "The configuration for template us under the following node (/modules/mail/config/templates)",
            sb);
        sb.append("</table>");

        sb.append("<h1>Options for statically defined emails</h1>");
        sb.append("<table>");
        addSection("From", FROM, HTML_TEXT, "The sender of the email", sb);
        addSection("Subject", SUBJECT, HTML_TEXT, "The subject of the email", sb);
        addSection(
            "Text",
            TEXT,
            HTML_BIG_TEXT_AREA,
            "The content of the email. Note that this is not used if a template is set",
            sb);
        addSection("Cc", CCRECIPIENTS, HTML_TEXT, "The cc recipients of the email", sb);
        addSection("Attach", FILE, HTML_FILE, "The attachment has a default id of '"
            + ATT_ID
            + "'. This is only limited in the servlet", sb);
        addSection(
            "Email Type",
            TYPE,
            HTML_SELECT,
            "This define the format of the email. In the future more implementation can be added",
            sb);
        sb.append("</table>");
        sb.append("<center><input type=\"submit\" value=\"send\"/></center>");
        sb.append("</form>");

        return sb;
    }

    /**
     * Factorized code for the form
     * @param label description of the entry
     * @param name to use for parameter
     * @param htmlInputType the type of the input
     * @param comments if needed for explanations
     * @param sb the stringbuffer to append the code to
     * @return the same string buffer as sb, appended with the form section
     */
    private StringBuffer addSection(String label, String name, String htmlInputType, String comments, StringBuffer sb) {
        sb.append("<tr>");
        sb.append("<td width=\"40%\">").append(label).append(" :</td>");
        sb.append("<td>");
        if (htmlInputType.equals(HTML_SMALL_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"2\"  name=\"").append(name).append("\"></textArea>");
        }
        else if (htmlInputType.equals(HTML_SELECT)) {
            sb.append(getSelectBox(name));
        }
        else if (htmlInputType.equals(HTML_BIG_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"5\"  name=\"").append(name).append("\"/></textArea>");
        }
        else if (htmlInputType.equals(HTML_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"4\"  name=\"").append(name).append("\"/></textArea>");
        }
        else if (htmlInputType.equals(HTML_TEXT)) {
            sb.append("<input type=\"text\" width=\"80%\"cols=\"80\" name=\"").append(name).append("\"/>");
        }
        else if (htmlInputType.equals(HTML_FILE)) {
            sb.append("<input cols=\"80\" width=\"80%\" type=\"file\" name=\"").append(name).append("\"/>");
        }
        sb.append("<p class=\"comments\">").append(comments).append("</p>");
        sb.append("</td>");
        sb.append("</tr>");
        return sb;
    }

    /**
     * Get an html select box string
     * @param type <code>TYPE</code> or <code>TEMPLATE</code>
     * @return html code for select box
     */
    private StringBuffer getSelectBox(String type) {
        if (type.equalsIgnoreCase(TYPE)) {
            return getMailTypeSelectBox();
        }
        else if (type.equalsIgnoreCase(TEMPLATE)) {
            return getTemplateSelectBox();
        }
        else {
            return new StringBuffer();
        }

    }

    private StringBuffer getMailTypeSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select name=\"" + TYPE + "\">");
        addOption(MailConstants.MAIL_TEMPLATE_TEXT, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_HTML, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_VELOCITY, buffer);
        buffer.append("</select>");
        return buffer;
    }

    private void addOption(String option, StringBuffer buffer) {
        buffer.append("<option value=\"").append(option).append("\">").append(option).append("");
    }

    private StringBuffer getTemplateSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select width=\"80%\"");
        try {
            List list = MgnlMailFactory.getInstance().listTemplates();

            // if no template, disable
            if (list.size() == 0) {
                buffer.append(" disabled ");
            }
            buffer.append(" name=\"" + TEMPLATE + "\">");

            addOption(NONE, buffer);
            for (int i = 0; i < list.size(); i++) {
                String item = (String) list.get(i);
                addOption(item, buffer);
            }
        }
        catch (Exception e) {
            log.error("Error while getting the templates");
        }
        buffer.append("</select>");
        return buffer;

    }

}
