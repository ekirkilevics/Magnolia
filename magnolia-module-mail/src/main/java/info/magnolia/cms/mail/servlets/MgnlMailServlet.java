package info.magnolia.cms.mail.servlets;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.mail.MailConstants;
import info.magnolia.cms.mail.MgnlMailFactory;
import info.magnolia.cms.mail.handlers.MgnlMailHandler;
import info.magnolia.cms.mail.templates.MailAttachment;
import info.magnolia.cms.mail.templates.MgnlEmail;
import info.magnolia.cms.util.RequestFormUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * Servlet to send email using the email module.
 * Date: Mar 31, 2006
 * Time: 10:12:45 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlMailServlet extends javax.servlet.http.HttpServlet {

    Logger log = LoggerFactory.getLogger(MgnlMailServlet.class);
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

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

        RequestFormUtil request = new RequestFormUtil(httpServletRequest);

        if (request.getParameter(ACTION) == null) {
            this.doGet(httpServletRequest, httpServletResponse);
            return;
        }

        try {

            MultipartForm form = (MultipartForm) httpServletRequest.getAttribute(MultipartForm.REQUEST_ATTRIBUTE_NAME);
            Document doc = form.getDocument("file");

            MailAttachment attachment = null;
            if (doc != null) {
                attachment = new MailAttachment(doc.getFile(), ATT_ID);
            }


            String type = request.getParameter(TYPE);
            String subject = request.getParameter(SUBJECT);
            String from = request.getParameter(FROM);
            String text = request.getParameter(TEXT);
            String to = request.getParameter(TORECIPIENTS);
            String cc = request.getParameter(CCRECIPIENTS);
            String parameters = request.getParameter(PARAMETERS);
            String template = request.getParameter(TEMPLATE);


            MgnlMailFactory factory = MgnlMailFactory.getInstance();
            MgnlMailHandler handler = factory.getEmailHandler();

            HashMap map = convertToMap(parameters);

            if (template != null && !(template.equals(StringUtils.EMPTY))) {
                MgnlEmail email = factory.getEmailFromTemplate(template, map);
                email.setToList(factory.convertEmailList(to));
                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            } else {
                MgnlEmail email = factory.getEmailFromType(type);
                email.setFrom(from);
                email.setSubject(subject);
                email.setToList(factory.convertEmailList(to));
                email.setBody(text, map);
                if (attachment != null)
                    email.addAttachment(attachment);
                //email.setTemplate(template);
                email.setCcList(factory.convertEmailList(cc));
                handler.prepareAndSendMail(email);
            }

            setMessage(false, null, "Mail was sent to " + to, httpServletResponse);
        }
        catch (Exception e) {
            setMessage(true, e, "Error while sending email ", httpServletResponse);
        }
        finally {
            doGet(httpServletRequest, httpServletResponse);
        }
    }

    private void setMessage(boolean error, Exception e, String message, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write("<h1>Message</h1>");
        if (!error)
            writer.write("<p class=\"success\">");
        else {
            writer.write("<p class=\"error\">");
            e.printStackTrace(writer);
        }
        writer.write(message);
        writer.write("</p>");
        writer.flush();
    }

    private HashMap convertToMap(String parameters) throws IOException {
        HashMap map = new HashMap();
        StringBufferInputStream string = new StringBufferInputStream(parameters);
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

    private String getServletStyle() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("" +
                "<style type=\"text/css\">\n" +
                "<!--\n " +
                "body    { background-color: rgb(255,255,255); color: rgb(0,0,0); margin: 0px 0px; font-size: 10pt ; font-family: verdana}" +
                "p.error       { font-size: 8pt; color: red; }" +
                "p.success { font-size: 8pt; color: green; }" +
                "p.comments { font-size: 7pt; }" +
                "textArea {color: blue }" +
                "input {color:blue ; font-size: 10pt}" +
                "table {font-size: 10pt}" +
                "tr {font-size: 10pt}" +
                "td {font-size: 10pt}" +
                "h1 {font-family: verdana ; font-size : 12pt}" +
                "-->\n " +
                "</style>" +
                "");
        return buffer.toString();
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        displayForm(httpServletResponse);
    }

    private void displayForm(HttpServletResponse httpServletResponse) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(getServletStyle());

        sb.append("<h1>Email Servlet</h1>");

        // Email edit area
        sb.append("<form method=\"post\" enctype=\"multipart/form-data\">");
        sb.append("<input type=\"hidden\"  width=\"80%\" name=\"" + ACTION + "\" value=\"action\"/>");
        sb.append("<table>");

        addSection("From", FROM, HTML_TEXT, "The sender of the email", sb);
        addSection("Subject", SUBJECT, HTML_TEXT, "The subject of the email", sb);
        addSection("Text", TEXT, HTML_BIG_TEXT_AREA, "The content of the email. Note that this is not used if a template is set", sb);
        addSection("To", TORECIPIENTS, HTML_TEXT, "The main recipients of the email", sb);
        addSection("Cc", CCRECIPIENTS, HTML_TEXT, "The cc recipients of the email", sb);
        addSection("Parameters", PARAMETERS, HTML_TEXT_AREA, "Parameters that can be used when processing the email", sb);
        addSection("Attach", FILE, HTML_FILE, "The attachment has a default id of '" + ATT_ID + "'. This is only limited in the servlet", sb);
        addSection("Template", TEMPLATE, HTML_SELECT, "If a template is set, the type of the mail, the content and subject are retrieved from the repository.The configuration for template us under the following node (" + MailConstants.MAIL_TEMPLATES_PATH + ")", sb);
        addSection("Email Type", TYPE, HTML_SELECT, "This define the format of the email. In the future more implementation can be added", sb);

        sb.append("</table>");

        sb.append("<center><input type=\"submit\" value=\"send\"/></center>");
        sb.append("</form>");

        PrintWriter writer = httpServletResponse.getWriter();
        writer.write(sb.toString());
        writer.flush();
    }

    private StringBuffer addSection(String label, String name, String htmlInputType, String comments, StringBuffer sb) {
        sb.append("<tr>");
        sb.append("<td>" + label + " :</td>");
        sb.append("<td>");
        if (htmlInputType.equals(HTML_SMALL_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"2\"  name=\"" + name + "\"></textArea>");
        } else if (htmlInputType.equals(HTML_SELECT)) {
            sb.append(getSelectBox(name));
        } else if (htmlInputType.equals(HTML_BIG_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"10\"  name=\"" + name + "\"/></textArea>");
        } else if (htmlInputType.equals(HTML_TEXT_AREA)) {
            sb.append("<textArea  cols=\"80\" width=\"80%\"rows=\"5\"  name=\"" + name + "\"/></textArea>");
        } else if (htmlInputType.equals(HTML_TEXT)) {
            sb.append("<input type=\"text\" width=\"80%\"cols=\"80\" name=\"" + name + "\"/>");
        } else if (htmlInputType.equals(HTML_FILE)) {
            sb.append("<input cols=\"80\" width=\"80%\" type=\"file\" name=\"" + name + "\"/>");
        }
        sb.append("<p class=\"comments\">" + comments + "</p>");
        sb.append("</td>");
        sb.append("</tr>");
        return sb;
    }

    private StringBuffer getSelectBox(String type) {
        if (type.equalsIgnoreCase(TYPE))
            return getMailTypeSelectBox();
        else if (type.equalsIgnoreCase(TEMPLATE))
            return getTemplateSelectBox();
        else return new StringBuffer();

    }

    private StringBuffer getMailTypeSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select name=\"" + TYPE + "\">");
        addOption(MailConstants.MAIL_TEMPLATE_SIMPLE, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_STATIC, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_HTML, buffer);
        addOption(MailConstants.MAIL_TEMPLATE_VELOCITY, buffer);
        buffer.append("</select>");
        return buffer;
    }

    private void addOption(String option, StringBuffer buffer) {
        buffer.append("<option value=\"" + option + "\">" + option + "");
    }

    private StringBuffer getTemplateSelectBox() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<select ");
        try {
            ArrayList list = MgnlMailFactory.getInstance().listTemplatesFromRepository();

            // if no template, disable
            if (list.size() == 0)
                buffer.append(" disabled ");
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
